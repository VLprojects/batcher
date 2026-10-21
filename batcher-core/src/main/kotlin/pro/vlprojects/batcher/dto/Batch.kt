package pro.vlprojects.batcher.dto

import java.util.UUID

/**
 * Records in the order they were accepted, so the first is the oldest. [id] survives redelivery from
 * a durable queue, so a writer can hand it to the storage as a deduplication token.
 *
 * [records] is taken as is, without a copy: whoever dispatches the batch must not touch the list again.
 */
public class Batch<out R : Record<*>>(
    /** Given explicitly only by a durable stage that restores a batch it already held. */
    public val id: UUID = UUID.randomUUID(),
    public val records: List<R>,
) {

    public val size: Int
        get() = records.size

    init {
        require(records.isNotEmpty()) { "A batch must hold at least one record" }
    }

    override fun equals(other: Any?): Boolean = other is Batch<*> && id == other.id && records == other.records

    override fun hashCode(): Int = id.hashCode()

    /** No records: a batch of thousands would flood every log line it ends up in. */
    override fun toString(): String = "Batch[id: $id, size: $size]"
}
