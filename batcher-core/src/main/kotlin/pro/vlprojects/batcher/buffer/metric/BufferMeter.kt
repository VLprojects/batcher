package pro.vlprojects.batcher.buffer.metric

import pro.vlprojects.batcher.buffer.accept.BufferBackPressurePolicy
import pro.vlprojects.batcher.buffer.dispatch.DispatchPolicy
import pro.vlprojects.batcher.queue.metric.QueueMeter
import pro.vlprojects.batcher.writer.metric.WriterMeter
import java.time.Duration

/**
 * Separate from QueueMeter and WriterMeter, though one class may record all three. Every method does nothing
 * by default, so a new event in a later version breaks no meter written against this one.
 */
public interface BufferMeter {

    /** One method for both results, so their ratio needs no join across series. */
    public fun incrementRecordAccepting(records: Int, result: AcceptanceResult) {}

    /** Functions and not numbers: a meter reads them on every scrape for as long as the buffer lives. */
    public fun watchBuffer(records: () -> Int, oldestRecordWait: () -> Duration) {}

    /** [policy] is the one met; its name is the tag. */
    public fun incrementBatchDispatching(policy: DispatchPolicy<*>) {}

    /** What the buffer still held when it closed, dispatched with no policy met. */
    public fun incrementBatchDispatchingOnClose() {}

    /** [records] turned away once [policy] required back pressure; its name is the tag. */
    public fun incrementBackPressureApplying(records: Int, policy: BufferBackPressurePolicy<*>) {}

    /** Every scheduled check of [policy], not only the failures: the sum is the whole work of the schedule. */
    public fun incrementPolicyChecked(policy: DispatchPolicy<*>, status: PolicyCheckStatus) {}
}
