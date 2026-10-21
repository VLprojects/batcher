package pro.vlprojects.batcher.queue.fallback

import pro.vlprojects.batcher.data.Batch
import pro.vlprojects.batcher.data.Record

/**
 * Keeps a dispatched batch the writing queue did not take, and throws when it cannot: the buffer
 * lets go of the batch once this returns. [error] is null when nothing was thrown, as with a full queue.
 */
public fun interface EnqueuingFallback<in R : Record<*>> {

    public fun handle(batch: Batch<R>, reason: EnqueuingFallbackReason, error: Throwable?)
}
