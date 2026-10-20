package pro.vlprojects.batcher.fallback

public enum class FallbackReason {

    /** The queue had no room for the batch. */
    BATCH_REJECTED,

    /** The queue failed to take the batch. */
    ENQUEUE_FAILED,

    /** The write failed, and so did every slice these records were in. */
    WRITE_FAILED,

    /** The stop ran out of its budget before these were written. */
    WRITING_STOPPED,
}
