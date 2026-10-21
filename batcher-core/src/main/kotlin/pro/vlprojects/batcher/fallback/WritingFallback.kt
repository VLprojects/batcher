package pro.vlprojects.batcher.fallback

/**
 * Keeps what could not be written until something replays it, and throws when it cannot: silence
 * here turns a loss into a clean shutdown. [error] is null when nothing was thrown, as with a full
 * queue.
 */
public fun interface WritingFallback<in T : Any> {

    public fun handle(records: Collection<T>, reason: FallbackReason, error: Throwable?)

    public fun handle(record: T, reason: FallbackReason, error: Throwable?): Unit =
        handle(listOf(record), reason, error)
}
