package pro.vlprojects.batcher.dto

import java.time.Instant

/**
 * What the batcher holds for one accepted record: [data] as the service gave it, wrapped and not
 * required to implement anything, plus what the batcher itself knows of it. Where it came from, a
 * queue to put it back to say, is the service's to keep in [data].
 */
public class AcceptedRecord<out D : Any>(
    public val data: D,
    /** Stamped by the batcher's clock on accept and kept as is by a durable stage that restores it. */
    public val acceptedAt: Instant,
) {

    override fun equals(other: Any?): Boolean =
        other is AcceptedRecord<*> && data == other.data && acceptedAt == other.acceptedAt

    override fun hashCode(): Int = 31 * data.hashCode() + acceptedAt.hashCode()

    /** No data: it may hold what must not reach a log. */
    override fun toString(): String = "AcceptedRecord[acceptedAt: $acceptedAt]"
}
