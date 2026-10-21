package pro.vlprojects.batcher.writer

import pro.vlprojects.batcher.dto.Batch
import pro.vlprojects.batcher.dto.Record

/**
 * The insert itself, and the only part a service has to write: one statement per call, a throw if
 * it did not land.
 */
public interface BatchWriter<in R : Record<*>> {

    public fun persist(batch: Batch<R>)

    /** The recovery path: a failed batch comes back here record by record. */
    public fun persist(record: R)
}
