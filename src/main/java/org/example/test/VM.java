package org.example.test;

import org.example.object.*;

import java.lang.Object;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import static org.example.test.OpCodes.*;

public class VM {
    Stack<Object> excecutionStack;
    StackController stackController;
    int ip = 0;
    private List<Object> globals = new ArrayList<>();
    private Stack<Frame> callStack = new Stack<>();

    private static class Frame {
        int returnAddress;
        List<Object> locals;
    }

    public VM(StackController stackController) {
        this.excecutionStack = new Stack<>();
        this.stackController = stackController;
    }

    public void run() {
        ip = 0;
        boolean done = true;
        while (done && ip < stackController.getCode().length) {
            OpCodes result = fromByte(stackController.readCode(ip));
            switch (result) {
                case CONSTANT -> {
                    handelConstant();

                }
                case ADD, MINUS, MULTIPLY, DIVIDE, LOWER, LOWER_EQUAL, EQUAL, NOT_EQUAL, GREATER, GREATER_EQUAL -> {
                    handelOperation(result);
                    ip++;
                }
                case AND, OR -> {
                    handelLogicalOperation(result);
                    ip++;
                }
                case SET_GLOBAL_VALUE -> {
                    handelSetGlobalValue();
                    ip += 2;
                }
                case GET_GLOBAL_VALUE -> {
                    handleGetGlobal();
                    ip += 2;
                }
                case GET_LOCAL -> {
                    int index = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    Frame currentFrame = callStack.peek();
                    if (index >= currentFrame.locals.size()) {
                        throw new RuntimeException("Local variable index out of bounds");
                    }
                    excecutionStack.push(currentFrame.locals.get(index));
                    ip += 2;
                }
                case SET_LOCAL -> { // Add OpCodes.SET_LOCAL = 0x07
                    int localSetIndex = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    Object localValue = excecutionStack.pop();
                    Frame currentFrameSet = callStack.peek();
                    if (localSetIndex >= currentFrameSet.locals.size()) {
                        throw new RuntimeException("Local variable index out of bounds: " + localSetIndex);
                    }
                    currentFrameSet.locals.set(localSetIndex, localValue);
                    ip += 2;
                    break;
                }
                case NEGATE -> {
                    Object value = excecutionStack.pop();
                    if (value instanceof IntegerObject i) {
                        excecutionStack.push(new IntegerObject(-i.getValue()));
                    }
                    ip++;
                }

                case RETURN -> {
                    Frame frame = callStack.pop();
                    ip = frame.returnAddress;
                }
                case BANG -> {
                    Object value = excecutionStack.pop();
                    excecutionStack.push(new BooleanObject(!isTruthy(value)));
                    ip++;
                }
                case JUMP_IF_NOT_TRUE -> {
                    byte offsetByte = stackController.readCode(ip + 1);
                    int offset = (int) offsetByte; // Signed offset
                    Object condition = excecutionStack.pop(); // Always pop
                    if (!isTruthy(condition)) {
                        ip += 2 + offset;
                    } else {
                        ip += 2;
                    }
                }
                case JUMP -> {
                    byte offsetByte = stackController.readCode(ip + 1);
                    int offset = (int) offsetByte; // Signed offset
                    ip += 2 + offset;
                }
                case LOOP -> {
                    byte offsetByte = stackController.readCode(ip + 1);
                    int offset = (int) offsetByte; // Signed offset
                    ip += 2 + offset; // Use JUMP-like behavior
                }
                case CALL -> {
                    int argCount = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    ip += 2;
                    Object funcObj = excecutionStack.pop();
                    if (!(funcObj instanceof FunctionObjectCode)) {
                        throw new RuntimeException("Not a function");
                    }
                    FunctionObjectCode func = (FunctionObjectCode) funcObj;
                    Frame frame = new Frame();
                    frame.returnAddress = ip;
                    frame.locals = new ArrayList<>(argCount);
                    for (int i = 0; i < argCount; i++) {
                        frame.locals.add(excecutionStack.pop());
                    }
                    callStack.push(frame);
                    ip = func.codeStart;
                }
                case EOF -> {
                    done = false;
                }
                default -> ip++;
            }
        }
    }

    private boolean isTruthy(Object value) {
        if (value instanceof BooleanObject b) return b.getValue();
        if (value instanceof IntegerObject i) return i.getValue() != 0;
        if (value instanceof StringObject s) return !s.getValue().isEmpty();
        return true;
    }

