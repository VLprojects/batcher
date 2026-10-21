package pro.vlprojects.batcher.metric

public enum class BatchDispatchReason {

    /** Records come in faster than the policy's max age. */
    CAPACITY_REACHED,

    /** The writer gets fewer records than the policy's capacity at a time. */
    MAX_AGE_REACHED,
}
