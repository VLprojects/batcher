package pro.vlprojects.batcher.queue.policy

import pro.vlprojects.batcher.queue.WritingBatchQueue
import java.time.Duration

/**
 * Rejects records while the queue is full, so the source redelivers them. A batch dispatched just
 * before the queue filled up still goes to the enqueuing fallback.
 */
public class RejectingWritingBackPressurePolicy : WritingBackPressurePolicy {

    override val enqueueTimeout: Duration = Duration.ZERO

    override fun admits(queue: WritingBatchQueue<*>): Boolean = !queue.isFull

    override fun toString(): String = "RejectingWritingBackPressurePolicy[]"
}
