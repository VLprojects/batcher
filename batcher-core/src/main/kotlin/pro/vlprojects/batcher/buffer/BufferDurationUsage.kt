package pro.vlprojects.batcher.buffer

import java.time.Duration
import java.time.Instant

public interface BufferDurationUsage : BufferUsage {

    /**
     * How long the first record still collecting has waited by [now], zero while empty. [now] is the
     * buffer's own clock, so every policy asked at once sees the same moment.
     */
    public fun duration(now: Instant): Duration
}
