package pro.vlprojects.batcher.metric

public enum class RecordWriteResult {

    WRITTEN,

    /** Handed to the write fallback, to be replayed from there. */
    FALLBACK,

    /** Neither the storage nor the fallback took them: this one must stay at zero. */
    LOST,
}
