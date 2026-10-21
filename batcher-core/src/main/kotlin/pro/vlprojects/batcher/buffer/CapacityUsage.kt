package pro.vlprojects.batcher.buffer

public interface CapacityUsage : BufferUsage {

    /** Records collecting. Read by the gauges on every scrape too: has to be cheap, may be approximate. */
    public val size: Int
}
