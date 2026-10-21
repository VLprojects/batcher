package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.BufferDurationUsage
import java.time.Duration

/** Met once the first record has waited [maxDuration]: a half-full batch does not wait forever. */
public class BufferDurationPolicy(public val maxDuration: Duration) : BufferingPolicy<BufferDurationUsage> {

    override val usageType: Class<BufferDurationUsage> = BufferDurationUsage::class.java

    override val name: String = "max_duration"

    init {
        require(maxDuration.isPositive) { "A max duration must be positive: $maxDuration" }
    }

    override fun shouldDispatch(usage: BufferDurationUsage): Boolean = usage.measureDuration() >= maxDuration

    override fun toString(): String = "BufferDurationPolicy[maxDuration: $maxDuration]"
}
