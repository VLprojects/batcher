package pro.vlprojects.batcher.dto

import java.time.Instant

/**
 * What the batcher holds for one accepted record: [payload] as the service gave it, wrapped and not
 * required to implement anything, plus what the batcher itself knows of it. Where it came from, a
 * queue to put it back to say, is the service's to keep in [payload].
 */
public class Record<out T : Any>(
    public val payload: T,
    /** Stamped by the batcher's clock on accept and kept as is by a durable stage that restores it. */
    public val acceptedAt: Instant,
) {

    override fun equals(other: Any?): Boolean =
        other is Record<*> && payload == other.payload && acceptedAt == other.acceptedAt

    override fun hashCode(): Int = 31 * payload.hashCode() + acceptedAt.hashCode()

    /** No payload: it may hold what must not reach a log. */
    override fun toString(): String = "Record[acceptedAt: $acceptedAt]"
}
