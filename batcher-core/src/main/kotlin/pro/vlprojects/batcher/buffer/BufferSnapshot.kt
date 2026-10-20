package pro.vlprojects.batcher.buffer

import java.time.Instant

public class BufferSnapshot(
    public val records: Int,
    /** Null while the buffer is empty. */
    public val oldestRecordAt: Instant?,
) {

    override fun toString(): String = "BufferSnapshot[records: $records, oldestRecordAt: $oldestRecordAt]"

    public companion object {

        @JvmField
        public val EMPTY: BufferSnapshot = BufferSnapshot(0, null)
    }
}
