package org.example.compiler;

import junit.framework.TestCase;
import static org.junit.jupiter.api.Assertions.*;

import org.example.ast.*;
import org.example.bytecode.Bytecode;
import org.example.bytecode.Opcode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CompilerTest extends TestCase {

    /*@Test
    void testIntegerAddition() throws Exception {
        // 5 + 10
        InfixExpression expr = new InfixExpression(
                new IntegerLiteral(5),
                "+",
                new IntegerLiteral(10)
        );

        Bytecode bytecode = compileExpression(expr);

        // Verify constants
        assertEquals(List.of(5L, 10L), bytecode.getConstants());

        // Verify bytecode instructions
        assertInstructions(bytecode, List.of(
                new Instruction(Opcode.OP_CONSTANT, 0),
                new Instruction(Opcode.OP_CONSTANT, 1),
                new Instruction(Opcode.OP_ADD),
                new Instruction(Opcode.OP_POP)
        ));
    }

    @Test
    void testBooleanComparison() throws Exception {
        // true == false
        InfixExpression expr = new InfixExpression(
                new BooleanLiteral(true),
                "==",
                new BooleanLiteral(false)
        );

        Bytecode bytecode = compileExpression(expr);

        assertInstructions(bytecode, List.of(
                new Instruction(Opcode.OP_TRUE),
                new Instruction(Opcode.OP_FALSE),
                new Instruction(Opcode.OP_EQUAL),
                new Instruction(Opcode.OP_POP)
        ));
    }

    @Test
    void testStringConcatenation() throws Exception {
        // "foo" + "bar"
        InfixExpression expr = new InfixExpression(
                new StringLiteral("foo"),
                "+",
                new StringLiteral("bar")
        );

        Bytecode bytecode = compileExpression(expr);

        assertEquals(
                List.of(new StringObject("foo"), new StringObject("bar")),
                bytecode.getConstants()
        );

        assertInstructions(bytecode, List.of(
                new Instruction(Opcode.OP_CONSTANT, 0),
                new Instruction(Opcode.OP_CONSTANT, 1),
                new Instruction(Opcode.OP_ADD),
                new Instruction(Opcode.OP_POP)
        ));
    }

    @Test
    void testLessThanOperator() throws Exception {
        // 3 < 5
        InfixExpression expr = new InfixExpression(
                new IntegerLiteral(3),
                "<",
                new IntegerLiteral(5)
        );

        Bytecode bytecode = compileExpression(expr);

        // Should compile to 5 > 3
        assertEquals(List.of(5L, 3L), bytecode.getConstants());
        assertInstructions(bytecode, List.of(
                new Instruction(Opcode.OP_CONSTANT, 0),
                new Instruction(Opcode.OP_CONSTANT, 1),
                new Instruction(Opcode.OP_GREATER_THAN),
                new Instruction(Opcode.OP_POP)
        ));
    }

    @Test
    void testVariableExpression() throws Exception {
        // x + y (assuming x and y are defined)
        Program program = new Program(List.of(
                new LetStatement(new Identifier("x"), new IntegerLiteral(5)),
                new LetStatement(new Identifier("y"), new IntegerLiteral(10)),
                new ExpressionStatement(new InfixExpression(
                        new Identifier("x"),
                        "+",
                        new Identifier("y")
                ))
        ));

        Compiler compiler = new Compiler();
        Bytecode bytecode = compiler.compile(program);

        assertInstructions(bytecode, List.of(
                // let x = 5
                new Instruction(Opcode.OP_CONSTANT, 0),
                new Instruction(Opcode.OP_SET_GLOBAL, 0),

                // let y = 10
                new Instruction(Opcode.OP_CONSTANT, 1),
                new Instruction(Opcode.OP_SET_GLOBAL, 1),

                // x + y
                new Instruction(Opcode.OP_GET_GLOBAL, 0),
                new Instruction(Opcode.OP_GET_GLOBAL, 1),
                new Instruction(Opcode.OP_ADD),
                new Instruction(Opcode.OP_POP)
        ));
    }

    @Test
    void testUnknownOperator() {
        InfixExpression expr = new InfixExpression(
                new IntegerLiteral(5),
                "^",
                new IntegerLiteral(3)
        );

        assertThrows(RuntimeException.class, () -> compileExpression(expr));
    }

    // Helper methods
    private Bytecode compileExpression(Expression expr) {
        Program program = new Program(List.of(
                new ExpressionStatement(expr)
        ));
        return new Compiler().compile(program);
    }

    private void assertInstructions(Bytecode bytecode, List<Instruction> expected) {
        List<Instruction> actual = parseInstructions(bytecode.getInstructions());
        assertEquals(expected, actual);
    }

    private List<Instruction> parseInstructions(byte[] bytecode) {
        List<Instruction> instructions = new ArrayList<>();
        int ip = 0;

        while (ip < bytecode.length) {
            byte opByte = bytecode[ip++];
            Opcode op = Opcode.fromByte(opByte);

            if (op == Opcode.OP_CONSTANT || op == Opcode.OP_SET_GLOBAL || op == Opcode.OP_GET_GLOBAL) {
                int operand = ((bytecode[ip++] & 0xFF) << 8 | (bytecode[ip++] & 0xFF);
                instructions.add(new Instruction(op, operand));
            } else {
                instructions.add(new Instruction(op));
            }
        }

        return instructions;
    }

    private record Instruction(Opcode op, Integer... operands) {
        Instruction(Opcode op, int operand) {
            this(op, new Integer[]{operand});
        }
    }*/

}