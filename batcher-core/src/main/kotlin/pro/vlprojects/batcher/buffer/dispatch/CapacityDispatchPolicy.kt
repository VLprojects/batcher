package pro.vlprojects.batcher.buffer.dispatch

import pro.vlprojects.batcher.buffer.usage.CapacityMeasurable
import java.time.Duration

/**
 * Met once the buffer holds [capacity] records: a full batch goes at once, on the add that fills it.
 * The check every [checkInterval] is a backstop for a buffer that others fill as well.
 */
public class CapacityDispatchPolicy(
    public val capacity: Int,
    override val checkInterval: Duration,
) : DispatchPolicy<CapacityMeasurable> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
        require(checkInterval >= Duration.ofMillis(1)) {
            "A check interval must be at least a millisecond: $checkInterval"
        }
    }

    override fun requiresDispatch(buffer: CapacityMeasurable): Boolean = buffer.countRecords() >= capacity

    override fun toString(): String = "CapacityDispatchPolicy[capacity: $capacity, checkInterval: $checkInterval]"
}
