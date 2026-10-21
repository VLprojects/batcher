package pro.vlprojects.batcher.metric

/**
 * Separate from [BufferMeter] and [WriterMeter], though one class may record all three. Every method
 * does nothing by default, so a new event in a later version breaks no meter written against this one.
 */
public interface BatcherMeter {

    /** One method for both results, so their ratio needs no join across series. */
    public fun incrementRecordAccepting(records: Int, result: AcceptanceResult) {}
}
