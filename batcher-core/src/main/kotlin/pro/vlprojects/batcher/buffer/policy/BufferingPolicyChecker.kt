package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.RecordBuffer
import pro.vlprojects.batcher.metric.BufferMeter

/**
 * Asks every policy of one buffer on that policy's own [check interval][BufferingPolicy.checkInterval]
 * through [RecordBuffer.dispatch], and counts every check by [BufferMeter.incrementPolicyChecked], a
 * throw included. Its threads are named after the batcher, so a thread dump tells whose they are.
 */
public interface BufferingPolicyChecker {

    /** Once the buffer is open: a check before that would dispatch to no listener. */
    public fun start()

    /** Before the buffer closes, and waits for a check under way, so none runs into a closed buffer. */
    public fun stop()
}
