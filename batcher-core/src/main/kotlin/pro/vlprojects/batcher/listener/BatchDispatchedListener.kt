package pro.vlprojects.batcher.listener

import pro.vlprojects.batcher.dto.Batch
import pro.vlprojects.batcher.dto.Record

/**
 * Takes a batch the buffer has dispatched. The buffer lets go of the records only once this returns; a
 * throw leaves them with the buffer. May block for as long as back pressure says.
 */
public fun interface BatchDispatchedListener<in R : Record<*>> {

    public fun handle(batch: Batch<R>)
}
