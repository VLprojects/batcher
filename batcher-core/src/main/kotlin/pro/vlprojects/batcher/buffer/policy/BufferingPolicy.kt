package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.BufferUsage

/**
 * One rule for when what a buffer collects goes as a batch. A buffer holds several, and the first one
 * met dispatches: a new rule, a byte budget say, is a policy over a usage of its own and not a buffer.
 */
public interface BufferingPolicy<U : BufferUsage> {

    /**
     * The usage it reads. Generics are erased at run time, so this is the only way for a buffer to see
     * when built that it can serve the policy, and not with a cast failing on the first record.
     */
    public val usageType: Class<U>

    /** The `policy` tag of the dispatch metric: dashboards key on it, so it must not change. */
    public val name: String

    /** Asked after every add and on every check of the buffer's own, with records collecting. */
    public fun shouldDispatch(usage: U): Boolean
}
