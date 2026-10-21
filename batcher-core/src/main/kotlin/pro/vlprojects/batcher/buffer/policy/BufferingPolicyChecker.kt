package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.RecordBuffer
import pro.vlprojects.batcher.metric.BufferMeter
import pro.vlprojects.batcher.metric.PolicyCheckStatus

/**
 * Asks a buffer's policies on a schedule, so that a policy due by time is met with no new record. How
 * often, and whether at all, is its own setting per policy: a buffer in memory needs no schedule for
 * its capacity, one shared by several instances does. It counts every check by
 * [BufferMeter.incrementPolicyChecked], a throw included, and its threads are named after the batcher,
 * so a thread dump tells whose they are.
 */
public interface BufferingPolicyChecker {

    /**
     * From [RecordBuffer.open], once per policy. [check] is the buffer's own: it asks [policy] and
     * dispatches for as long as it is met.
     */
    public fun schedule(policy: BufferingPolicy<*>, check: () -> PolicyCheckStatus)

    /** From [RecordBuffer.close], before the rest goes: waits for a check under way, and runs no more. */
    public fun stop()
}
