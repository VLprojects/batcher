package pro.vlprojects.batcher.queue.policy

import pro.vlprojects.batcher.queue.WritingBatchQueue
import java.time.Duration

/**
 * What a full writing queue means. It acts at two points: before a record goes into the buffer,
 * where the source can still keep it, and when a dispatched batch is enqueued, where it cannot, since
 * its records belong to callers that have returned.
 */
public interface WritingBackPressurePolicy {

    /** How long enqueue waits for room; then the batch goes to the fallback as `QUEUE_OVERFLOW`. */
    public val enqueueTimeout: Duration

    /** Asked before the record goes into the buffer: false rejects it, and the source keeps it. */
    public fun admits(queue: WritingBatchQueue<*>): Boolean = true
}
