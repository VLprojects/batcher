package pro.vlprojects.batcher.buffer.accept

import pro.vlprojects.batcher.buffer.dispatch.DispatchPolicy
import pro.vlprojects.batcher.buffer.fallback.AcceptingFallback
import pro.vlprojects.batcher.buffer.usage.UsageMeasurable

/**
 * What a full buffer means. Asked before every record goes in; once one requires back pressure the
 * record goes to the [AcceptingFallback][pro.vlprojects.batcher.fallback.AcceptingFallback] instead. A batcher holds several, typed like the [DispatchPolicy] list, and refuses
 * two of one name.
 */
public interface BufferBackPressurePolicy<in U : UsageMeasurable> {

    /** The `policy` tag of the back-pressure metric. */
    public val name: String

    /** The `component` tag of the policy's metrics: the stage it guards. */
    public val component: String
        get() = "buffer"

    /** The `action` tag of the policy's metrics: what it decides. */
    public val action: String
        get() = "back_pressure"

    public fun requiresBackPressure(buffer: U): Boolean
}
