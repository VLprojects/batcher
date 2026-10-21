package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.AgeUsage
import java.time.Duration
import java.time.Instant

/** Met once the first record has waited [maxAge]: a half-full batch does not wait forever. */
public class MaxAgeBufferingPolicy(public val maxAge: Duration) : BufferingPolicy<AgeUsage> {

    override val usageType: Class<AgeUsage> = AgeUsage::class.java

    override val name: String = "max_age"

    init {
        require(maxAge.isPositive) { "A max age must be positive: $maxAge" }
    }

    override fun isMet(usage: AgeUsage, now: Instant): Boolean = nextCheckAt(usage)?.let { !now.isBefore(it) } ?: false

    override fun nextCheckAt(usage: AgeUsage): Instant? = usage.firstRecordAt?.plus(maxAge)

    override fun toString(): String = "MaxAgeBufferingPolicy[maxAge: $maxAge]"
}
