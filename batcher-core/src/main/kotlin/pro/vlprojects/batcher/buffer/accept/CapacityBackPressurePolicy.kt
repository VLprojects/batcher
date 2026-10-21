package pro.vlprojects.batcher.buffer.accept

import pro.vlprojects.batcher.buffer.usage.CapacityMeasurable

/** Requires back pressure once the buffer holds [capacity] records: the memory it takes stays bounded. */
public class CapacityBackPressurePolicy(
    public val capacity: Int,
) : BufferBackPressurePolicy<CapacityMeasurable> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun requiresBackPressure(buffer: CapacityMeasurable): Boolean = buffer.countRecords() >= capacity

    override fun toString(): String = "CapacityBackPressurePolicy[capacity: $capacity]"
}
