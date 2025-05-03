package org.example.compiler;

import org.example.bytecode.Bytecode;
import org.example.bytecode.Opcode;

import java.util.ArrayList;
import java.util.List;

public class VM {
    private final Bytecode bytecode;
    private final List<Object> globals = new ArrayList<>();
    private final Object[] stack = new Object[1024];
    private int sp = -1;
    private int ip = 0;

    public VM(Bytecode bytecode) {
        this.bytecode = bytecode;
    }

    public void run() {
        byte[] instructions = bytecode.getInstructions();
        List<Object> constants = bytecode.getConstants();

        while (ip < instructions.length) {
            Opcode op = Opcode.fromByte(instructions[ip++]);
            switch (op) {
                case OP_CONSTANT -> push(constants.get(readUint16(instructions)));
                case OP_GET_GLOBAL -> push(globals.get(readUint16(instructions)));
                case OP_SET_GLOBAL -> {
                    int idx = readUint16(instructions);
                    ensureGlobalCapacity(idx);
                    globals.set(idx, pop());
                }
                case OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_EQUAL, OP_NOTEQUAL, OP_GREATER_THAN -> executeBinary(op);
                case OP_TRUE -> push(true);
                case OP_FALSE -> push(false);
                case OP_POP -> pop();
            }
        }
    }

    private void executeBinary(Opcode op) {
        Object right = pop();
        Object left = pop();
        switch (op) {
            case OP_ADD -> push((long) left + (long) right);
            case OP_SUB -> push((long) left - (long) right);
            case OP_MUL -> push((long) left * (long) right);
            case OP_DIV -> push((long) left / (long) right);
            case OP_EQUAL -> push(left.equals(right));
            case OP_NOTEQUAL -> push(!left.equals(right));
            case OP_GREATER_THAN -> push((long) left > (long) right);
            default -> throw new RuntimeException("Unhandled binary op: " + op);
        }
    }

    private int readUint16(byte[] code) {
        int hi = code[ip++] & 0xFF;
        int lo = code[ip++] & 0xFF;
        return (hi << 8) | lo;
    }

    private void ensureGlobalCapacity(int idx) {
        while (globals.size() <= idx) globals.add(null);
    }

    private void push(Object val) {
        stack[++sp] = val;
    }

    private Object pop() {
        return stack[sp--];
    }

    public List<Object> getGlobals() {
        return new ArrayList<>(globals);
    }
}