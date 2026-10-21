package pro.vlprojects.batcher.fallback

/** Why records went to the fallback; whether a replay would write them depends on it. */
public enum class FallbackReason {

    /** The writing queue had no room for the batch. A replay writes them. */
    QUEUE_FULL,

    /** A stop had closed the writing queue before the batch came. A replay writes them. */
    QUEUE_CLOSED,

    /** The writing queue threw on the batch. A replay writes them. */
    QUEUE_FAILED,

    /** The storage refused them one by one as well. A replay fails again: a bad record. */
    PERSIST_FAILED,

    /** The stop ran out of its budget before they were written. A replay writes them. */
    STOP_TIMED_OUT,
}
