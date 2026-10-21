package pro.vlprojects.batcher.writer

import pro.vlprojects.batcher.dto.AcceptedRecord
import pro.vlprojects.batcher.dto.Batch

/**
 * The insert itself, and the only part a service has to write: one statement per call, a throw if
 * it did not land.
 */
public interface BatchWriter<in D : Any> {

    public fun persist(batch: Batch<D>)

    /** The recovery path: a failed batch comes back here record by record. */
    public fun persist(record: AcceptedRecord<D>)
}
