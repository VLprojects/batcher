package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferUsage
import java.time.Instant

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class CapacityBufferingPolicy(public val capacity: Int) : BufferingPolicy<BufferUsage> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun isMet(usage: BufferUsage, now: Instant): Boolean = usage.size >= capacity

    override fun toString(): String = "CapacityBufferingPolicy[capacity: $capacity]"
}
