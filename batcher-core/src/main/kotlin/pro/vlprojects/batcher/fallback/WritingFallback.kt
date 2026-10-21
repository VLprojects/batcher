package pro.vlprojects.batcher.fallback

import pro.vlprojects.batcher.dto.Record

/**
 * Keeps what could not be written until something replays it, and throws when it cannot: silence
 * here turns a loss into a clean shutdown. [error] is null when nothing was thrown, as with a full
 * queue.
 */
public fun interface WritingFallback<in R : Record<*>> {

    public fun handle(records: Collection<R>, reason: FallbackReason, error: Throwable?)

    public fun handle(record: R, reason: FallbackReason, error: Throwable?): Unit =
        handle(listOf(record), reason, error)
}
