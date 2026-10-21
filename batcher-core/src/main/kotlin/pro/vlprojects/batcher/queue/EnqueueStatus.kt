package pro.vlprojects.batcher.queue

public enum class EnqueueStatus {

    ENQUEUED,

    /** No room within the wait the enqueue allowed. */
    QUEUE_OVERFLOW,

    /** Closed by a stop: the batch is still the caller's to place. */
    QUEUE_CLOSED,
}
