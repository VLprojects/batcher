package pro.vlprojects.batcher.queue

public enum class EnqueueStatus {

    ENQUEUED,

    /** Closed by a stop: the batch is still the caller's to place. */
    QUEUE_CLOSED,
}
