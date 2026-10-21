package pro.vlprojects.batcher.queue

import pro.vlprojects.batcher.data.Batch
import pro.vlprojects.batcher.data.Record
import java.time.Duration

/**
 * Holds dispatched batches until the writer takes them. A taken batch stays here until [confirm]: a
 * durable queue gives back what a crash interrupted, and the in-memory one what a stop did.
 */
public interface WritingBatchQueue<R : Record<*>> {

    /** Set by [close]: no batch gets in any more. */
    public val isClosed: Boolean

    /**
     * Nothing left to take. Closed and empty is the writer's signal to leave. A durable queue is empty
     * at once on close, since what it holds waits for the next start.
     */
    public val isEmpty: Boolean

    /** Before the first batch: a durable queue restores what it held, taken or not, ahead of new ones. */
    public fun open()

    /** Room is the back-pressure policy's to judge: the queue takes what comes. A throw means the batch is not in. */
    public fun enqueue(batch: Batch<R>): EnqueueStatus

    /** Null when nothing came within [timeout], and from the moment the queue is [isClosed] and [isEmpty]. */
    public fun take(timeout: Duration): Batch<R>?

    /** Written or parked: the taken batch leaves the queue for good. */
    public fun confirm(batch: Batch<R>)

    /** Not dealt with: the taken batch goes back, ahead of the rest. */
    public fun requeue(batch: Batch<R>)

    /** Takes no more batches. */
    public fun close()

    /** Once the writer has stopped: what is left, for the fallback. A durable queue keeps it and gives none. */
    public fun takeAll(): List<Batch<R>>
}
