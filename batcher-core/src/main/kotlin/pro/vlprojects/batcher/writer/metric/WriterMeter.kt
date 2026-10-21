package pro.vlprojects.batcher.writer.metric

import java.time.Duration

public interface WriterMeter {

    /** [WriteMode.ONE_BY_ONE] is a batch that failed whole; what landed is [incrementRecordWriting]. */
    public fun incrementBatchWriting(mode: WriteMode, took: Duration) {}

    /** One series for the three statuses: a sum over it is every record the writer answered for. */
    public fun incrementRecordWriting(records: Int, status: RecordWritingStatus) {}
}
