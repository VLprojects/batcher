package pro.vlprojects.batcher.queue.fallback

/** Why the writing queue did not take a dispatched batch. A replay writes it, whichever it is. */
public enum class EnqueuingFallbackReason {

    /** The back-pressure policy still required it once the enqueue timeout ran out. */
    QUEUE_OVERFLOW,

    /** A stop had closed the queue before the batch came. */
    QUEUE_CLOSED,

    /** The queue threw on the batch. */
    ENQUEUE_FAILED,
}
