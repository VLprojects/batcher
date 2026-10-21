package pro.vlprojects.batcher.buffer.usage

import pro.vlprojects.batcher.usage.UsageMeasurable

public interface CapacityMeasurable : UsageMeasurable {

    /** Records, not bytes. Read by the gauges on every scrape too: has to be cheap, may be approximate. */
    public fun countRecords(): Int
}
