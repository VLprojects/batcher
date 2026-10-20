package pro.vlprojects.batcher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
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
    fun `a batch keeps its own copy of the records`() {
        val records = mutableListOf("record-1")
        val batch = Batch("id", records, at)

        records += "record-2"

        assertEquals(listOf("record-1"), batch.records)
    }

    @Test
    fun `every new batch gets an id of its own`() {
        assertNotEquals(Batch.of(listOf("record"), at).id, Batch.of(listOf("record"), at).id)
    }

    @Test
    fun `a slice derives its id from the parent, so a redelivered batch slices the same way`() {
        val batch = Batch("parent", listOf("a", "b", "c", "d"), at)

        val slice = batch.slice(1, 3)

        assertEquals("parent/1-3", slice.id)
        assertEquals(listOf("b", "c"), slice.records)
        assertEquals(at, slice.firstRecordAt, "The wait is still that of the oldest record of the batch")
        assertEquals(slice, Batch("parent", listOf("a", "b", "c", "d"), at).slice(1, 3))
    }

    @Test
    fun `a slice of the whole batch is the batch itself`() {
        val batch = Batch("parent", listOf("a", "b"), at)

        assertSame(batch, batch.slice(0, 2))
    }

    @Test
    fun `the log line of a batch carries no records`() {
        val batch = Batch("id", listOf("secret"), at)

        assertFalse("secret" in batch.toString())
    }
}
