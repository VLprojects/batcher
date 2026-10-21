package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.BufferCapacityUsage
import java.time.Duration

/**
 * Met once the buffer holds [capacity] records: a full batch goes at once. In memory only an add
 * fills it, and the check on [checkInterval] is a backstop; a buffer shared by several instances also
 * fills with the records of the others, and that check is what dispatches them.
 */
public class BufferCapacityPolicy(
    public val capacity: Int,
    override val checkInterval: Duration = Duration.ofSeconds(1),
) : BufferingPolicy<BufferCapacityUsage> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
        require(checkInterval.isPositive) { "A check interval must be positive: $checkInterval" }
    }

    override fun shouldDispatch(usage: BufferCapacityUsage): Boolean = usage.countRecords() >= capacity

    override fun toString(): String = "BufferCapacityPolicy[capacity: $capacity, checkInterval: $checkInterval]"
}
