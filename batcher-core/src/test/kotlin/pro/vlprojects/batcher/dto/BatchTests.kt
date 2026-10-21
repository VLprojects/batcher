package pro.vlprojects.batcher.dto

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class BatchTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `a batch holds at least one record`() {
        assertThrows<IllegalArgumentException> { Batch(records = emptyList<String>(), firstRecordAt = at) }
    }

    @Test
    fun `every new batch gets an id of its own`() {
        val first = Batch(records = listOf("record"), firstRecordAt = at)
        val second = Batch(records = listOf("record"), firstRecordAt = at)

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `the log line of a batch carries no records`() {
        val batch = Batch(records = listOf("secret"), firstRecordAt = at)

        assertFalse("secret" in batch.toString())
    }
}
