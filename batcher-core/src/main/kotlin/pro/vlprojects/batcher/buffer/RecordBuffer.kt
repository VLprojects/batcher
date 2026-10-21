package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable
import pro.vlprojects.batcher.data.Batch
import pro.vlprojects.batcher.data.Record

/**
 * Holds accepted records until the batcher dispatches them. It reports its usage by implementing the
 * [UsageMeasurable] subtypes it can count; when to dispatch is up to the batcher and its policies, so a
 * buffer keeps no schedule and no listeners.
 */
public interface RecordBuffer<R : Record<*>> {

    /**
     * Before the first record. A durable buffer restores what it held at the last stop, batches it
     * dispatched and never had confirmed included, and the next [dispatch] returns them.
     */
    public fun open()

    /** Returns once the record is in. A throw means it is not, and it is still the caller's. */
    public fun add(record: R)

    /**
     * One by one by default; a buffer may take them in one go instead. A throw leaves the caller the
     * record it came on and those after it.
     */
    public fun add(records: Collection<R>): Unit = records.forEach(::add)

    /**
     * All the buffer holds, oldest first, in batches of at most [batchSize] records, or in one when it
     * is null; a batch per group if it groups records by a key. Empty when it holds nothing. What it
     * returns counts no more towards the usage but stays with the buffer until [confirm].
     */
    public fun dispatch(batchSize: Int?): List<Batch<R>>

    /**
     * [batch] is written or parked in the fallback: only now the buffer lets go of its records, so one
     * whose records the source acknowledges on confirm, a message broker say, loses none in between.
     * In memory there is nothing to do.
     */
    public fun confirm(batch: Batch<R>)

    /**
     * Stops taking records, once the batcher has stopped asking its policies. An in-memory buffer
     * leaves what it still holds to one last [dispatch]; a durable one keeps it, unconfirmed batches
     * included, for the next [open] and dispatches nothing more.
     */
    public fun close()
}
