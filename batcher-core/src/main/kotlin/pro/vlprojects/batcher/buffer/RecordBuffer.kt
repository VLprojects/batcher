package pro.vlprojects.batcher.buffer

import pro.vlprojects.batcher.buffer.policy.BufferingPolicy
import pro.vlprojects.batcher.buffer.usage.BufferUsage
import pro.vlprojects.batcher.dto.Batch
import pro.vlprojects.batcher.listener.BatchReadyListener

/**
 * Collects records and dispatches them in batches once any of its [policies] is met. It reports its
 * usage by implementing the [BufferUsage] subtypes it can count. A policy due by time needs a clock
 * running somewhere, and that is the buffer's own, not the batcher's.
 */
public interface RecordBuffer<R : Any> : BufferUsage {

    /**
     * Set when built and asked in this order; the first one met names the batch in the metrics. A
     * buffer that is not of some policy's [usage type][BufferingPolicy.usageType] must refuse to be built.
     */
    public val policies: List<BufferingPolicy<*>>

    /**
     * From any thread, at any time; counts from the next dispatched batch. A batch goes to every listener in
     * the order they were added, and the records are let go only once all of them have returned. A
     * throw from any keeps the batch, and all of them get it again: a listener has to bear a repeat,
     * which it can tell by [Batch.id].
     */
    public fun addListener(listener: BatchReadyListener<R>)

    public fun removeListener(listener: BatchReadyListener<R>)

    /**
     * Before the first record, once the listeners are in: an in-memory buffer starts its clock, a
     * durable one also restores what it held at the last stop and dispatches it as the policies say.
     */
    public fun open()

    /**
     * Returns once the record is in. A throw means it is not, and it is still the caller's: with no
     * listener a dispatched batch has nowhere to go, so the buffer stops taking records.
     */
    public fun add(record: R)

    /**
     * One by one by default; a buffer may take them in one go instead. A throw leaves the caller the
     * record it came on and those after it.
     */
    public fun add(records: Collection<R>): Unit = records.forEach(::add)

    /**
     * Stops taking records and dispatching batches. An in-memory buffer returns what was still
     * collecting for the caller to place; a durable one keeps it for the next [open] and returns null.
     */
    public fun close(): Batch<R>?
}
