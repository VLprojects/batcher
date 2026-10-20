package pro.vlprojects.batcher

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class BatchTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `a batch holds at least one record`() {
        assertThrows<IllegalArgumentException> { Batch("id", emptyList<String>(), at) }
    }

    @Test
    fun `every new batch gets an id of its own`() {
        assertNotEquals(Batch.of(listOf("record"), at).id, Batch.of(listOf("record"), at).id)
    }

    @Test
    fun `the log line of a batch carries no records`() {
        val batch = Batch("id", listOf("secret"), at)

        assertFalse("secret" in batch.toString())
    }
}
