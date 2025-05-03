package org.example.compiler;

import org.example.ast.*;
import org.example.bytecode.Bytecode;
import org.example.bytecode.Opcode;
import org.example.bytecode.SymbolTable;
import org.example.object.StringObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Compiler {
    private final SymbolTable symbols = new SymbolTable();
    private final List<Object> constants = new ArrayList<>();
    private final ByteArrayOutputStream bytecode = new ByteArrayOutputStream();
    private final Deque<EmittedInstruction> emittedInstructions = new ArrayDeque<>();

    public Bytecode compile(Program program) {
        for (Statement stmt : program.getStatements()) {
            compileStatement(stmt);
        }
        return new Bytecode(bytecode.toByteArray(), constants);
    }

    private void compileStatement(Statement stmt) {
        if (stmt instanceof LetStatement let) {
            compileLetStatement(let);
        } else if (stmt instanceof ExpressionStatement exprStmt) {
            compileExpression(exprStmt.getExpression());
            emit(Opcode.OP_POP);
        }
    }

    private void compileLetStatement(LetStatement let) {
        compileExpression(let.getValue());
        SymbolTable.Symbol symbol = symbols.define(let.getName().getToken().getLiteral());
        emit(Opcode.OP_SET_GLOBAL, symbol.index());
    }

    private void compileExpression(Expression expr) {
        if (expr instanceof IntegerLiteral intLit) {
            emitConstant(intLit.getValue());
        } else if (expr instanceof BooleanLiteral boolLit) {
            emit(boolLit.getValue() ? Opcode.OP_TRUE : Opcode.OP_FALSE);
        } else if (expr instanceof StringLiteral strLit) {
            StringObject strObj = new StringObject(strLit.getValue());
            int idx = addConstant(strObj);
            emit(Opcode.OP_CONSTANT, idx);
        } else if (expr instanceof Identifier id) {
            SymbolTable.Symbol symbol = symbols.resolve(id.getToken().getLiteral());
            emit(Opcode.OP_GET_GLOBAL, symbol.index());
        } else if (expr instanceof InfixExpression infix) {
            compileInfixExpression(infix);
        } else {
            throw new RuntimeException("Unknown expression: " + expr.getClass());
        }
    }

    private void compileInfixExpression(InfixExpression infix) {
        // Handle < by swapping and using >
        if ("<".equals(infix.getOperator())) {
            compileExpression(infix.getRight());
            compileExpression(infix.getLeft());
            emit(Opcode.OP_GREATER_THAN);
            return;
        }
        compileExpression(infix.getLeft());
        compileExpression(infix.getRight());

        switch (infix.getOperator()) {
            case "+" -> emit(Opcode.OP_ADD);
            case "-" -> emit(Opcode.OP_SUB);
            case "*" -> emit(Opcode.OP_MUL);
            case "/" -> emit(Opcode.OP_DIV);
            case ">" -> emit(Opcode.OP_GREATER_THAN);
            case "==" -> emit(Opcode.OP_EQUAL);
            case "!=" -> emit(Opcode.OP_NOTEQUAL);
            default -> throw new RuntimeException("Unknown operator: " + infix.getOperator());
        }
    }

    private void emitConstant(long value) {
        int idx = addConstant(value);
        emit(Opcode.OP_CONSTANT, idx);
    }

    private int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }

    private void emit(Opcode op, int... operands) {
        bytecode.write(op.getValue());
        for (int operand : operands) {
            bytecode.write((operand >> 8) & 0xFF);
            bytecode.write(operand & 0xFF);
        }
        emittedInstructions.push(new EmittedInstruction(op, bytecode.size()));
    }

    private record EmittedInstruction(Opcode opcode, int position) {}
}

