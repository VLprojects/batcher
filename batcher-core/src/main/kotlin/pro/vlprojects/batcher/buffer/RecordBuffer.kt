package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.buffer.policy.BufferingPolicy
import pro.vlprojects.batcher.buffer.policy.BufferingPolicyChecker
import pro.vlprojects.batcher.buffer.usage.BufferUsage
import pro.vlprojects.batcher.data.Record
import pro.vlprojects.batcher.dto.Batch
import pro.vlprojects.batcher.listener.BatchDispatchedListener

/**
 * Collects records and dispatches them in batches once any of its [policies] is met. It reports its
 * usage by implementing the [BufferUsage] subtypes it can count. A policy due by time is met with no
 * new record, so the buffer is built with a [BufferingPolicyChecker] that asks them on a schedule.
 */
public interface RecordBuffer<R : Record<*>> : BufferUsage {

    /**
     * Set when built. After every add each is asked in this order and dispatches for as long as it is
     * met with records collecting, then the next one is asked; the one that dispatched names the batch
     * in the metrics. A buffer takes them as policies over itself, so one whose usage it does not
     * report does not compile; two of one [name][BufferingPolicy.name] it must refuse.
     */
    public val policies: List<BufferingPolicy<*>>

    /**
     * From any thread, at any time; counts from the next dispatched batch. A batch goes to every listener in
     * the order they were added, and the records are let go only once all of them have returned. A
     * throw from any keeps the batch, and all of them get it again: a listener has to bear a repeat,
     * which it can tell by [Batch.id].
     */
    public fun addListener(listener: BatchDispatchedListener<R>)

    public fun removeListener(listener: BatchDispatchedListener<R>)

    /**
     * Before the first record, once the listeners are in: hands every policy to the checker. A durable
     * buffer also restores what it held at the last stop and dispatches it as the policies say.
     */
    public fun open()

    /**
     * Returns once the record is in. A throw means it is not, and it is still the caller's: with no
     * listener a dispatched batch has nowhere to go, so the buffer stops taking records.
     */
    public fun add(record: R)

    /**
     * One by one by default; a buffer may take them in one go instead. A throw leaves the caller the
     * record it came on and those after it.
     */
    public fun add(records: Collection<R>): Unit = records.forEach(::add)

    /**
     * Stops taking records and the checker, then empties itself before it returns. An in-memory buffer
     * dispatches what was still collecting with no policy met, a batch per group if it groups records
     * by a key; a durable one keeps it for the next [open]. Once closed, a throw from a listener is not
     * retried but comes out of here.
     */
    public fun close()
}
