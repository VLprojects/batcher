package pro.vlprojects.batcher.metric

import pro.vlprojects.batcher.buffer.policy.DispatchPolicy
import java.time.Duration

public interface BufferMeter {

    /** Functions and not numbers: a meter reads them on every scrape for as long as the buffer lives. */
    public fun watchBuffer(records: () -> Int, oldestRecordWait: () -> Duration) {}

    /** [policy] is the one met; its name is the tag. */
    public fun incrementBatchDispatching(policy: DispatchPolicy<*>) {}

    /** What the buffer still held when it closed, dispatched with no policy met. */
    public fun incrementBatchDispatchingOnClose() {}

    /** Every scheduled check of [policy], not only the failures: the sum is the whole work of the schedule. */
    public fun incrementPolicyChecked(policy: DispatchPolicy<*>, status: PolicyCheckStatus) {}
}
