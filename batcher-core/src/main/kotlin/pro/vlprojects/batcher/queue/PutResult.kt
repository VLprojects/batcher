package pro.vlprojects.batcher.queue

public enum class PutResult {

    IN_QUEUE,

    /** No room within the wait the put allowed. */
    QUEUE_FULL,

    /** Closed by a stop: the batch is still the caller's to place. */
    QUEUE_CLOSED,
}
