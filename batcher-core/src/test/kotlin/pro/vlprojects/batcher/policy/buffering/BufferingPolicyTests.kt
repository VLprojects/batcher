package pro.vlprojects.batcher.policy.buffering

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class BufferingPolicyTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `the capacity is met by the record that fills it`() {
        val policy = CapacityBufferingPolicy(capacity = 2)

        assertFalse(policy.isMet(listOf("a"), at, at))
        assertTrue(policy.isMet(listOf("a", "b"), at, at))
        assertNull(policy.nextCheckAt(at))
    }

    @Test
    fun `the max age is met once the first record has waited it`() {
        val policy = MaxAgeBufferingPolicy(maxAge = Duration.ofSeconds(1))

        assertFalse(policy.isMet(listOf("a"), at, at.plusMillis(999)))
        assertTrue(policy.isMet(listOf("a"), at, at.plusSeconds(1)))
        assertEquals(at.plusSeconds(1), policy.nextCheckAt(at))
    }
}
