package pro.vlprojects.batcher.listener

import pro.vlprojects.batcher.data.Batch
import pro.vlprojects.batcher.data.Record

/**
 * Takes a batch the batcher has dispatched; the writing queue is one. A throw from any leaves the
 * batch with the batcher, and all of them get it again: a listener has to bear a repeat, which it can
 * tell by [Batch.id]. May block for as long as back pressure says.
 */
public fun interface BatchDispatchedListener<in R : Record<*>> {

    public fun handle(batch: Batch<R>)
}
