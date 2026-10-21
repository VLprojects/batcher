package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable

/**
 * What a full buffer means. Asked before every record goes in; the first that requires back pressure
 * decides by its [action] what happens to the record. A batcher holds several, typed like the [DispatchPolicy] list, and refuses
 * two of one name.
 */
public interface BufferBackPressurePolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the back-pressure metric. */
    public val name: String

    public val action: BackPressureAction

    public fun requiresBackPressure(buffer: U): Boolean
}
