package pro.vlprojects.batcher.metric

public enum class BatchDispatchReason {

    /** Records come in faster than the buffer waits for them. */
    BUFFER_FULL,

    /** The oldest record waited as long as it may. */
    BUFFER_DEADLINE,
}
