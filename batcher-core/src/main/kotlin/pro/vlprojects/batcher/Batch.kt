package pro.vlprojects.batcher

import java.time.Instant
import java.util.UUID

/**
 * Records in arrival order. [id] survives redelivery from a durable queue, so a writer can hand it
 * to the storage as a deduplication token.
 *
 * [records] is taken as is, without a copy: whoever cuts the batch must not touch the list again.
 */
public class Batch<out T : Any>(
    public val records: List<T>,
    /** The oldest record's arrival, not when the batch was cut: it is that wait that matters. */
    public val firstRecordAt: Instant,
    /** Given explicitly only by a durable stage that restores a batch it already held. */
    public val id: String,
) {

    public constructor(records: List<T>, firstRecordAt: Instant) :
        this(records, firstRecordAt, UUID.randomUUID().toString())

    public val size: Int
        get() = records.size

    init {
        require(id.isNotBlank()) { "A batch id must not be blank" }
        require(records.isNotEmpty()) { "A batch must hold at least one record" }
    }

    override fun equals(other: Any?): Boolean =
        other is Batch<*> && id == other.id && firstRecordAt == other.firstRecordAt && records == other.records

    override fun hashCode(): Int = id.hashCode()

    /** No records: a batch of thousands would flood every log line it ends up in. */
    override fun toString(): String = "Batch[id: $id, size: $size, firstRecordAt: $firstRecordAt]"
}
