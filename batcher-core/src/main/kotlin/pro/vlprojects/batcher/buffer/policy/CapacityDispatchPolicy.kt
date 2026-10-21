package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.CapacityMeasurable

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class CapacityDispatchPolicy(
    public val capacity: Int,
) : DispatchPolicy<CapacityMeasurable> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun shouldDispatch(buffer: CapacityMeasurable): Boolean = buffer.countRecords() >= capacity

    override fun toString(): String = "CapacityDispatchPolicy[capacity: $capacity]"
}
