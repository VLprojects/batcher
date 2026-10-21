package pro.vlprojects.batcher.queue.enqueue

import pro.vlprojects.batcher.queue.WritingBatchQueue
import java.time.Duration

/**
 * What a full writing queue means. It acts at two points: before a record goes into the buffer,
 * where the source can still keep it, and when a dispatched batch is enqueued, where it cannot, since
 * its records belong to callers that have returned.
 */
public interface WritingBackPressurePolicy {

    /** The `component` tag of the policy's metrics: the stage it guards. */
    public val component: String
        get() = "writing_queue"

    /** The `action` tag of the policy's metrics: what it decides. */
    public val action: String
        get() = "back_pressure"

    /** How long enqueue waits for room; then the batch goes to the enqueuing fallback as `QUEUE_OVERFLOW`. */
    public val enqueueTimeout: Duration

    /** Asked before the record goes into the buffer: false rejects it, and the source keeps it. */
    public fun admits(queue: WritingBatchQueue<*>): Boolean = true
}
