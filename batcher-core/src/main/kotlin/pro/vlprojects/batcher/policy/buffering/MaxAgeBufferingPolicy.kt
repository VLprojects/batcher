package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferUsage
import java.time.Duration
import java.time.Instant

/** Met once the first record has waited [maxAge]: a half-full batch does not wait forever. */
public class MaxAgeBufferingPolicy(public val maxAge: Duration) : BufferingPolicy<BufferUsage> {

    override val name: String = "max_age"

    init {
        require(maxAge.isPositive) { "A max age must be positive: $maxAge" }
    }

    override fun isMet(usage: BufferUsage, now: Instant): Boolean = !now.isBefore(nextCheckAt(usage))

    override fun nextCheckAt(usage: BufferUsage): Instant = usage.firstRecordAt.plus(maxAge)

    override fun toString(): String = "MaxAgeBufferingPolicy[maxAge: $maxAge]"
}
