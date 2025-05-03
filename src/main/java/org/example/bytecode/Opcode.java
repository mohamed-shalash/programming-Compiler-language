package org.example.bytecode;

public enum Opcode {
    OP_CONSTANT((byte) 0x00),
    OP_ADD((byte) 0x01),
    OP_SUB((byte) 0x02),
    OP_MUL((byte) 0x03),
    OP_DIV((byte) 0x04),
    OP_TRUE((byte) 0x05),
    OP_FALSE((byte) 0x06),
    OP_EQUAL((byte) 0x07),
    OP_NOTEQUAL((byte) 0x08),
    OP_GREATER_THAN((byte) 0x09),
    OP_MINUS((byte) 0x0A),
    OP_BANG((byte) 0x0B),
    OP_JUMP_NOT_TRUTHY((byte) 0x0C),
    OP_JUMP((byte) 0x0D),
    OP_NULL((byte) 0x0E),
    OP_SET_GLOBAL((byte) 0x0F),
    OP_GET_GLOBAL((byte) 0x10),
    OP_POP((byte) 0x15);

    private final byte value;

    Opcode(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static Opcode fromByte(byte b) {
        for (Opcode op : values()) {
            if (op.value == b) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: 0x" + String.format("%02x", b));
    }
}
