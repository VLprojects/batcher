package pro.vlprojects.batcher.dto

import java.time.Instant

/**
 * One accepted record: the service's own type, holding [payload] as it came and whatever else the
 * service needs back, where the record came from say.
 */
public interface Record<out T : Any> {

    public val payload: T

    /** Kept as is by a durable stage that restores the record: the wait is measured from it. */
    public val acceptedAt: Instant
}
