package com.example.examplemod.core;

/**
 * Anything that can receive a payload implements this: belts, machine inputs/outputs, etc.
 */
public interface Port {
    boolean canAccept(Payload payload);
    void accept(Payload payload);
}

