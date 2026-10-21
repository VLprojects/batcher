package pro.vlprojects.batcher.metric

/** Every way one of the buffer's own policy checks can end. */
public enum class PolicyCheckStatus {

    BATCH_DISPATCHED,

    /** Records were collecting, but no policy was met yet. */
    NOT_DUE,

    BUFFER_EMPTY,

    /** The check threw, and the next one runs on time: a buffer that stops checking keeps its records. */
    FAILED,
}
