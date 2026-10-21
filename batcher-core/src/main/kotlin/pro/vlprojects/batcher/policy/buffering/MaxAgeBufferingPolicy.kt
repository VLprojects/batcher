package pro.vlprojects.batcher.policy.buffering

import java.time.Duration
import java.time.Instant

/** Met once the first record has waited [maxAge]: a half-full batch does not wait forever. */
public class MaxAgeBufferingPolicy(public val maxAge: Duration) : BufferingPolicy<Any> {

    override val name: String = "max_age"

    init {
        require(maxAge.isPositive) { "A max age must be positive: $maxAge" }
    }

    override fun isMet(records: List<Any>, firstRecordAt: Instant, now: Instant): Boolean =
        !now.isBefore(nextCheckAt(firstRecordAt))

    override fun nextCheckAt(firstRecordAt: Instant): Instant = firstRecordAt.plus(maxAge)

    override fun toString(): String = "MaxAgeBufferingPolicy[maxAge: $maxAge]"
}
