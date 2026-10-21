package pro.vlprojects.batcher.metric

import java.time.Duration

public interface BufferMeter {

    /** Functions and not numbers: a meter reads them on every scrape for as long as the buffer lives. */
    public fun watchBuffer(records: () -> Int, oldestRecordWait: () -> Duration) {}

    public fun incrementBatchCutting(reason: BatchCutReason) {}

    /** Every run, not only the failures: the sum is the whole work of the max-age check. */
    public fun incrementAgeChecking(result: AgeCheckResult) {}
}
