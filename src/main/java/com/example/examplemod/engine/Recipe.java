package com.example.examplemod.engine;


public record Recipe(String inputTypeId, String outputTypeId, long durationTicks) {

    public boolean matchesInput(Payload payload) {
        return payload.typeId().equals(inputTypeId);
    }

    public Payload craftOutput() {
        return new Payload(outputTypeId);
    }
}

