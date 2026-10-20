package pro.vlprojects.batcher

/**
 * The insert itself, and the only part a service has to write: one statement per call, a throw if
 * it did not land. A failed batch comes back here in slices, down to single records.
 */
public fun interface BatchWriter<in T : Any> {

    public fun persist(batch: Batch<T>)
}
