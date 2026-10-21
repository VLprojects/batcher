package pro.vlprojects.batcher.queue.enqueue

import pro.vlprojects.batcher.queue.usage.BatchCountMeasurable
import java.time.Duration

/** Requires back pressure once the queue holds [capacity] batches: the batches in flight stay bounded. */
public class CapacityWritingBackPressurePolicy(
    public val capacity: Int,
    override val enqueueTimeout: Duration,
) : WritingBackPressurePolicy<BatchCountMeasurable> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
        require(!enqueueTimeout.isNegative) { "An enqueue timeout must not be negative: $enqueueTimeout" }
    }

    override fun requiresBackPressure(queue: BatchCountMeasurable): Boolean = queue.countBatches() >= capacity

    override fun toString(): String =
        "CapacityWritingBackPressurePolicy[capacity: $capacity, enqueueTimeout: $enqueueTimeout]"
}
