package pro.vlprojects.batcher.policy.buffering

import java.time.Instant

/** Met once the buffer holds [capacity] records: a full batch goes at once. */
public class CapacityBufferingPolicy(public val capacity: Int) : BufferingPolicy<Any> {

    override val name: String = "capacity"

    init {
        require(capacity > 0) { "A capacity must be positive: $capacity" }
    }

    override fun isMet(records: List<Any>, firstRecordAt: Instant, now: Instant): Boolean = records.size >= capacity

    override fun toString(): String = "CapacityBufferingPolicy[capacity: $capacity]"
}
