package pro.vlprojects.batcher.buffer.usage

import pro.vlprojects.batcher.usage.UsageMeasurable
import java.time.Duration

public interface DurationMeasurable : UsageMeasurable {

    /**
     * How long the first record still collecting has waited since it was accepted, by the clock that
     * stamped it; zero while empty.
     */
    public fun measureDuration(): Duration
}
