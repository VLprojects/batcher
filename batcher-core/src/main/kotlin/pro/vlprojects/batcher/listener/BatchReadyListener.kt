package pro.vlprojects.batcher.listener

import pro.vlprojects.batcher.model.Batch

/**
 * Takes a batch the buffer has cut. The buffer lets go of the records only once this returns; a
 * throw leaves them with the buffer. May block for as long as back pressure says.
 */
public fun interface BatchReadyListener<in T : Any> {

    public fun handle(batch: Batch<T>)
}
