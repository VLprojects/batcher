package pro.vlprojects.batcher.metric

import java.time.Duration

/** Read on every scrape, on the registry's thread: holds no lock for long and keeps nothing. */
public interface BatcherGauges {

    public fun bufferedRecords(): Int

    /** Zero while the buffer is empty. */
    public fun oldestRecordWait(): Duration

    /** Null when the queue cannot tell cheaply. */
    public fun queuedBatches(): Int?
}
