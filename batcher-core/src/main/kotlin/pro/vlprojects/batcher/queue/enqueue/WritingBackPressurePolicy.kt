package pro.vlprojects.batcher.queue.enqueue

import pro.vlprojects.batcher.usage.UsageMeasurable
import java.time.Duration

/**
 * What a full writing queue means, asked before every dispatched batch is enqueued. Over one kind of
 * usage, like a dispatch policy. While it requires back pressure, the batcher waits up to
 * [enqueueTimeout]; then the batch goes to the enqueuing fallback as `QUEUE_OVERFLOW`. The source is
 * guarded by the buffer's own back-pressure policies.
 */
public interface WritingBackPressurePolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the back-pressure metric. */
    public val name: String

    /** The `component` tag of the policy's metrics: the stage it guards. */
    public val component: String
        get() = "writing_queue"

    /** The `action` tag of the policy's metrics: what it decides. */
    public val action: String
        get() = "back_pressure"

    /** How long the batcher waits for back pressure to end, zero being no wait. */
    public val enqueueTimeout: Duration

    public fun requiresBackPressure(queue: U): Boolean
}
