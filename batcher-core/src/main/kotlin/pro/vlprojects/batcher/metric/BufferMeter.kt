package pro.vlprojects.batcher.metric

import pro.vlprojects.batcher.policy.buffering.BufferingPolicy
import java.time.Duration

public interface BufferMeter {

    /** Functions and not numbers: a meter reads them on every scrape for as long as the buffer lives. */
    public fun watchBuffer(records: () -> Int, oldestRecordWait: () -> Duration) {}

    /** [policy] is the one met; its name is the tag. */
    public fun incrementBatchDispatching(policy: BufferingPolicy<*>) {}

    /** Every run, not only the failures: the sum is the whole work of the max-age check. */
    public fun incrementAgeChecked(status: AgeCheckStatus) {}
}
