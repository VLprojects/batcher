package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable
import java.time.Duration

/**
 * One rule for when what a buffer collects goes as a batch. A buffer holds several and asks each in
 * turn: a new rule, a byte budget say, is a policy over a usage of its own and not a buffer.
 * [U] is `in`, so a policy over a usage is a policy over any buffer that reports it, and the compiler
 * turns down the rest.
 */
public interface DispatchPolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /**
     * How often the buffer asks with no new record, at least a millisecond. A policy due by time is met
     * only then, so a batch goes at most one interval late.
     */
    public val checkInterval: Duration

    /** Asked after every add and every [checkInterval], with records collecting. */
    public fun shouldDispatch(buffer: U): Boolean
}
