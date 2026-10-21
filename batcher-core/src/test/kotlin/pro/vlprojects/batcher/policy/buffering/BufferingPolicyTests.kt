package pro.vlprojects.batcher.policy.buffering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.buffer.AgeUsage
import pro.vlprojects.batcher.buffer.CapacityUsage
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
    fun `the max age is met once the first record has waited it`() {
        val policy = MaxAgeBufferingPolicy(maxAge = Duration.ofSeconds(1))

        assertFalse(policy.isMet(ageUsage(firstRecordAt = at), at.plusMillis(999)))
        assertTrue(policy.isMet(ageUsage(firstRecordAt = at), at.plusSeconds(1)))
        assertEquals(at.plusSeconds(1), policy.nextCheckAt(ageUsage(firstRecordAt = at)))
    }

    @Test
    fun `an empty buffer never meets the max age`() {
        val policy = MaxAgeBufferingPolicy(maxAge = Duration.ofSeconds(1))

        assertFalse(policy.isMet(ageUsage(firstRecordAt = null), at.plusSeconds(10)))
        assertNull(policy.nextCheckAt(ageUsage(firstRecordAt = null)))
    }

    private fun capacityUsage(size: Int): CapacityUsage =
        object : CapacityUsage {
            override val size = size
        }

    private fun ageUsage(firstRecordAt: Instant?): AgeUsage =
        object : AgeUsage {
            override val firstRecordAt = firstRecordAt
        }
}
