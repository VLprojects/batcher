package pro.vlprojects.batcher.queue

import pro.vlprojects.batcher.Batch

/** A batch out of the queue but still the queue's to answer for, until one of these two is called. */
public interface BatchLease<out T : Any> {

    public val batch: Batch<T>

    /** Written or parked: the batch leaves the queue for good. */
    public fun complete()

    /** Not dealt with: the batch goes back, ahead of the rest. */
    public fun release()
}
