package org.example.test;

import org.example.object.*;

import java.lang.Object;
import java.util.*;

import static org.example.test.OpCodes.*;


public class VM {
    Stack<Object> excecutionStack;
    StackController stackController;
    int ip = 0;
    private List<Object> globals = new ArrayList<>();
    private Stack<Frame> callStack = new Stack<>();

    protected static final Map<String, BuiltinFunction> BUILTINS = new HashMap<>();

    static {
        BUILTINS.put("len", new BuiltinFunction("len", (vm, args) -> {
            if (args.size() != 1) throw new RuntimeException("len expects 1 argument");
            Object arg = args.get(0);

            if (arg instanceof StringObject s) {
                return new IntegerObject(s.getValue().length());
            }
            if (arg instanceof ArrayObject a) {
                return new IntegerObject(a.getElements().size());
            }
            if (arg instanceof HashObjectCode h) {
                return new IntegerObject(h.getPairs().size());
            }
            throw new RuntimeException("Unsupported type for len");
        }        ));
        BUILTINS.put("print",
                new BuiltinFunction("print", (vm, args) -> {
                    StringBuilder output = new StringBuilder();
                    for (Object arg : args) {
                        if (arg instanceof org.example.object.Object obj) {
                            output.append(obj.inspect());
                        } else {
                            output.append(arg);
                        }
                        output.append(" ");
                    }
                    System.out.println(output.toString().trim());
                    return new NullObject();  // Return null object after printing
                }));
    }
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
                        excecutionStack.push(new IntegerObject((int) -i.getValue()));
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
                case JUMP,LOOP -> {
                    byte offsetByte = stackController.readCode(ip + 1);
                    int offset = (int) offsetByte; // Signed offset
                    ip += 2 + offset;
                }
                case GET_BUILTIN -> {
                    int nameLength = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    byte[] nameBytes = new byte[nameLength];
                    System.arraycopy(
                            stackController.getCode(),
                            ip + 2,
                            nameBytes,
                            0,
                            nameLength
                    );
                    String name = new String(nameBytes);

                    BuiltinFunction builtin = BUILTINS.get(name);
                    if (builtin == null) {
                        throw new RuntimeException("Unknown builtin: " + name);
                    }

                    excecutionStack.push(builtin);
                    ip += 2 + nameLength;
                }
                case CALL -> {
                    int argCount = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    ip += 2;
                    Object funcObj = excecutionStack.pop();
                    if (funcObj instanceof BuiltinFunction builtin) {
                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, excecutionStack.pop()); // Reverse order
                        }
                        try {
                            Object results = builtin.function.execute(this, args);
                            excecutionStack.push(results);
                        } catch (Exception e) {
                            throw new RuntimeException("Builtin error: " + e.getMessage());
                        }
                    }
                    else if (funcObj instanceof FunctionObjectCode func) {
                        //FunctionObjectCode func = (FunctionObjectCode) funcObj;
                        Frame frame = new Frame();
                        frame.returnAddress = ip;
                        frame.locals = new ArrayList<>(argCount);
                        for (int i = 0; i < argCount; i++) {
                            frame.locals.add(excecutionStack.pop());
                        }
                        callStack.push(frame);
                        ip = func.codeStart;
                    }
                }
                case OP_HASH -> {
                    int numElements = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    ip += 2;
                    HashObjectCode hash = new HashObjectCode();
                    for (int i = 0; i < numElements; i += 2) {
                        Object value = excecutionStack.pop();
                        Object key = excecutionStack.pop();
                        hash.put((org.example.object.Object) key, (org.example.object.Object) value);
                    }
                    excecutionStack.push(hash);
                }
                case OP_INDEX -> {
                    Object index = excecutionStack.pop();
                    Object collection = excecutionStack.pop();

                    if (collection instanceof HashObjectCode hash) {
                        Object value = hash.get((org.example.object.Object) index);
                        excecutionStack.push(value != null ? value : new NullObject());
                    } else if (collection instanceof ArrayObject array) { // Assuming ArrayObject exists
                        if (index instanceof IntegerObject i) {
                            int idx = (int) i.getValue();
                            if (idx < 0 || idx >= array.getElements().size()) {
                                excecutionStack.push(new NullObject());
                            } else {
                                excecutionStack.push(array.getElements().get(idx));
                            }
                        } else {
                            excecutionStack.push(new NullObject());
                        }
                    } else {
                        throw new RuntimeException("Index operation on non-indexable type");
                    }
                    ip++;
                }
                case OP_ARRAY -> {
                    int numElements = Byte.toUnsignedInt(stackController.readCode(ip + 1));
                    ip += 2;
                    List<org.example.object.Object> elements  = new ArrayList<>();

                    for (int i = 0; i < numElements; i++) {
                        elements.add(0,(org.example.object.Object) excecutionStack.pop());
                    }
                    ArrayObject array = new ArrayObject(elements);
                    excecutionStack.push(array);
                }
                case OP_LENGTH -> {
                    Object obj = excecutionStack.pop();
                    if (obj instanceof ArrayObject array) {
                        excecutionStack.push(new IntegerObject(array.getElements().size()));
                    } else if (obj instanceof StringObject str) {
                        excecutionStack.push(new IntegerObject(str.getValue().length()));
                    } else {
                        throw new RuntimeException("length not supported for type");
                    }
                    ip++;
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
        System.out.println(globals.get(symbol.index()));
    }
    public Object returnResult(String x, SymbolTable symbolTable) {
        SymbolTable.Symbol symbol = symbolTable.resolve(x);
        return globals.get(symbol.index());
    }
}