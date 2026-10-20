package pro.vlprojects.batcher.metric

import java.time.Duration

/**
 * Everything a batcher reports. Every method does nothing by default, so a meter implements only
 * what it records, and a new event in a later version breaks no meter written against this one.
 */
public interface BatcherMeter {

    /** Called once, when the batcher starts. */
    public fun watch(gauges: BatcherGauges) {}

    public fun onRecordsAccepting(records: Int, result: AcceptanceResult) {}

    public fun onBatchDispatched(reason: BatchDispatchReason) {}

    /** Every firing, not only the failures: the sum is the whole work of the deadline thread. */
    public fun onDeadlineChecked(result: DeadlineCheckResult) {}

    /** One batch through the writer, however it had to be written; what landed is [onRecordsWritten]. */
    public fun onBatchWritten(mode: WriteMode, took: Duration) {}

    public fun onRecordsWritten(records: Int, result: RecordWriteResult) {}
}
