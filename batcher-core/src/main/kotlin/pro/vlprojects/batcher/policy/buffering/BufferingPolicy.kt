package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferUsage
import java.time.Instant

/**
 * One rule for when what a buffer collects goes as a batch. A buffer holds several, and the first one
 * met dispatches: a new rule, a byte budget say, is a policy over a usage of its own and not a buffer.
 */
public interface BufferingPolicy<U : BufferUsage> {

    /** The usage it reads, for the buffer to check once when built rather than on every record. */
    public val usageType: Class<U>

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /** Asked after every add and at every [nextCheckAt], with records collecting. */
    public fun isMet(usage: U, now: Instant): Boolean

    /** When the buffer must ask again with no new record, or null when only an add can meet the rule. */
    public fun nextCheckAt(usage: U, now: Instant): Instant? = null
}
