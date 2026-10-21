package pro.vlprojects.batcher.policy

import java.time.Duration

/**
 * When the records a buffer collects go as a batch. A byte budget is a policy and not a buffer of its
 * own, and the metrics keep their tags: the reason is `CAPACITY_REACHED` whatever the capacity counts.
 */
public interface BufferingPolicy<in T : Any> {

    /** In the units of [weigh]. A batch is dispatched once the records weigh this much. */
    public val capacity: Int

    /** The longest the first record waits: a batch is dispatched once it is up, however light. */
    public val maxAge: Duration

    /** A record's share of [capacity]: one by default, its encoded size for a byte budget. */
    public fun weigh(record: T): Int = 1
}
