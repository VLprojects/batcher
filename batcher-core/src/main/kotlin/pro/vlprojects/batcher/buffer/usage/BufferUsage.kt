package pro.vlprojects.batcher.buffer.usage

/**
 * What a buffer reports about the records it is collecting. Each kind is a subtype of its own, and a
 * buffer implements those it can count: a policy reads one kind and works with such a buffer only.
 */
public interface BufferUsage
