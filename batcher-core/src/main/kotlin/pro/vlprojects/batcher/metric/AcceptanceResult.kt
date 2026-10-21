package pro.vlprojects.batcher.metric

public enum class AcceptanceResult {

    ACCEPTED,

    /** Never entered the batcher, so these records are still the caller's. */
    REJECTED,

    /** Turned away by back pressure and dropped. */
    DISCARDED,

    /** Turned away by back pressure into the fallback. */
    FALLBACK,
}
