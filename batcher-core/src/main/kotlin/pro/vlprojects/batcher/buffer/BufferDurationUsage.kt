package pro.vlprojects.batcher.buffer

import java.time.Duration
import java.time.Instant

public interface BufferDurationUsage : BufferUsage {

    /**
     * How long the first record still collecting has waited by [now], zero while empty. A function and
     * not a property: it grows with the clock, and the buffer passes its own.
     */
    public fun duration(now: Instant): Duration
}
