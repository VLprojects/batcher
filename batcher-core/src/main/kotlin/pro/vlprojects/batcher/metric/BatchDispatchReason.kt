package pro.vlprojects.batcher.metric

public enum class BatchDispatchReason {

    /** Records come in faster than the buffer's max age. */
    CAPACITY_REACHED,

    /** The writer gets fewer records than the buffer's capacity at a time. */
    MAX_AGE_REACHED,
}
