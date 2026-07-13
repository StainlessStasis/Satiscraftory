package io.github.stainlessstasis.manifold.factory_component;

/**
 * Anything that can receive a payload implements this: belts, machine inputs/outputs, etc.
 */
public interface Port {
    boolean canAccept(Payload payload);
    void accept(Payload payload);
    default void acceptWithOverflow(Payload payload, double overflowAmount) {
        accept(payload);
    }
}

