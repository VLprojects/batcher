package pro.vlprojects.batcher.policy.buffering

import pro.vlprojects.batcher.buffer.BufferFill
import java.time.Instant

/**
 * One rule for when what a buffer collects goes as a batch. A buffer holds several, and the first one
 * met dispatches: a new rule, a byte budget say, is a policy over a [BufferFill] subtype and not a
 * buffer of its own.
 */
public interface BufferingPolicy<in F : BufferFill> {

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /** Asked after every add and at every [nextCheckAt]. */
    public fun isMet(fill: F, now: Instant): Boolean

    /** When the buffer must ask again with no new record, or null when only an add can meet the rule. */
    public fun nextCheckAt(fill: F): Instant? = null
}
