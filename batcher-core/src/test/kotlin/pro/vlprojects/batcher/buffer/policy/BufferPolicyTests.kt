package pro.vlprojects.batcher.buffer.policy

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.buffer.usage.CapacityMeasurable
import pro.vlprojects.batcher.buffer.usage.DurationMeasurable
import java.time.Duration

class BufferPolicyTests {

    @Test
    fun `the capacity policy dispatches on the record that fills it`() {
        val policy = CapacityDispatchPolicy(capacity = 2, checkInterval = Duration.ofSeconds(1))

        assertFalse(policy.requiresDispatch(capacityBuffer(recordCount = 1)))
        assertTrue(policy.requiresDispatch(capacityBuffer(recordCount = 2)))
    }

    @Test
    fun `the duration policy dispatches once the first record has waited the max duration`() {
        val policy = DurationDispatchPolicy(maxDuration = Duration.ofSeconds(1), checkInterval = Duration.ofMillis(100))

        assertFalse(policy.requiresDispatch(durationBuffer(duration = Duration.ofMillis(999))))
        assertTrue(policy.requiresDispatch(durationBuffer(duration = Duration.ofSeconds(1))))
    }

    @Test
    fun `the capacity back-pressure policy pushes back once the buffer is full`() {
        val policy = CapacityBackPressurePolicy(maxRecords = 2)

        assertFalse(policy.requiresBackPressure(capacityBuffer(recordCount = 1)))
        assertTrue(policy.requiresBackPressure(capacityBuffer(recordCount = 2)))
    }

    private fun capacityBuffer(recordCount: Int): CapacityMeasurable =
        object : CapacityMeasurable {
            override fun countRecords(): Int = recordCount
        }

    private fun durationBuffer(duration: Duration): DurationMeasurable =
        object : DurationMeasurable {
            override fun measureDuration(): Duration = duration
        }
}
