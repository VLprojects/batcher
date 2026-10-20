package pro.vlprojects.batcher.queue

public enum class OfferResult {

    ACCEPTED,

    /** No room within the wait the offer allowed. */
    FULL,

    /** Closed by a stop: the batch is still the caller's to place. */
    CLOSED,
}
