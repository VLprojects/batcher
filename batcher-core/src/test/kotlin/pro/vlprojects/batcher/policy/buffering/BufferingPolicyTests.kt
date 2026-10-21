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

        assertFalse(policy.isMet(capacityUsage(recordCount = 1), at))
        assertTrue(policy.isMet(capacityUsage(recordCount = 2), at))
        assertNull(policy.nextCheckAt(capacityUsage(recordCount = 1), at))
    }

    @Test
    fun `the max duration is met once the first record has waited it`() {
        val policy = MaxDurationBufferingPolicy(maxDuration = Duration.ofSeconds(1))
        val usage = durationUsage(firstRecordAt = at)

        assertFalse(policy.isMet(usage, at.plusMillis(999)))
        assertTrue(policy.isMet(usage, at.plusSeconds(1)))
        assertEquals(at.plusSeconds(1), policy.nextCheckAt(usage, at.plusMillis(400)))
    }

    private fun capacityUsage(recordCount: Int): BufferCapacityUsage =
        object : BufferCapacityUsage {
            override fun countRecords(): Int = recordCount
        }

    private fun durationUsage(firstRecordAt: Instant): BufferDurationUsage =
        object : BufferDurationUsage {
            override fun measureDuration(now: Instant): Duration = Duration.between(firstRecordAt, now)
        }
}
