package pro.vlprojects.batcher.queue.metric

public interface QueueMeter {

    /** A function and not a number: a meter reads it on every scrape. Not called for a queue that cannot tell its size. */
    public fun watchWritingQueue(batches: () -> Int) {}
}
