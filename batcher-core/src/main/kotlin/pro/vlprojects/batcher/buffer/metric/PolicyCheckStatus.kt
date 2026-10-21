package pro.vlprojects.batcher.buffer.metric

/** Every way one scheduled check of a policy can end. */
public enum class PolicyCheckStatus {

    BATCH_DISPATCHED,

    /** Records were collecting, but the policy was not met yet. */
    NOT_DUE,

    BUFFER_EMPTY,

    /** The check threw, and the next one runs on time: a schedule that stops leaves the records stuck. */
    FAILED,
}
