package pro.vlprojects.batcher.writer.metric

public enum class WriteMode {

    BATCH,

    /** The recovery path: one rejected record is enough to end up here. */
    ONE_BY_ONE,
}
