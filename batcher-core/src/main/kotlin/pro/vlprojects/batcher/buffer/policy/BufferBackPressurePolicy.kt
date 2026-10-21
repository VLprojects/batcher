package pro.vlprojects.batcher.buffer.policy

import pro.vlprojects.batcher.buffer.usage.UsageMeasurable

/**
 * What a full buffer means. Asked before every record goes in; once one requires back pressure the
 * record goes to the [AcceptingFallback][pro.vlprojects.batcher.fallback.AcceptingFallback] instead. A batcher holds several, typed like the [DispatchPolicy] list, and refuses
 * two of one name.
 */
public interface BufferBackPressurePolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the back-pressure metric. */
    public val name: String

    public fun requiresBackPressure(buffer: U): Boolean
}
