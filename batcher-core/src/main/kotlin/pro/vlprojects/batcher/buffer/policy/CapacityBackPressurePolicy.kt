package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.CapacityMeasurable

/** Admits a record while the buffer holds fewer than [maxRecords]: the memory it takes stays bounded. */
public class CapacityBackPressurePolicy(
    public val maxRecords: Int,
    override val action: BackPressureAction,
) : BufferBackPressurePolicy<CapacityMeasurable> {

    override val name: String = "capacity"

    init {
        require(maxRecords > 0) { "A max record count must be positive: $maxRecords" }
    }

    override fun admits(buffer: CapacityMeasurable): Boolean = buffer.countRecords() < maxRecords

    override fun toString(): String = "CapacityBackPressurePolicy[maxRecords: $maxRecords, action: $action]"
}
