package org.example.bytecode;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Bytecode {
    private final byte[] instructions;
    private final List<Object> constants;

    public Bytecode(byte[] instructions, List<Object> constants) {
        this.instructions = instructions.clone();
        this.constants = List.copyOf(constants);
    }

    public byte[] getInstructions() {
        return instructions.clone();
    }

    public List<Object> getConstants() {
        return constants;
    }
}
