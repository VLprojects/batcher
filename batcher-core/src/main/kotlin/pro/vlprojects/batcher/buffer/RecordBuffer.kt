package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.listener.BatchReadyListener
import pro.vlprojects.batcher.model.Batch
import java.time.Instant

/**
 * Collects records and cuts them into batches by a rule of its own: size, age, bytes, a key. A cut
 * by age needs a clock running somewhere, and that is the buffer's own, not the batcher's.
 */
public interface RecordBuffer<T : Any> {

    /** Records outlive the process, so the source may acknowledge one as soon as [add] returns. */
    public val isDurable: Boolean

    /**
     * From any thread, at any time; counts from the next cut batch. A batch goes to every listener in
     * the order they were added, and the records are let go only once all of them have returned. A
     * throw from any keeps the batch, and all of them get it again: a listener has to bear a repeat,
     * which it can tell by [Batch.id].
     */
    public fun addListener(listener: BatchReadyListener<T>)

    public fun removeListener(listener: BatchReadyListener<T>)

    /**
     * Returns once the record is in. A throw means it is not, and it is still the caller's: with no
     * listener a cut batch has nowhere to go, so the buffer stops taking records.
     */
    public fun add(record: T)

    /**
     * Stops taking records and cutting batches. Returns what was still collecting for the caller to
     * place, or null: nothing was, or the buffer is durable and keeps it for the next start.
     */
    public fun close(): Batch<T>?

    /** The most records it holds: a batch is cut once [size] reaches it, if not earlier. */
    public val capacity: Int

    /** Read by the gauges on every scrape, as is [firstRecordAt]: has to be cheap, may be approximate. */
    public val size: Int

    /** When the first record still collecting arrived, as the cut batch will carry it; null while empty. */
    public val firstRecordAt: Instant?
}
