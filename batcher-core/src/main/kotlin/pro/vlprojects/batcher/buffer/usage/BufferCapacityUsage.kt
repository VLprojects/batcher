package pro.vlprojects.batcher.buffer.usage

public interface BufferCapacityUsage : BufferUsage {

    /** Records, not bytes. Read by the gauges on every scrape too: has to be cheap, may be approximate. */
    public fun countRecords(): Int
}
