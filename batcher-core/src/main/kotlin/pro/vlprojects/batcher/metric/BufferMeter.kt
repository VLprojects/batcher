package pro.vlprojects.batcher.metric

import pro.vlprojects.batcher.buffer.policy.BufferingPolicy
import java.time.Duration

public interface BufferMeter {

    /** Functions and not numbers: a meter reads them on every scrape for as long as the buffer lives. */
    public fun watchBuffer(records: () -> Int, oldestRecordWait: () -> Duration) {}

    /** [policy] is the one met; its name is the tag. */
    public fun incrementBatchDispatching(policy: BufferingPolicy<*>) {}

    /** What the buffer still held when it closed, dispatched with no policy met. */
    public fun incrementBatchDispatchingOnClose() {}

    /** Every check on the [policy]'s interval, not only the failures: the sum is the whole work of it. */
    public fun incrementPolicyChecked(policy: BufferingPolicy<*>, status: PolicyCheckStatus) {}
}
