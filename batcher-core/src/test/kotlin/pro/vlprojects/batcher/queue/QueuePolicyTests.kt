package pro.vlprojects.batcher.queue

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.queue.enqueue.CapacityWritingBackPressurePolicy
import pro.vlprojects.batcher.queue.usage.BatchCountMeasurable
import java.time.Duration

class QueuePolicyTests {

    @Test
    fun `the capacity back-pressure policy pushes back once the queue is full`() {
        val policy = CapacityWritingBackPressurePolicy(capacity = 2, enqueueTimeout = Duration.ZERO)

        assertFalse(policy.requiresBackPressure(countingQueue(batchCount = 1)))
        assertTrue(policy.requiresBackPressure(countingQueue(batchCount = 2)))
    }

    private fun countingQueue(batchCount: Int): BatchCountMeasurable =
        object : BatchCountMeasurable {
            override fun countBatches(): Int = batchCount
        }
}
