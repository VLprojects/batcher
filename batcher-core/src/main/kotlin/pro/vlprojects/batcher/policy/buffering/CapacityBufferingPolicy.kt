package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferCapacityUsage
import java.time.Instant

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class CapacityBufferingPolicy(public val capacity: Int) : BufferingPolicy<BufferCapacityUsage> {

    override val usageType: Class<BufferCapacityUsage> = BufferCapacityUsage::class.java

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun isMet(usage: BufferCapacityUsage, now: Instant): Boolean = usage.size >= capacity

    override fun toString(): String = "CapacityBufferingPolicy[capacity: $capacity]"
}
