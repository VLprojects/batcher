package pro.vlprojects.batcher.metric

import java.time.Duration

public interface WriterMeter {

    /** A function and not a number: a meter reads it on every scrape. Not called for a queue that cannot tell its size. */
    public fun watchWritingQueue(batches: () -> Int) {}

    /** [WriteMode.ONE_BY_ONE] is a batch that failed whole; what landed is [incrementRecordWriting]. */
    public fun incrementBatchWriting(mode: WriteMode, took: Duration) {}

    /** One series for the three statuses: a sum over it is every record the writer answered for. */
    public fun incrementRecordWriting(records: Int, status: RecordWritingStatus) {}
}
