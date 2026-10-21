package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.BufferCapacityUsage

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class BufferCapacityPolicy(public val capacity: Int) : BufferingPolicy<BufferCapacityUsage> {

    override val usageType: Class<BufferCapacityUsage> = BufferCapacityUsage::class.java

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun shouldDispatch(usage: BufferCapacityUsage): Boolean = usage.countRecords() >= capacity

    override fun toString(): String = "BufferCapacityPolicy[capacity: $capacity]"
}
