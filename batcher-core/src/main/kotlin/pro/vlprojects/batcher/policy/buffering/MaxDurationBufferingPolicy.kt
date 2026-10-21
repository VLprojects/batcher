package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferDurationUsage
import java.time.Duration
import java.time.Instant

/** Met once the first record has waited [maxDuration]: a half-full batch does not wait forever. */
public class MaxDurationBufferingPolicy(public val maxDuration: Duration) : BufferingPolicy<BufferDurationUsage> {

    override val usageType: Class<BufferDurationUsage> = BufferDurationUsage::class.java

    override val name: String = "max_duration"

    init {
        require(maxDuration.isPositive) { "A max duration must be positive: $maxDuration" }
    }

    override fun isMet(usage: BufferDurationUsage, now: Instant): Boolean = usage.duration(now) >= maxDuration

    override fun nextCheckAt(usage: BufferDurationUsage, now: Instant): Instant = now.plus(
        maxDuration.minus(usage.duration(now)),
    )

    override fun toString(): String = "MaxDurationBufferingPolicy[maxDuration: $maxDuration]"
}
