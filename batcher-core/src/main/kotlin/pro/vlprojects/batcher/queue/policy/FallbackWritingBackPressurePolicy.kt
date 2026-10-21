package pro.vlprojects.batcher.queue.policy

import java.time.Duration

/** Hands a batch with no room to the enqueuing fallback at once: the caller never waits and never gets a throw. */
public class FallbackWritingBackPressurePolicy : WritingBackPressurePolicy {

    override val enqueueTimeout: Duration = Duration.ZERO

    override fun toString(): String = "FallbackWritingBackPressurePolicy[]"
}
