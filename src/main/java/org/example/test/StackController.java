package org.example.test;

import lombok.Getter;
import lombok.Setter;
import org.example.ast.Expression;
import org.example.object.Object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class StackController {
    private byte[] code;
    private int capacity ;
    private int index ;
    private List<Object> constants;


    StackController() {
        code = new byte[0];
        constants = new ArrayList<>();
        capacity = 0;
        index = 0;
    }

    public void writeCode(byte code) {
        if (capacity < index + 1) {
            capacity = growCapacity(capacity);
            this.code = growArray(this.code, capacity);
        }
        this.code[index] = code;
        index++;
    }
    public void emitConstant(Object constant){
        this.constants.add(constant);
    }

    public byte[] getCode() {
        return Arrays.copyOf(code, index);
    }

    public List<Object> getConstants() {
        return constants;
    }

    public Object readConstant(int idx){
        return constants.get(idx);
    }
    public void writeConstant(Object constant){
        this.constants.add(constant);
    }
    public byte readCode(int idx){return code[idx];}

    private int growCapacity(int cap) {
        return cap < 8 ? 8 : cap * 2;
    }

    private byte[] growArray(byte[] oldArray, int newCap) {
        byte[] newA = new byte[newCap];
        System.arraycopy(oldArray, 0, newA, 0, oldArray.length);
        return newA;
    }

    public void setCode(int index, byte value) {
        if (index < 0 || index >= code.length) {
            throw new IndexOutOfBoundsException(
                    "Invalid code index: " + index + " (code length: " + code.length + ")"
            );
        }
        code[index] = value;
    }

    public void writeBytes(byte[] bytes) {
        int neededCapacity = index + bytes.length;
        if (capacity < neededCapacity) {
            while (capacity < neededCapacity) {
                capacity = growCapacity(capacity);
            }
            this.code = growArray(this.code, capacity);
        }

        System.arraycopy(bytes, 0, this.code, index, bytes.length);
        index += bytes.length;
    }

    @Override
    public String toString() {
        return "StackController{" +
                "code=" + Arrays.toString(code) +
                ", capacity=" + capacity +
                ", constants=" + constants +
                '}';
    }
}


enum OpCodes{
    CONSTANT((byte) 0x00),
    ADD((byte) 0x01),
    MINUS((byte) 0x02),
    MULTIPLY((byte) 0x03),
    DIVIDE((byte) 0x04),
    EOF((byte) 0x05),
    GREATER((byte) 0x06),  LOWER((byte) 0x07),    LOWER_EQUAL((byte) 0x08),GREATER_EQUAL((byte) 0x09),
    EQUAL((byte)0x0A),   NOT_EQUAL((byte)0x0B), AND((byte)0x0C) , OR((byte) 0x0D),
    SET_GLOBAL_VALUE((byte) 0x0E),GET_GLOBAL_VALUE((byte) 0x0F),
    JUMP_IF_NOT_TRUE((byte) 0x10),  // Jump if top of stack is false
    JUMP((byte) 0x11),
    POP_SCOPE((byte) 0x12)   ,        NEGATE((byte) 0x13),    BANG((byte) 0x14),POP((byte) 0x15),
    LOOP_START((byte) 0x20),      LOOP_END((byte) 0x21),LOOP((byte) 0x16),CONTINUE((byte) 0x17) , BREAK((byte) 0x18),
    CALL((byte) 0x19),    // Call function with N args
    RETURN((byte) 0x22),
    GET_LOCAL((byte) 0x23),
    SET_LOCAL((byte) 0x24),
    OP_HASH(  (byte) 0x25), OP_INDEX((byte) 0x1A),OP_ARRAY( (byte) 0x1B), GET_BUILTIN((byte)(0x1C) ),

    OP_LENGTH((byte) 0x1D);

    private final byte value;

    OpCodes(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static OpCodes fromByte(byte b) {
        for (OpCodes op : values()) {
            if (op.value == b) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: 0x" + String.format("%02x", b));
    }
}