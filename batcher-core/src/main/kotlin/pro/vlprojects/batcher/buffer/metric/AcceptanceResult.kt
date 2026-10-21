package pro.vlprojects.batcher.buffer.metric

public enum class AcceptanceResult {

    ACCEPTED,

    /** Turned away by back pressure before the buffer, to the accepting fallback. */
    REJECTED,
}