    private void handelOperation(OpCodes result) {
        if (excecutionStack.size() < 2) {
            throw new RuntimeException("Stack underflow for operation " + result + " at ip=" + ip + ", stack=" + excecutionStack);
        }
        Object rightObject = excecutionStack.pop();
        Object leftObject = excecutionStack.pop();
        if (rightObject instanceof IntegerObject right && leftObject instanceof IntegerObject left) {
            switch (result) {
                case ADD -> excecutionStack.push(new IntegerObject(left.getValue() + right.getValue()));
                case MINUS -> excecutionStack.push(new IntegerObject(left.getValue() - right.getValue()));
                case MULTIPLY -> excecutionStack.push(new IntegerObject(left.getValue() * right.getValue()));
                case DIVIDE -> excecutionStack.push(new IntegerObject(left.getValue() / right.getValue()));
                case NOT_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() != right.getValue()));
                case LOWER -> excecutionStack.push(new BooleanObject(left.getValue() < right.getValue()));
                case GREATER -> excecutionStack.push(new BooleanObject(left.getValue() > right.getValue()));
                case EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() == right.getValue()));
                case LOWER_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() <= right.getValue()));
                case GREATER_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() >= right.getValue()));
            }
        } else if (rightObject instanceof StringObject right && leftObject instanceof StringObject left) {
            switch (result) {
                case ADD -> excecutionStack.push(new StringObject(left.getValue() + right.getValue()));

                case NOT_EQUAL -> excecutionStack.push(new BooleanObject(!left.getValue().equals(right.getValue())));
                case LOWER -> excecutionStack.push(new BooleanObject(left.getValue().length() < right.getValue().length()));
                case GREATER -> excecutionStack.push(new BooleanObject(left.getValue().length() > right.getValue().length()));
                case EQUAL -> excecutionStack.push(new BooleanObject(left.getValue().equals(right.getValue())));
                case LOWER_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue().length() <= right.getValue().length()));
                case GREATER_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue().length() >= right.getValue().length()));
                default -> throw new RuntimeException("cant do operation " + result + " on strings");
            }
        } else if (rightObject instanceof BooleanObject right && leftObject instanceof BooleanObject left) {
            short boolr = (short) (right.getValue() ? 1 : 0);
            short booll = (short) (left.getValue() ? 1 : 0);
            switch (result) {
                case ADD, OR -> excecutionStack.push(new BooleanObject(left.getValue() || right.getValue()));
                case MULTIPLY, AND -> excecutionStack.push(new BooleanObject(left.getValue() && right.getValue()));
                case NOT_EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() != right.getValue()));
                case LOWER -> excecutionStack.push(new BooleanObject(booll < boolr));
                case GREATER -> excecutionStack.push(new BooleanObject(booll > boolr));
                case EQUAL -> excecutionStack.push(new BooleanObject(left.getValue() == right.getValue()));
                case LOWER_EQUAL -> excecutionStack.push(new BooleanObject(booll <= boolr));
                case GREATER_EQUAL -> excecutionStack.push(new BooleanObject(booll >= boolr));
                default -> throw new RuntimeException("cant do operation " + result + " on booleans");
            }
        }
        else if (rightObject instanceof IntegerObject right && leftObject instanceof StringObject left) {
            switch (result) {
                case MULTIPLY -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < right.getValue(); i++) {
                        sb.append(left.getValue());
                    }
                    excecutionStack.push(new StringObject(sb.toString()));
                }

                default -> throw new RuntimeException("cant do operation " + result + " on strings");
            }
        }
    }

    private void handelLogicalOperation(OpCodes result) {
        Object rightObject = excecutionStack.pop();
        Object leftObject = excecutionStack.pop();
        if (rightObject instanceof BooleanObject right && leftObject instanceof BooleanObject left) {
            switch (result) {
                case AND -> excecutionStack.push(new BooleanObject(left.getValue() && right.getValue()));
                case OR -> excecutionStack.push(new BooleanObject(left.getValue() || right.getValue()));
            }
        }
    }

    private void handelConstant() {
        int idx = Byte.toUnsignedInt(stackController.readCode(ip + 1));
        if (idx >= stackController.getConstants().size()) {
            throw new RuntimeException("Invalid constant index " + idx + " at ip=" + ip + ", constants=" + stackController.getConstants());
        }
        excecutionStack.push(stackController.getConstants().get(idx));
        ip += 2;
    }

    private void handelSetGlobalValue() {
        if (excecutionStack.isEmpty()) {
            throw new RuntimeException("Stack underflow for SET_GLOBAL_VALUE at ip=" + ip + ", stack=" + excecutionStack);
        }
        int index = Byte.toUnsignedInt(stackController.readCode(ip + 1));
        Object value = excecutionStack.pop();
        while (globals.size() <= index) {
            globals.add(null);
        }
        globals.set(index, value);
        //System.out.println("set result" + index + "   " + value);
    }

    private void handleGetGlobal() {
        int index = Byte.toUnsignedInt(stackController.readCode(ip + 1));
        if (index >= globals.size() || globals.get(index) == null) {
            throw new RuntimeException("Undefined global variable at index " + index);
        }
        excecutionStack.push(globals.get(index));
        //System.out.println("get result" + index + "   " + globals.get(index));
    }


    public void printResults() {
        while (!excecutionStack.isEmpty()) {
            System.out.println(excecutionStack.pop());
        }
    }

    public void printResult(String x, SymbolTable symbolTable) {
        SymbolTable.Symbol symbol = symbolTable.resolve(x);
    }
    public Object returnResult(String x, SymbolTable symbolTable) {
        SymbolTable.Symbol symbol = symbolTable.resolve(x);
        return globals.get(symbol.index());
    }
}