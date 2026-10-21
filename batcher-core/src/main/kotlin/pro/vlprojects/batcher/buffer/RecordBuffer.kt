package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.dto.Batch
import pro.vlprojects.batcher.listener.BatchReadyListener
import pro.vlprojects.batcher.policy.buffering.BufferingPolicy

/**
 * Collects records and dispatches them in batches once any of its [policies] is met. A policy due by
 * time needs a clock running somewhere, and that is the buffer's own, not the batcher's.
 */
public interface RecordBuffer<R : Any, U : BufferUsage> {

    /** Records outlive the process, so the source may acknowledge one as soon as [add] returns. */
    public val isDurable: Boolean

    /** Asked in this order; the first one met names the batch in the metrics. */
    public val policies: List<BufferingPolicy<U>>

    /** Null while empty. Read by the gauges on every scrape: has to be cheap, may be approximate. */
    public val usage: U?

    /**
     * From any thread, at any time; counts from the next dispatched batch. A batch goes to every listener in
     * the order they were added, and the records are let go only once all of them have returned. A
     * throw from any keeps the batch, and all of them get it again: a listener has to bear a repeat,
     * which it can tell by [Batch.id].
     */
    public fun addListener(listener: BatchReadyListener<R>)

    public fun removeListener(listener: BatchReadyListener<R>)

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
     * Stops taking records and dispatching batches. Returns what was still collecting for the caller to
     * place, or null: nothing was, or the buffer is durable and keeps it for the next start.
     */
    public fun close(): Batch<R>?
}
