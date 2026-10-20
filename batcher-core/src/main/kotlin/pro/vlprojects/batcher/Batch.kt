package pro.vlprojects.batcher

import java.time.Instant
import java.util.UUID

/**
 * Records in arrival order. [id] survives redelivery from a durable queue, so a writer can hand it
 * to the storage as a deduplication token; a slice derives its id from its parent's.
 */
public class Batch<out T : Any>(
    public val id: String,
    records: List<T>,
    /** The oldest record's arrival, not when the batch was cut: it is that wait that matters. */
    public val firstRecordAt: Instant,
) {

    public val records: List<T> = records.toList()

    public val size: Int
        get() = records.size

    init {
        require(id.isNotBlank()) { "A batch id must not be blank" }
        require(this.records.isNotEmpty()) { "A batch must hold at least one record" }
    }

    /** Records [fromIndex] until [toIndex]: how a failed batch is written again in parts. */
    public fun slice(fromIndex: Int, toIndex: Int): Batch<T> {
        if (fromIndex == 0 && toIndex == size) {
            return this
        }

        return Batch("$id/$fromIndex-$toIndex", records.subList(fromIndex, toIndex), firstRecordAt)
    }

    override fun equals(other: Any?): Boolean =
        other is Batch<*> && id == other.id && firstRecordAt == other.firstRecordAt && records == other.records

    override fun hashCode(): Int = id.hashCode()

    /** No records: a batch of thousands would flood every log line it ends up in. */
    override fun toString(): String = "Batch(id=$id, size=$size, firstRecordAt=$firstRecordAt)"

    public companion object {

        @JvmStatic
        public fun <T : Any> of(records: List<T>, firstRecordAt: Instant): Batch<T> =
            Batch(UUID.randomUUID().toString(), records, firstRecordAt)
    }
}
