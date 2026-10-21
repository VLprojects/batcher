package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferFill
import java.time.Instant

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class CapacityBufferingPolicy(public val capacity: Int) : BufferingPolicy<BufferFill> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun isMet(fill: BufferFill, now: Instant): Boolean = fill.size >= capacity

    override fun toString(): String = "CapacityBufferingPolicy[capacity: $capacity]"
}
