package pro.vlprojects.batcher.policy.buffering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.batcher.buffer.BufferFill
import java.time.Duration
import java.time.Instant

class BufferingPolicyTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `the capacity is met by the record that fills it`() {
        val policy = CapacityBufferingPolicy(capacity = 2)

        assertFalse(policy.isMet(fill(size = 1), at))
        assertTrue(policy.isMet(fill(size = 2), at))
        assertNull(policy.nextCheckAt(fill(size = 1)))
    }

    @Test
    fun `the max age is met once the first record has waited it`() {
        val policy = MaxAgeBufferingPolicy(maxAge = Duration.ofSeconds(1))

        assertFalse(policy.isMet(fill(size = 1), at.plusMillis(999)))
        assertTrue(policy.isMet(fill(size = 1), at.plusSeconds(1)))
        assertEquals(at.plusSeconds(1), policy.nextCheckAt(fill(size = 1)))
    }

    private fun fill(size: Int): BufferFill =
        object : BufferFill {
            override val size = size
            override val firstRecordAt = at
        }
}
