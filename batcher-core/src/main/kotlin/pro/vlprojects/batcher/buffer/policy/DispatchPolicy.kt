package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable
import java.util.concurrent.ScheduledExecutorService

/**
 * One rule for when what a buffer collects goes as a batch. A buffer holds several and asks each in
 * turn: a new rule, a byte budget say, is a policy over a usage of its own and not a buffer.
 * [U] is `in`, so a policy over a usage is a policy over any buffer that reports it, and the compiler
 * turns down the rest.
 */
public interface DispatchPolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /** Asked after every add and on the schedule from [schedule], with records collecting. */
    public fun shouldDispatch(buffer: U): Boolean

    /**
     * From [open][pro.vlprojects.batcher.buffer.RecordBuffer.open]. A policy that can be due with no new
     * record puts [check] on [scheduler]; one that changes only on add leaves it be. The scheduler and
     * its threads are the buffer's, and so is shutting them down: the policy keeps nothing of the schedule.
     */
    public fun schedule(scheduler: ScheduledExecutorService, check: Runnable) {}
}
