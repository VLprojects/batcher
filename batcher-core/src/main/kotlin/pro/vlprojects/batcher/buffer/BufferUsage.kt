package pro.vlprojects.batcher.buffer

import java.time.Instant

/**
 * What a buffer is collecting, as its policies see it. A buffer that tracks more, bytes say, gives a
 * subtype, and a policy that reads it works with such a buffer only.
 */
public interface BufferUsage {

    /** Records, never zero: an empty buffer has no usage. */
    public val size: Int

    /** When the first record still collecting arrived, as the dispatched batch will carry it. */
    public val firstRecordAt: Instant
}
