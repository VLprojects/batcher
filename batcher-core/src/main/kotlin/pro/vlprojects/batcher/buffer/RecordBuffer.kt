package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.model.Batch

/**
 * Collects records and cuts them into batches by a rule of its own: size, age, bytes, a key. A cut
 * by age needs a clock running somewhere, and that is the buffer's own, not the batcher's.
 */
public interface RecordBuffer<T : Any> {

    /** Records outlive the process, so the source may acknowledge one as soon as [add] returns. */
    public val isDurable: Boolean

    /** Before the first [add]: cut batches go to [listener] from then on. */
    public fun open(listener: BatchReadyListener<T>)

    /** Returns once the record is in. A throw means it is not, and it is still the caller's. */
    public fun add(record: T)

    /**
     * Stops taking records and cutting batches. Returns what was still collecting for the caller to
     * place, or null: nothing was, or the buffer is durable and keeps it for the next start.
     */
    public fun close(): Batch<T>?

    /** Read by the gauges on every scrape: has to be cheap, may be approximate. */
    public fun snapshot(): BufferSnapshot
}
