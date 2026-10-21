package pro.vlprojects.batcher.writer.fallback

/** Why records went to the writing fallback; whether a replay would write them depends on it. */
public enum class WritingFallbackReason {

    /** The storage refused them one by one as well. A replay fails again: a bad record. */
    PERSISTING_FAILED,

    /** `shutdown-await-period` ran out before they were written. A replay writes them. */
    SHUTDOWN_PERIOD_EXPIRED,
}
