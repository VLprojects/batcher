package pro.vlprojects.batcher.metric

/** Every way one firing of a buffer's deadline check can end. */
public enum class DeadlineCheckResult {

    BATCH_DISPATCHED,

    /** Woken before the wait was over, so the check is back for the time that was left. */
    CHECK_RESCHEDULED,

    /** Nothing was collecting, and the next record arms a check of its own. */
    NOTHING_BUFFERED,

    /** The check threw and is armed again: a buffer that stops watching keeps its records. */
    CHECK_FAILED,
}
