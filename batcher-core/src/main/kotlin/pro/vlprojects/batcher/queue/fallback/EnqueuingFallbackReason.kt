package pro.vlprojects.batcher.queue.fallback

/** Why the writing queue did not take a dispatched batch. A replay writes it, whichever it is. */
public enum class EnqueuingFallbackReason {

    /** No room within the wait the back-pressure policy allows. */
    QUEUE_OVERFLOW,

    /** A stop had closed the queue before the batch came. */
    QUEUE_CLOSED,

    /** The queue threw on the batch. */
    ENQUEUE_FAILED,
}
