package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable

/**
 * What a full buffer means. Asked before every record goes in; the first that does not admit it
 * decides by its [action]. A batcher holds several, typed like the [DispatchPolicy] list, and refuses
 * two of one name.
 */
public interface BufferBackPressurePolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the back-pressure metric. */
    public val name: String

    public val action: BackPressureAction

    public fun admits(buffer: U): Boolean
}
