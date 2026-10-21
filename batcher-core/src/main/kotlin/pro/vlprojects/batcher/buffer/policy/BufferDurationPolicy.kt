package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.BufferDurationUsage
import java.time.Duration

/**
 * Met once the first record has waited [maxDuration]: a half-full batch does not wait forever. By
 * default it is asked ten times per [maxDuration], so a batch goes at most a tenth of it late.
 */
public class BufferDurationPolicy(
    public val maxDuration: Duration,
    override val checkInterval: Duration = maxDuration.dividedBy(10),
) : BufferingPolicy<BufferDurationUsage> {

    override val name: String = "max_duration"

    init {
        require(maxDuration.isPositive) { "A max duration must be positive: $maxDuration" }
        require(checkInterval.isPositive) { "A check interval must be positive: $checkInterval" }
    }

    override fun shouldDispatch(usage: BufferDurationUsage): Boolean = usage.measureDuration() >= maxDuration

    override fun toString(): String = "BufferDurationPolicy[maxDuration: $maxDuration, checkInterval: $checkInterval]"
}
