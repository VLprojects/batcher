package pro.vlprojects.batcher.queue

import pro.vlprojects.batcher.model.Batch
import java.time.Duration

/**
 * Holds cut batches until the writer takes them. Taking is a lease: a batch leaves for good only on
 * [BatchLease.complete], so a durable queue gives back what a crash interrupted.
 */
public interface BatchQueue<T : Any> {

    public val isDurable: Boolean

    /** Batches waiting, or null when the queue cannot tell cheaply. */
    public val size: Int?

    /** Closed and with nothing more to give: the writer's signal to leave. */
    public val isExhausted: Boolean

    /** Waits up to [timeout] for room, zero being no wait. A throw means the batch is not in. */
    public fun offer(batch: Batch<T>, timeout: Duration): OfferResult

    /** Null when nothing came within [timeout], and from the moment the queue [isExhausted]. */
    public fun poll(timeout: Duration): BatchLease<T>?

    /** Takes no more batches. A durable queue may be exhausted at once: what is in waits there. */
    public fun close()

    /** Once the writer has stopped: what is left, for the fallback. A durable queue keeps it and gives none. */
    public fun drain(): List<Batch<T>>
}
