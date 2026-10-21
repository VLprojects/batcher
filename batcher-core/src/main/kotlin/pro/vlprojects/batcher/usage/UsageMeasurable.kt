package pro.vlprojects.batcher.usage

/**
 * What a stage reports about what it holds. Each kind is a subtype of its own, and a stage implements
 * those it can count: a policy reads one kind and works with such a stage only.
 */
public interface UsageMeasurable
