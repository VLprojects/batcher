package pro.vlprojects.batcher.fallback

import pro.vlprojects.batcher.buffer.policy.BufferBackPressurePolicy
import pro.vlprojects.batcher.data.Record

/**
 * Decides what becomes of records a [policy] turned away before the buffer: a throw rejects them back
 * to the caller, so the source redelivers; a return means they are parked or dropped on purpose.
 */
public fun interface AcceptingFallback<in R : Record<*>> {

    public fun handle(records: Collection<R>, policy: BufferBackPressurePolicy<*>)

    public fun handle(record: R, policy: BufferBackPressurePolicy<*>): Unit = handle(listOf(record), policy)
}
