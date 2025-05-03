package org.example.test;

import java.util.List;

public class BuiltinFunction {
    public interface BuiltinFunc {
        Object execute(VM vm, List<Object> args);
    }

    private final String name;
    public final BuiltinFunc function;

    public BuiltinFunction(String name, BuiltinFunc function) {
        this.name = name;
        this.function = function;
    }

    @Override
    public String toString() {
        return "[Builtin: " + name + "]";
    }
}
