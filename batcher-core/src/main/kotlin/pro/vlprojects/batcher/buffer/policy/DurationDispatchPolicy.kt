package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.DurationMeasurable
import java.time.Duration

/** Met once the first record has waited [maxDuration]: a half-full batch does not wait forever. */
public class DurationDispatchPolicy(
    public val maxDuration: Duration,
    override val checkInterval: Duration,
) : DispatchPolicy<DurationMeasurable> {

    override val name: String = "duration"

    init {
        require(maxDuration.isPositive) { "A max duration must be positive: $maxDuration" }
        require(checkInterval >= Duration.ofMillis(1)) {
            "A check interval must be at least a millisecond: $checkInterval"
        }
    }

    override fun shouldDispatch(buffer: DurationMeasurable): Boolean = buffer.measureDuration() >= maxDuration

    override fun toString(): String =
        "DurationDispatchPolicy[maxDuration: $maxDuration, checkInterval: $checkInterval]"
}
