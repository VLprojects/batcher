package pro.vlprojects.batcher.policy.buffering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.buffer.BufferCapacityUsage
import pro.vlprojects.batcher.buffer.BufferDurationUsage
import java.time.Duration
import java.time.Instant

class BufferingPolicyTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `the capacity is met by the record that fills it`() {
        val policy = CapacityBufferingPolicy(capacity = 2)

        assertFalse(policy.isMet(capacityUsage(size = 1), at))
        assertTrue(policy.isMet(capacityUsage(size = 2), at))
        assertNull(policy.nextCheckAt(capacityUsage(size = 1)))
    }

    @Test
    fun `the max duration is met once the first record has waited it`() {
        val policy = MaxDurationBufferingPolicy(maxDuration = Duration.ofSeconds(1))

        assertFalse(policy.isMet(durationUsage(firstRecordAt = at), at.plusMillis(999)))
        assertTrue(policy.isMet(durationUsage(firstRecordAt = at), at.plusSeconds(1)))
        assertEquals(at.plusSeconds(1), policy.nextCheckAt(durationUsage(firstRecordAt = at)))
    }

    @Test
    fun `an empty buffer never meets the max duration`() {
        val policy = MaxDurationBufferingPolicy(maxDuration = Duration.ofSeconds(1))

        assertFalse(policy.isMet(durationUsage(firstRecordAt = null), at.plusSeconds(10)))
        assertNull(policy.nextCheckAt(durationUsage(firstRecordAt = null)))
    }

    private fun capacityUsage(size: Int): BufferCapacityUsage =
        object : BufferCapacityUsage {
            override val size = size
        }

    private fun durationUsage(firstRecordAt: Instant?): BufferDurationUsage =
        object : BufferDurationUsage {
            override val firstRecordAt = firstRecordAt
        }
}
