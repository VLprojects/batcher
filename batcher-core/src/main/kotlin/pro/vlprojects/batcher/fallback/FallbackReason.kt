package pro.vlprojects.batcher.fallback

/** Why records went to the fallback; whether a replay would write them depends on it. */
public enum class FallbackReason {

    /** A buffer back-pressure policy turned them away. A replay writes them. */
    BUFFER_OVERFLOW,

    /** The writing queue had no room for the batch. A replay writes them. */
    QUEUE_OVERFLOW,

    /** A stop had closed the writing queue before the batch came. A replay writes them. */
    QUEUE_CLOSED,

    /** The writing queue threw on the batch. A replay writes them. */
    ENQUEUE_FAILED,

    /** The storage refused them one by one as well. A replay fails again: a bad record. */
    PERSISTING_FAILED,

    /** `shutdown-await-period` ran out before they were written. A replay writes them. */
    SHUTDOWN_PERIOD_EXPIRED,
}
