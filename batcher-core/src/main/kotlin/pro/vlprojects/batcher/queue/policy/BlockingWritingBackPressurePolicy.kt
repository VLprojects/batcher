package pro.vlprojects.batcher.queue.policy

import java.time.Duration

/**
 * Makes the dispatching thread wait for room. A message listener that waits stops acknowledging,
 * and the broker stops delivering.
 */
public class BlockingWritingBackPressurePolicy(override val enqueueTimeout: Duration) : WritingBackPressurePolicy {

    init {
        require(enqueueTimeout.isPositive) { "An enqueue timeout must be positive: $enqueueTimeout" }
    }

    override fun toString(): String = "BlockingWritingBackPressurePolicy[enqueueTimeout: $enqueueTimeout]"
}
