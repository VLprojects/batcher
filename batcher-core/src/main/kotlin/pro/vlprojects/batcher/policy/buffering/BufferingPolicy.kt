package pro.vlprojects.batcher.policy.buffering

import java.time.Instant

/**
 * One rule for when the records a buffer collects go as a batch. A buffer holds several, and the
 * first one met dispatches: a new rule, a byte budget say, is a policy and not a buffer of its own.
 */
public interface BufferingPolicy<in T : Any> {

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /** Asked after every add and at every [nextCheckAt]. [records] are never empty. */
    public fun isMet(records: List<T>, firstRecordAt: Instant, now: Instant): Boolean

    /** When the buffer must ask again with no new record, or null when only an add can meet the rule. */
    public fun nextCheckAt(firstRecordAt: Instant): Instant? = null
}
