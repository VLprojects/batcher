package pro.vlprojects.batcher.metric

/** Every way one run of a buffer's max-age check can end. */
public enum class AgeCheckResult {

    BATCH_CUT,

    /** Woken before the wait was over, so the check is back for the time that was left. */
    RESCHEDULED,

    /** Nothing was collecting, and the next record arms a check of its own. */
    BUFFER_EMPTY,

    /** The check threw and is armed again: a buffer that stops watching keeps its records. */
    FAILED,
}
