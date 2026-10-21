# batcher

A small library that turns a stream of single records into batched writes, with
its own buffer, write thread, failure recovery and metrics. Written for
ClickHouse, but it knows nothing about ClickHouse: the write itself is your code.

Contents:

1. [The problem](#1-the-problem)
2. [The library in brief](#2-the-library-in-brief)
3. [How ClickHouse takes inserts](#3-how-clickhouse-takes-inserts)
4. [Why `async_insert` is not always the answer](#4-why-async_insert-is-not-always-the-answer)
5. [How it works](#5-how-it-works)
6. [Abstractions](#6-abstractions)
7. [Extension points](#7-extension-points)
8. [Failure handling](#8-failure-handling)
9. [Lifecycle and shutdown](#9-lifecycle-and-shutdown)
10. [Guarantees](#10-guarantees)
11. [Configuration](#11-configuration)
12. [Metrics](#12-metrics)
13. [Build and publishing](#13-build-and-publishing)
14. [Out of scope for v1](#14-out-of-scope-for-v1)

## 1. The problem

Records arrive one at a time: a queue consumer gets a message, an HTTP handler
gets a request, a job reads a page. The storage wants them in groups. Writing
one row per record gives you, in order of appearance:

- a round trip to the database per record, so consumer threads spend their time
  waiting;
- in ClickHouse, one data part per record (see section 3);
- no place to put a record whose write failed, so the consumer either loses it
  or rejects the message and gets it again in a loop.

Writing in groups needs more than a list and a timer:

| Need | Why |
| --- | --- |
| Dispatch by size and by duration | A full batch must go at once; a half-full batch must not wait forever. |
| Accept while writing | The consumer must not wait for the previous batch to land. |
| Bounded memory | A slow storage must not turn into an unbounded queue. |
| Attribute failures | When a batch fails, you need to know which records to retry and which to park. |
| Lose nothing on shutdown | Buffered records must be written or parked before the process exits. |
| Visible behaviour | Batch size, wait time, queue depth and lost records must be on a dashboard. |

Every service that writes to ClickHouse writes this code again. That is what
this library is.

## 2. The library in brief

![Modules, and which of them a Spring Boot or a plain JVM service takes](docs/assets/modules-light.svg#gh-light-mode-only)
![Modules, and which of them a Spring Boot or a plain JVM service takes](docs/assets/modules-dark.svg#gh-dark-mode-only)

A batcher takes single records, holds them in memory, dispatches a batch when it is
full or when the oldest record has waited long enough, and writes batches on a
thread of its own. Around the write it adds recovery, back pressure, metrics and
an orderly shutdown. It never builds a query: what a write is stays in your code.

What you get:

- the consumer thread does no I/O: `accept` appends to a buffer and returns;
- memory is bounded, and a full queue has a defined outcome;
- a failed batch is retried record by record, and what still fails goes to a
  fallback, not to the floor;
- shutdown drains the buffer within a budget;
- metrics for every step, Micrometer out of the box;
- several named batchers in one application, each with its own settings;
- every part is an interface with a default, so you can replace one piece or
  assemble the whole pipeline yourself; the buffer and the queue are in memory
  by default, and their contracts leave room for durable ones.

Four artifacts, group `pro.vlprojects`. Spring Boot is optional, and so is
Micrometer:

| Artifact | For | Depends on |
| --- | --- | --- |
| `batcher-core` | any JVM service, with or without Spring: the engine and every interface | `slf4j-api` |
| `batcher-micrometer` | metrics without Spring | core, `micrometer-core` |
| `batcher-spring-boot` | auto-configuration, configuration properties, lifecycle | core, Spring Boot; the Micrometer adapter if it is on the classpath |
| `batcher-spring-boot-starter` | the usual way in: dependencies only | the three above |

The naming follows the Spring Boot reference: a module of your own is
`<name>-spring-boot`, the starter is `<name>-spring-boot-starter`, and nothing
starts with `spring-boot`. Configuration keys live under `batcher`, not under
`spring`.

The only thing you have to write is the batch write itself:

```kotlin
class HistoryWriter(private val history: HistoryService) : BatchWriter<HistoryRecord> {

    override fun persist(batch: Batch<HistoryRecord>) = history.saveAll(batch.records)

    override fun persist(record: HistoryRecord) = history.save(record)
}
```

With the starter, a batcher is one bean and a few settings:

```kotlin
@Bean
fun historyBatcher(batchers: BatcherFactory, writer: HistoryWriter): Batcher<HistoryRecord> =
    batchers.create("history", writer)
```

```yaml
batcher:
  instances:
    history:
      batch-size: 2000
      buffering-max-duration: 1s
```

Without Spring, the same thing is a builder:

```kotlin
val batcher = Batcher.builder<HistoryRecord>("history")
    .batchSize(2000)
    .bufferingMaxDuration(Duration.ofSeconds(1))
    .writer(HistoryWriter(history))
    .meter(MicrometerBatcherMeter("history", registry))
    .build()

batcher.start()
```

Both give the same object. The rest of this document is what it does and where
you can get in between.

## 3. How ClickHouse takes inserts

Each INSERT into a MergeTree table creates one data part: its own directory with
column files, indexes and metadata. Parts are merged in the background. Part
count, not row count, is the limit.

Thresholds per partition, with their documented defaults:

| Setting | Default | Effect |
| --- | --- | --- |
| `parts_to_delay_insert` | 1000 | inserts start to be slowed down |
| `max_delay_to_insert` | 1 | upper bound of that delay, in seconds |
| `parts_to_throw_insert` | 3000 | inserts fail with `TOO_MANY_PARTS` |
| `max_parts_in_total` | 100000 | the same, counted across all partitions |

The vendor's own guidance: at least 1000 rows per insert, ideally 10 000 to
100 000, and about one insert per second per table. A partition key with high
cardinality multiplies parts, because one insert touching N partitions creates
N parts.

So 2000 records a second is either 2000 parts a second, which kills the table
within minutes, or two inserts a second, which is normal load. The difference is
only where the records waited.

## 4. Why `async_insert` is not always the answer

ClickHouse can batch on its own: with `async_insert=1` the server keeps incoming
rows in memory and flushes them when a size, time or query-count threshold is
hit. Documented defaults today: `async_insert_max_data_size` 100 MiB,
`async_insert_busy_timeout_ms` 200 ms (1000 ms on Cloud),
`async_insert_max_query_number` 450, `wait_for_async_insert` 1. Those defaults
differ between versions and deployments, so read them off the server you write
to.

It solves the part-count problem. It does not solve the rest:

| | `async_insert` | Client-side batching |
| --- | --- | --- |
| Where rows wait | server memory | your process |
| Client thread with `wait_for_async_insert=1` | blocked until the server flushes | returns as soon as the record is buffered |
| Client thread with `wait_for_async_insert=0` | returns at once, no result reported | — |
| Who learns about a bad row | nobody, with `wait=0`: the error goes to the log and `system.asynchronous_inserts` | your code, with the batch in hand |
| Failed rows | no dead letter | your fallback |
| Batch boundaries | server timers, shared by all clients with the same query shape and settings | your configuration, per batcher |
| Needs server settings | yes | no |

The points that decide it in practice:

- **A blocked thread is still a blocked thread.** With `wait_for_async_insert=1`
  the insert returns only after the flush, so your consumer waits up to the
  server's timeout on every record. The thread count, not the part count,
  becomes the limit.
- **Fire-and-forget drops the result, not just the wait.** With
  `wait_for_async_insert=0` the server acknowledges a buffer, not a write. A
  parse error, a type error or a restart before the flush is something your
  service never hears about.
- **One bad row fails its whole payload.** Errors surface at flush time, and the
  failing payload is rejected as a whole. From the client you cannot tell which
  record was bad or write the rest one by one, because you no longer have them.
- **Buffers are shared.** Rows are grouped per query shape and settings, per
  node. Your batch boundaries are decided by other producers' traffic.
- **Deduplication depends on the version.** Async inserts are deduplicated the
  same way as synchronous ones only from version 26.2, per user query rather
  than per flushed batch, and plain MergeTree needs
  `non_replicated_deduplication_window` above zero.
- **Buffered rows are not queryable** until the flush.
- **Too many parts can still happen** when the partition key has high
  cardinality.

When `async_insert` alone is enough: uniform small traffic, a server you
control, and records you may lose without anyone noticing. When it is not: you
need the write result in the code that produced the record.

The two are not alternatives. Batch in the client to control size, latency and
failures; leave `async_insert` on as a second line of defence if your server
already has it.

## 5. How it works

One batcher is a short pipeline. With the default in-memory buffer and queue it
has two threads of its own.

![From accept to the storage](docs/assets/pipeline-light.svg#gh-light-mode-only)
![From accept to the storage](docs/assets/pipeline-dark.svg#gh-dark-mode-only)

- `accept` takes the lock, appends to the buffer and returns. It does no I/O.
- The record that fills the batch carries it out to the queue; nothing else
  happens on the consumer thread.
- The buffer thread, a daemon, watches how long the first record has waited and
  dispatches a batch once that is past the max duration.
- The queue is bounded. When it is full the batcher applies the back-pressure
  setting: park the batch, reject the record, or wait for room.
- One writer thread writes batches in order, one at a time.

The same in time: a batch dispatched by size, one by duration, and a write that fails once:

![Calls on the consumer, buffer and writer threads](docs/assets/sequence-light.svg#gh-light-mode-only)
![Calls on the consumer, buffer and writer threads](docs/assets/sequence-dark.svg#gh-dark-mode-only)

## 6. Abstractions

Everything in the pipeline above is an interface in `batcher-core`.

![The types of batcher-core and how they refer to each other](docs/assets/types-light.svg#gh-light-mode-only)
![The types of batcher-core and how they refer to each other](docs/assets/types-dark.svg#gh-dark-mode-only)

| Type | Responsibility | Default |
| --- | --- | --- |
| `Batcher<R>` | entry point: `accept(record)`, `accept(records)`, start, stop | the only implementation |
| `Batch<R>` | records, an id, the arrival of the first one | value type |
| `BatchWriter<R>` | one insert of one batch | **yours**, there is no default |
| `RecordBuffer<R>` | collect records and dispatch a batch once any of its policies is met | in memory, with a daemon thread for the duration |
| `BufferUsage` | what a buffer reports about the records it collects, one subtype per kind: `BufferCapacityUsage` (`countRecords()`), `BufferDurationUsage` (`measureDuration()`) | the in-memory buffer reports both |
| `BufferingPolicy<U>` | one rule for when a batch is due, over one kind of usage; fixed when the buffer is built, taken as policies over the buffer's own type, so one whose usage it does not report does not compile; a repeated name is refused | `BufferCapacityPolicy` (`batch-size`) and `BufferDurationPolicy` (`buffering-max-duration`) |
| `WritingBackPressurePolicy` | what a full writing queue means: reject the record, or how long to wait for room | by `back-pressure`: fallback, rejecting or blocking |
| `BatchDispatchedListener<R>` | take a batch the buffer has dispatched; any number, added and removed at run time | the batcher, which enqueues it in the writing queue |
| `WritingBatchQueue<R>` | hold dispatched batches until the writer takes one, and keep a taken one until it is confirmed | in memory, bounded |
| `WritingFallback<R>` | keep what could not be written, with the reason and the error | none: log at ERROR and count |
| `BatcherMeter`, `BufferMeter`, `WriterMeter` | report every event; each method does nothing by default | none in core, Micrometer in `batcher-micrometer` |

`BatchWriter` is the only thing you must write: `persist(batch)` for one insert
of a batch, `persist(record)` for the retry of a failed one. `Batch.id` can go to
the storage as a deduplication token: it stays the same when a durable queue
delivers the batch again.

### Handing a batch on

A stage lets go of a batch only once the next one has taken it:

- the buffer drops the records once every `BatchDispatchedListener` has returned; a
  throw from any leaves them in the buffer, and all the listeners get the batch
  again;
- the writer takes a batch from the writing queue and confirms it once the batch
  is written or parked; a stop that interrupts the write requeues it.

In memory this costs nothing. For a durable buffer or queue it is the point
where it commits, so a crash between the two steps repeats a batch instead of
losing it. Neither declares what it is: the difference is in `open` and
`close`. On `open` a durable stage restores what it held; on `close` it keeps
it for the next start, while an in-memory buffer dispatches it and an in-memory
queue hands it to the fallback. A
durable buffer also lets the source acknowledge a record as soon as `accept`
returns.

## 7. Extension points

Three ways to use the library, from least to most work.

**Use it as designed.** Add the starter, declare a `BatchWriter` bean, set
`batcher.*`. Buffer, queue, writer, metrics, lifecycle and fallback behaviour
come from auto-configuration.

**Replace one part.** Declare a bean of the type you want to own and
auto-configuration steps aside for it — every bean it creates is conditional on
the absence of yours. Named batchers get their own qualifier, so one batcher can
keep the defaults while another one does not.

| Seam | Replace it when |
| --- | --- |
| `BatchWriter<R>` | always: this is your insert |
| `WritingFallback<R>` | failed records must go somewhere: a log, a table, back to a queue |
| `BufferingPolicy<U>` | a batch is due by a rule of yours, for example a byte budget over a usage kind that counts bytes; it adds to the two defaults |
| `WritingBackPressurePolicy` | none of `fallback`, `reject` and `block` fits the source |
| `RecordBuffer<R>` | it must report a usage the default does not (bytes in memory), records must be grouped on the way in (a key per table), or survive a restart, for example in Redis |
| `WritingBatchQueue<R>` | batches must survive a restart, for example in Kafka, RabbitMQ or a table |
| `BatcherMeter`, `BufferMeter`, `WriterMeter` | your observability is not Micrometer |

`back-pressure` in section 11 picks one of the three ready back-pressure policies.

**Assemble your own.** `batcher-core` has no Spring dependency. Construct the
buffer, the writer and the batcher yourself, or keep only the pieces you need
and drive them from your own code.

## 8. Failure handling

A write failure is not an exception for the consumer. The batcher is already one
thread away from it, so it recovers instead.

![A failed batch is retried record by record](docs/assets/recovery-light.svg#gh-light-mode-only)
![A failed batch is retried record by record](docs/assets/recovery-dark.svg#gh-dark-mode-only)

Why one by one: a batch usually fails for one of two reasons. The storage is
unavailable, and then every record fails and goes to the fallback, or one record
is bad, and then only that one does and the other 499 land. Without the second
pass you cannot tell those apart, and you lose good records because of a bad
neighbour.

The retry is `persist(record)` for each record of the failed batch. What is
parked reaches the fallback as records, with one of five reasons and the error,
if there was one. The reason tells a fallback what a replay would do:

| Reason | Meaning | A replay |
| --- | --- | --- |
| `QUEUE_OVERFLOW` | the writing queue had no room | writes it |
| `QUEUE_CLOSED` | a stop had closed the writing queue before the batch came | writes it |
| `ENQUEUE_FAILED` | the writing queue threw on the batch | writes it |
| `PERSISTING_FAILED` | the storage refused these records one by one as well | fails again: a bad record |
| `SHUTDOWN_PERIOD_EXPIRED` | `shutdown-await-period` ran out before the records were written | writes it |

## 9. Lifecycle and shutdown

![States of a batcher](docs/assets/lifecycle-light.svg#gh-light-mode-only)
![States of a batcher](docs/assets/lifecycle-dark.svg#gh-dark-mode-only)

Shutdown is the part that decides whether you lose data on every deploy.

![What happens on stop](docs/assets/shutdown-light.svg#gh-light-mode-only)
![What happens on stop](docs/assets/shutdown-dark.svg#gh-dark-mode-only)

In order:

1. The batcher stops accepting, and the buffer closes. What it still holds is
   dispatched with no policy met, so the batches are enqueued in the writing
   queue as usual, waiting for room within the budget.
2. The queue closes. The writer works through what is in it until it is empty
   or the budget is spent.
3. A write still running at the end of the budget is interrupted and its batch
   requeued. Whatever the queue still holds goes to the fallback as
   `SHUTDOWN_PERIOD_EXPIRED`.

A durable buffer or queue keeps what it holds instead, for the next start.

The batcher stops in a late Spring phase, after the message listeners, so the
sources stop producing before the buffer stops accepting.

## 10. Guarantees

With the default in-memory buffer and queue, what holds:

- Records are written in arrival order, by one thread, one batch at a time.
- A record is accepted or it is not: `accept` either returns, meaning the record
  is in the buffer, or throws.
- A record that cannot be written is handed to the fallback; a record is counted
  as lost only when the fallback fails too, and that is logged at ERROR.
- Shutdown writes or parks what is buffered, within a configured budget.

What does not hold:

- **Durability.** The buffer is in memory. A hard kill loses the records it
  holds. If you cannot afford that, acknowledge the source message only after
  the write, which means not using a batcher for that path.
- **Exactly once.** A batch can be written twice: the insert can succeed while
  the client sees a timeout. Deduplicate in the storage —
  `ReplacingMergeTree` with `LIMIT 1 BY <id>` on read, or an
  `insert_deduplication_token`.
- **Order between batchers.** Two named batchers are independent.

A durable buffer and queue move the line:

- **Durability holds** as far as the stores do: a record is safe once `accept`
  returns, and a hard kill loses nothing either one has committed.
- **At least once, not at most once.** A crash between a write and its
  confirmation writes the batch again. Pass `Batch.id` as the
  `insert_deduplication_token`.
- **Order** holds only as far as the queue keeps it, and not at all across
  several instances writing from one queue.

## 11. Configuration

Defaults apply to every batcher; `instances` names them and overrides what it
needs.

```yaml
batcher:
  defaults:
    batch-size: 500              # records per write
    buffering-max-duration: 1s   # how long the first record of a batch may wait
    write-queue-capacity: 16     # batches in flight before back pressure
    shutdown-await-period: 15s   # budget for draining on stop
    slow-write-duration: 5s      # a write slower than this is logged
    back-pressure: fallback      # fallback | reject | block
  instances:
    history:
      batch-size: 2000
    audit:
      buffering-max-duration: 5s
      back-pressure: reject
```

Two numbers set the shape of the load: `batch-size` and
`buffering-max-duration`. Rows per second at full load is
`batch-size / buffering-max-duration`, and that is also the worst-case delay
before a record is queryable, give or take one policy check of the buffer. Pick the delay you can live with, then the size
that keeps inserts at roughly one per second per table.

`back-pressure` picks a `WritingBackPressurePolicy`, and a bean of your own
replaces it. It decides what a full queue means:

- `fallback` parks the batch and returns;
- `reject` checks for room before the record goes into the buffer and throws
  from `accept` when there is none, so the source keeps the record and
  redelivers it. A batch that is already dispatched is never thrown back: its records
  belong to callers that have returned;
- `block` makes the thread that dispatched the batch wait for room, up to a timeout,
  then parks it. A message listener that waits stops acknowledging, and the
  broker stops delivering.

`write-queue-capacity` is the memory bound:
`batch-size * (write-queue-capacity + 1)` records at most.

Each named instance gets its own metrics tag, its own threads and its own
`BatchWriter` bean, resolved by qualifier.

## 12. Metrics

Micrometer, prefix `batcher`, every series tagged with the batcher name. The
adapter lives in `batcher-micrometer` and works without Spring; with the starter
it is wired in when Micrometer is on the classpath.

| Series | Type | Tags | Answers |
| --- | --- | --- | --- |
| `batcher.acceptance` | counter, records | `result` | are records being rejected? |
| `batcher.records` | counter, records | `result` | persisted, sent to the fallback or lost? |
| `batcher.buffer.records` | gauge, records | | how full is the buffer? |
| `batcher.buffer.age` | gauge, seconds | | how long has the oldest record waited? |
| `batcher.buffer.dispatched` | counter, batches | `policy` | dispatched at capacity, at max duration or on close? |
| `batcher.batch.write` | timer | `mode` | how slow are writes, and how often do we recover? |
| `batcher.batch.size` | distribution summary, records | | are batches full? |
| `batcher.writer.queue.size` | gauge, batches | | is the storage keeping up? |

Two signals are enough for an alert: `batcher.records{result="lost"}` above zero,
and `batcher.buffer.age` near `buffering-max-duration` for minutes.

## 13. Build and publishing

As in `convertible`, the other published library: Gradle with the Kotlin DSL,
one root build with shared settings, versions in `gradle.properties`.

- group `pro.vlprojects`, four artifacts as listed in section 2;
- published to Maven Central through the Central Portal with the
  `com.vanniktech.maven.publish` plugin, signed, with sources and javadoc;
- a GitHub Actions workflow on a `*.*.*` tag runs
  `publishAndReleaseToMavenCentral`, with the signing key and portal credentials
  from repository secrets;
- Apache 2.0;
- current versions only, no support for older ones: Java 25, Kotlin 2.4,
  Spring Boot 4.1. The versions of shared dependencies follow that Spring Boot
  BOM;
- `explicitApi()` and a checked-in API dump, so a breaking change shows up in the
  diff of a pull request: `./gradlew updateKotlinAbi` rewrites the dump;
- every pull request runs `./gradlew build`: compilation, ktlint, the API check
  and the tests.

## 14. Out of scope for v1

- **An adaptive window.** `buffering-max-duration` could be derived from the
  measured arrival rate, so that batches stay close to `batch-size` under any
  load. A `RecordBuffer` of its own can do it; the library does not ship one.
- **Durable buffers and queues.** The contracts are in core; Redis, Kafka,
  RabbitMQ or a table come later as separate modules. Until then durability
  stays with the source. How a durable stage turns a record into bytes is
  decided with the first one.
- **Retries with a delay.** A failed record goes to the fallback immediately.
  Retry policy belongs to whatever reads the fallback.
- **A ready-made fallback.** Parking records in RabbitMQ or Kafka is a few lines
  against your own transport; the library ships the interface, not the binding.

---

The diagrams are generated by `docs/diagrams/build.py`, in a light and a dark
variant. It needs nothing but Python.
