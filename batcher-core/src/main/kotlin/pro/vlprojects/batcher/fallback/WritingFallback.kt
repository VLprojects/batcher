package pro.vlprojects.batcher.fallback

import pro.vlprojects.batcher.data.Record

/**
 * Keeps what could not be written until something replays it, and throws when it cannot: silence
 * here turns a loss into a clean shutdown. [error] is null when nothing was thrown, as with a stop
 * that ran out of time.
 */
public fun interface WritingFallback<in R : Record<*>> {

    public fun handle(records: Collection<R>, reason: WritingFallbackReason, error: Throwable?)

    public fun handle(record: R, reason: WritingFallbackReason, error: Throwable?): Unit =
        handle(listOf(record), reason, error)
}
