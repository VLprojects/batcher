package pro.vlprojects.batcher.buffer.policy

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.buffer.usage.BufferCapacityUsage
import pro.vlprojects.batcher.buffer.usage.BufferDurationUsage
import java.time.Duration

class BufferingPolicyTests {

    @Test
    fun `the capacity policy dispatches on the record that fills it`() {
        val policy = BufferCapacityPolicy(capacity = 2)

        assertFalse(policy.shouldDispatch(capacityUsage(recordCount = 1)))
        assertTrue(policy.shouldDispatch(capacityUsage(recordCount = 2)))
    }

    @Test
    fun `the duration policy dispatches once the first record has waited the max duration`() {
        val policy = BufferDurationPolicy(maxDuration = Duration.ofSeconds(1))

        assertFalse(policy.shouldDispatch(durationUsage(duration = Duration.ofMillis(999))))
        assertTrue(policy.shouldDispatch(durationUsage(duration = Duration.ofSeconds(1))))
    }

    private fun capacityUsage(recordCount: Int): BufferCapacityUsage =
        object : BufferCapacityUsage {
            override fun countRecords(): Int = recordCount
        }

    private fun durationUsage(duration: Duration): BufferDurationUsage =
        object : BufferDurationUsage {
            override fun measureDuration(): Duration = duration
        }
}
