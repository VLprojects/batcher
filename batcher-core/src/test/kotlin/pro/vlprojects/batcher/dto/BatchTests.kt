package pro.vlprojects.batcher.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class BatchTests {

    private val at = Instant.parse("2026-10-21T10:00:00Z")

    @Test
    fun `a batch holds at least one record`() {
        assertThrows<IllegalArgumentException> { Batch(records = emptyList<Record<String>>()) }
    }

    @Test
    fun `the first record of a batch is its oldest`() {
        val batch = Batch(records = listOf(Record("first", at), Record("second", at.plusSeconds(1))))

        assertEquals(at, batch.firstRecordAt)
    }

    @Test
    fun `every new batch gets an id of its own`() {
        val first = Batch(records = listOf(Record("record", at)))
        val second = Batch(records = listOf(Record("record", at)))

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `the log line of a batch carries no records`() {
        val batch = Batch(records = listOf(Record("secret", at)))

        assertFalse("secret" in batch.toString())
        assertFalse("secret" in batch.records.single().toString())
    }
}
