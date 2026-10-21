package pro.vlprojects.batcher.buffer.policy

/** What happens to a record a [BufferBackPressurePolicy] does not admit. */
public enum class BackPressureAction {

    /** Accept throws: the record is still the caller's, and the source redelivers it. */
    REJECT,

    /** Dropped and counted, for records that can be lost. */
    DISCARD,

    /** Goes to the fallback as `BUFFER_OVERFLOW`, for a replay later. */
    FALLBACK,
}
