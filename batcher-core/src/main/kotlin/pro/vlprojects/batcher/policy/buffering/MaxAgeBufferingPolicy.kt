package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferFill
import java.time.Duration
import java.time.Instant

/** Met once the first record has waited [maxAge]: a half-full batch does not wait forever. */
public class MaxAgeBufferingPolicy(public val maxAge: Duration) : BufferingPolicy<BufferFill> {

    override val name: String = "max_age"

    init {
        require(maxAge.isPositive) { "A max age must be positive: $maxAge" }
    }

    override fun isMet(fill: BufferFill, now: Instant): Boolean = !now.isBefore(nextCheckAt(fill))

    override fun nextCheckAt(fill: BufferFill): Instant = fill.firstRecordAt.plus(maxAge)

    override fun toString(): String = "MaxAgeBufferingPolicy[maxAge: $maxAge]"
}
