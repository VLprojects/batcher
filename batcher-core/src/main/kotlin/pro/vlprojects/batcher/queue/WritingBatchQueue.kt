package pro.vlprojects.batcher.queue

import pro.vlprojects.batcher.model.Batch
import java.time.Duration

/**
 * Holds cut batches until the writer takes them. A taken batch stays here until [confirm]: a durable
 * queue gives back what a crash cut short, and the in-memory one what a stop did.
 */
public interface WritingBatchQueue<T : Any> {

    public val isDurable: Boolean

    /** Batches waiting, or null when the queue cannot tell cheaply. */
    public val size: Int?

    /**
     * Closed, and nothing more will be taken: the writer's signal to leave. A durable queue is so at
     * once on close, since what it holds waits for the next start.
     */
    public val isClosedAndEmpty: Boolean

    /** Waits up to [timeout] for room, zero being no wait. A throw means the batch is not in. */
    public fun put(batch: Batch<T>, timeout: Duration): PutResult

    /** Null when nothing came within [timeout], and from the moment the queue [isClosedAndEmpty]. */
    public fun take(timeout: Duration): Batch<T>?

    /** Written or parked: the taken batch leaves the queue for good. */
    public fun confirm(batch: Batch<T>)

    /** Not dealt with: the taken batch goes back, ahead of the rest. */
    public fun putBack(batch: Batch<T>)

    /** Takes no more batches. */
    public fun close()

    /** Once the writer has stopped: what is left, for the fallback. A durable queue keeps it and gives none. */
    public fun takeRemaining(): List<Batch<T>>
}
