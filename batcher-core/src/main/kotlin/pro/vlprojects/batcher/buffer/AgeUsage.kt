package pro.vlprojects.batcher.buffer

import java.time.Instant

public interface AgeUsage : BufferUsage {

    /** When the first record still collecting arrived, as the dispatched batch will carry it; null while empty. */
    public val firstRecordAt: Instant?
}
