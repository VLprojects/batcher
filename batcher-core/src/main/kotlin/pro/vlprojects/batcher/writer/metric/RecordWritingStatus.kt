package pro.vlprojects.batcher.writer.metric

public enum class RecordWritingStatus {

    PERSISTED,

    /** To be replayed from there. */
    SENT_TO_FALLBACK,

    /** Neither the storage nor the fallback took them: this one must stay at zero. */
    LOST,
}
