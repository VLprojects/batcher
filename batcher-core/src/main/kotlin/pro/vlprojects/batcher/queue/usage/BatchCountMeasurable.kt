package pro.vlprojects.batcher.queue.usage

import pro.vlprojects.batcher.usage.UsageMeasurable

public interface BatchCountMeasurable : UsageMeasurable {

    /** Waiting and taken, until confirmed. Read by the gauge on every scrape too: has to be cheap, may be approximate. */
    public fun countBatches(): Int
}
