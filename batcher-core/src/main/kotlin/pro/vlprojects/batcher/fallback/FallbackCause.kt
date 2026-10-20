package pro.vlprojects.batcher.fallback

public class FallbackCause(
    public val reason: FallbackReason,
    /** Null when nothing was thrown, as with a full queue. */
    public val error: Throwable?,
) {

    override fun toString(): String = "FallbackCause[reason: $reason, error: $error]"
}
