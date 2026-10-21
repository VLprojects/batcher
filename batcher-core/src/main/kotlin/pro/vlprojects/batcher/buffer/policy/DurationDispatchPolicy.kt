package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.DurationMeasurable
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Met once the first record has waited [maxDuration]: a half-full batch does not wait forever. Only a
 * check every [checkInterval] meets it, so a batch goes at most one interval late.
 */
public class DurationDispatchPolicy(
    public val maxDuration: Duration,
    public val checkInterval: Duration,
) : DispatchPolicy<DurationMeasurable> {

    override val name: String = "duration"

    init {
        require(maxDuration.isPositive) { "A max duration must be positive: $maxDuration" }
        require(checkInterval >= Duration.ofMillis(1)) {
            "A check interval must be at least a millisecond: $checkInterval"
        }
    }

    override fun shouldDispatch(buffer: DurationMeasurable): Boolean = buffer.measureDuration() >= maxDuration

    /** Fixed delay: a slow check pushes the next one back instead of piling them up. */
    override fun schedule(scheduler: ScheduledExecutorService, check: Runnable) {
        val interval = checkInterval.toMillis()
        scheduler.scheduleWithFixedDelay(check, interval, interval, TimeUnit.MILLISECONDS)
    }

    override fun toString(): String =
        "DurationDispatchPolicy[maxDuration: $maxDuration, checkInterval: $checkInterval]"
}
