package pro.vlprojects.batcher.dto

import java.time.Instant

/**
 * One accepted record: [payload] as the service gave it, wrapped rather than made to implement
 * anything. Open so a service can carry more of its own, where a record came from say, in a subtype.
 */
public open class Record<out T : Any>(
    public val payload: T,
    /** Kept as is by a durable stage that restores the record: the wait is measured from it. */
    public val acceptedAt: Instant = Instant.now(),
) {

    /** Same class only: a subtype with fields of its own must not equal its base. */
    override fun equals(other: Any?): Boolean =
        other != null &&
            other.javaClass == javaClass &&
            payload == (other as Record<*>).payload &&
            acceptedAt == other.acceptedAt

    override fun hashCode(): Int = 31 * payload.hashCode() + acceptedAt.hashCode()

    /** No payload: it may hold what must not reach a log. */
    override fun toString(): String = "Record[acceptedAt: $acceptedAt]"
}
