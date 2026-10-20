package pro.vlprojects.batcher.fallback

import pro.vlprojects.batcher.Batch

/**
 * Keeps what could not be written until something replays it, and throws when it cannot: silence
 * here turns a loss into a clean shutdown. [FallbackCause.reason] tells a bad record, which a replay
 * would only fail again, from a sound one that missed its turn.
 */
public fun interface WriteFallback<in T : Any> {

    public fun handle(batch: Batch<T>, cause: FallbackCause)
}
