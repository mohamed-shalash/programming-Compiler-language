package org.example.test;

import org.example.ast.*;
import org.example.object.BooleanObject;
import org.example.object.FunctionObjectCode;
import org.example.object.IntegerObject;
import org.example.object.StringObject;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private StackController stackController;
    private SymbolTable symbolTable ;


    Compiler(StackController stackController,SymbolTable symbolTable){
        this.stackController = stackController;
        this.symbolTable =symbolTable;
    }

    public void compileProgram(Program program){
        for(Node statement: program.getStatements()){
            compileStatement(statement);
        }
        stackController.writeCode(OpCodes.EOF.getValue());
    }

    private void compileStatement(Node statement) {
        if (statement instanceof LetStatement letStatement){
            compileLetStatement(letStatement);
        }
        else if (statement instanceof ReturnStatement returnStatement) {
            System.out.println("returnStatement");
            compileExpression(returnStatement.getReturnValue());
            stackController.writeCode(OpCodes.RETURN.getValue());
        }

        else if (statement instanceof ExpressionStatement expressionStatement) {
            Expression expr = expressionStatement.getExpression();
            if (expr instanceof IfExpression ifExpr) {
                System.out.println("Single if");
                // You can implement compilation here
                compileIfExpression(ifExpr);
            } else if (expr instanceof IfElseIfExpression ifElseExpr) {
                System.out.println("if-else chain");
                // Implement this logic here
                compileIfElseExpression(ifElseExpr);

            }else if (expr instanceof WhileExpression whileExpr) {
                compileWhileExpression(whileExpr);
            }

            //System.out.println("assign "+(expressionStatement.getExpression() instanceof AssignmentExpression));
            else  if (expressionStatement.getExpression() instanceof AssignmentExpression)
                compileAssignmentExpression((AssignmentExpression)expressionStatement.getExpression());
            else
                compileExpression(expressionStatement.getExpression());
        }
    }

    private void compileExpression(Expression exception){

        if (exception instanceof InfixExpression infixExpression){
            compileExpression(infixExpression.getLeft());
            compileExpression(infixExpression.getRight());
            compileOperator(infixExpression.getOperator());
        }
        else if (exception instanceof PrefixExpression prefixExpression){
            compilePrefix(prefixExpression);
        }
       /* else if (exception instanceof Identifier identefier) {
            SymbolTable.Symbol symbol = symbolTable.resolve(identefier.getValue());
            stackController.writeCode(OpCodes.GET_GLOBAL_VALUE.getValue());
            stackController.writeCode((byte) symbol.index());
        }*/
        if (exception instanceof Identifier ident) {
            /*SymbolTable.Symbol symbol = symbolTable.resolve(ident.getValue());
            stackController.writeCode(OpCodes.GET_GLOBAL_VALUE.getValue());
            stackController.writeCode((byte) symbol.index());*/
            SymbolTable.Symbol symbol = symbolTable.resolve(ident.getValue());
            if (symbol.depth() > 0) { // Local variable/parameter
                stackController.writeCode(OpCodes.GET_LOCAL.getValue());
                stackController.writeCode((byte) symbol.index());
            } else { // Global variable
                stackController.writeCode(OpCodes.GET_GLOBAL_VALUE.getValue());
                stackController.writeCode((byte) symbol.index());
            }

        }
        else if (exception instanceof IntegerLiteral integerLiteral) {
            compileInteger(integerLiteral);
        }  else if (exception instanceof BooleanLiteral booleanLiteral) {
            compileBoolean(booleanLiteral);
        }else if (exception instanceof StringLiteral stringLiteral) {
            compileString(stringLiteral);
        } else if (exception instanceof LogicalExpression logicalExpression) {
            compileExpression(logicalExpression.getLeft());
            compileExpression(logicalExpression.getRight());
            compileLogic(logicalExpression.getOperator());
        }  else if (exception instanceof FunctionLiteral functionLiteral) {
            //System.out.println(">>>>  "+functionLiteral);
            compileFunctionLiteral(functionLiteral);
        } else if (exception instanceof CallExpression callExpression) {
            //System.out.println(">>>>  "+callExpression);
            compileCallExpression(callExpression);
        } else if (exception instanceof HashLiteral hashLiteral) {
            List<Expression> keys = new ArrayList<>(hashLiteral.getPairs().keySet());
            keys.sort((a, b) -> a.toString().compareTo(b.toString()));//sort keys

            // Compile each key and its corresponding value in sorted order.
            for (Expression key : keys) {
                compileExpression(key);
                compileExpression(hashLiteral.getPairs().get(key));
            }
            int numPairs = hashLiteral.getPairs().size();

            stackController.writeCode(OpCodes.OP_HASH.getValue());
            stackController.writeCode((byte) (numPairs * 2));
        }  else if (exception instanceof ArrayLiteral arrayLiteral) {
            System.out.println(">>>>  "+arrayLiteral);
        }  else if (exception instanceof IndexExpression indexExpression) {
            System.out.println(">>>>  "+indexExpression);
            compileExpression(indexExpression.getLeft());

            // Compile the index expression.
            compileExpression(indexExpression.getIndex());

            // Emit the opcode for indexing.
            stackController.writeCode(OpCodes.OP_INDEX.getValue());
        }
    }

    private void compilePrefix(PrefixExpression prefixExpression) {
        compileExpression(prefixExpression.getRight());
        if (prefixExpression.getOperator().equals("-")){
            stackController.writeCode(OpCodes.NEGATE.getValue());
        }
        else if (prefixExpression.getOperator().equals("!")){
            stackController.writeCode(OpCodes.BANG.getValue());
        }
    }

    private void compileLetStatement(LetStatement statement){

        String varName = statement.getName().getValue();
        SymbolTable.Symbol symbol = symbolTable.define(varName);
        compileExpression(statement.getValue());
        stackController.writeCode(OpCodes.SET_GLOBAL_VALUE.getValue());
        stackController.writeCode((byte) symbol.index());


    }

    private void compileAssignmentExpression(AssignmentExpression assignmentExpression) {
        /*compileExpression(assignmentExpression.getValue());
        SymbolTable.Symbol symbol = symbolTable.resolve(assignmentExpression.getName().getValue());
        stackController.writeCode(OpCodes.SET_GLOBAL_VALUE.getValue());
        stackController.writeCode((byte) symbol.index());*/
        compileExpression(assignmentExpression.getValue());
        SymbolTable.Symbol symbol = symbolTable.resolve(assignmentExpression.getName().getValue());
        if (symbol.depth() > 0) { // Local variable
            stackController.writeCode(OpCodes.SET_LOCAL.getValue());
            stackController.writeCode((byte) symbol.index());
        } else { // Global variable
            stackController.writeCode(OpCodes.SET_GLOBAL_VALUE.getValue());
            stackController.writeCode((byte) symbol.index());
        }
    }

    public void compileIfExpression(IfExpression expr) {
        compileExpression(expr.getCondition());
        int jumpIfFalsePos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP_IF_NOT_TRUE.getValue());
        stackController.writeCode((byte) 0); // Placeholder


        compileBlockStatement(expr.getConsequence());
        stackController.writeCode(OpCodes.POP_SCOPE.getValue());

        if (expr.getAlternative() != null) {
            // Emit unconditional jump to skip else block
            int jumpPos = stackController.getIndex();
            stackController.writeCode(OpCodes.JUMP.getValue());
            stackController.writeCode((byte) 0); // Placeholder

            // Target the start of the else block
            int afterConsequence = stackController.getIndex();
            int offset = afterConsequence - (jumpIfFalsePos + 2);
            updateJumpOffset(jumpIfFalsePos, offset);

            compileBlockStatement(expr.getAlternative());
            stackController.writeCode(OpCodes.POP_SCOPE.getValue()); // Add POP_SCOPE for consistency

            int endPos = stackController.getIndex();
            offset = endPos - (jumpPos + 2); // Correct JUMP offset
            updateJumpOffset(jumpPos, offset);
        } else {
            int currentIndex = stackController.getIndex();
            int offset = currentIndex - (jumpIfFalsePos + 2); // Correct JUMP_IF_FALSE offset
            updateJumpOffset(jumpIfFalsePos, offset);
        }
    }


    public void compileIfElseExpression(IfElseIfExpression expr) {
        compileExpression(expr.getCondition());
        int jumpIfFalsePos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP_IF_NOT_TRUE.getValue());
        stackController.writeCode((byte) 0); // Placeholder

        compileBlockStatement(expr.getConsequence());

        stackController.writeCode(OpCodes.POP_SCOPE.getValue());
        int jumpPos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP.getValue());
        stackController.writeCode((byte) 0); // Placeholder
        int afterConsequence = stackController.getIndex();
        int offset;
        if (expr.getAlternative() instanceof BlockStatement) {
            // For else block, target the start of the block
            offset = afterConsequence - (jumpIfFalsePos + 2);
            updateJumpOffset(jumpIfFalsePos, offset);

            compileBlockStatement((BlockStatement) expr.getAlternative());

            stackController.writeCode(OpCodes.POP_SCOPE.getValue());
        } else {
            // For else if or if, use standard offset
            offset = afterConsequence - (jumpIfFalsePos + 2);
            updateJumpOffset(jumpIfFalsePos, offset);
            if (expr.getAlternative() instanceof IfElseIfExpression ifElseExpr) {
                compileIfElseExpression(ifElseExpr);
            } else if (expr.getAlternative() instanceof IfExpression ifExpr) {
                compileIfExpression(ifExpr);
            }
        }
        int endPos = stackController.getIndex();
        offset = endPos - (jumpPos + 2);
        updateJumpOffset(jumpPos, offset);
    }
    private void updateJumpOffset(int jumpOpcodePosition, int offset) {
        // Ensure offset fits in 1 byte (-128 to 127)
        if (offset < Byte.MIN_VALUE || offset > Byte.MAX_VALUE) {
            throw new RuntimeException(
                    "Jump offset " + offset + " out of byte range at position " + jumpOpcodePosition
            );
        }

        // Write offset after the opcode (position + 1)
        stackController.setCode(jumpOpcodePosition + 1, (byte) offset);
    }

    private void compileBlockStatement(BlockStatement block) {
        for (Statement stmt : block.getStatements()) {
            compileStatement(stmt);
        }
    }
    public void compileWhileExpression(WhileExpression expr) {
        int loopStart = stackController.getIndex();
        stackController.writeCode(OpCodes.LOOP_START.getValue());

        compileExpression(expr.getCondition());
        int jumpIfFalsePos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP_IF_NOT_TRUE.getValue());
        stackController.writeCode((byte) 0); // Placeholder

        compileBlockStatement(expr.getBody());


        int jumpInstructionPos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP.getValue());
        int jumpBackOffset = loopStart - (jumpInstructionPos + 2); // Negative offset to loopStart
        stackController.writeCode((byte) jumpBackOffset);

        int loopEndPos = stackController.getIndex();
        stackController.writeCode(OpCodes.LOOP_END.getValue());
        int exitOffset = loopEndPos - (jumpIfFalsePos + 2); // Offset to LOOP_END
        updateJumpOffset(jumpIfFalsePos, exitOffset);
    }


    void compileInteger(IntegerLiteral integerLiteral){
        stackController.writeCode(OpCodes.CONSTANT.getValue());
        stackController.writeCode((byte) stackController.getConstants().size());
        stackController.writeConstant(new IntegerObject(integerLiteral.getValue()));
    }

    private void compileBoolean(BooleanLiteral booleanLiteral) {
        stackController.writeCode(OpCodes.CONSTANT.getValue());
        stackController.writeCode((byte) stackController.getConstants().size());
        stackController.writeConstant(new BooleanObject(booleanLiteral.getValue()));
    }

    private void compileString(StringLiteral stringLiteral) {
        stackController.writeCode(OpCodes.CONSTANT.getValue());
        stackController.writeCode((byte) stackController.getConstants().size());
        stackController.writeConstant(new StringObject(stringLiteral.getValue()));
    }


    void compileOperator(String operator){
        switch (operator){
            case "+":
                stackController.writeCode(OpCodes.ADD.getValue());
                break;
            case "-":
                stackController.writeCode(OpCodes.MINUS.getValue());
                break;
            case "*":
                stackController.writeCode(OpCodes.MULTIPLY.getValue());
                break;
            case "/":
                stackController.writeCode(OpCodes.DIVIDE.getValue());
                break;
            case ">":
                stackController.writeCode(OpCodes.GREATER.getValue());
                break;
            case "<":
                stackController.writeCode(OpCodes.LOWER.getValue());
                break;
            case "==":
                stackController.writeCode(OpCodes.EQUAL.getValue());
                break;
            case "!=":
                stackController.writeCode(OpCodes.NOT_EQUAL.getValue());
                break;
            case ">=":
                stackController.writeCode(OpCodes.GREATER_EQUAL.getValue());
                break;
            case "<=":
                stackController.writeCode(OpCodes.LOWER_EQUAL.getValue());
                break;

        }
    }
    private void compileLogic(String operator) {
        switch (operator){
            case "&&":
                stackController.writeCode(OpCodes.AND.getValue());
                break;
            case "||":
                stackController.writeCode(OpCodes.OR.getValue());
                break;

        }
    }


    // In Compiler.java
    public void compileFunctionLiteral(FunctionLiteral func) {
        // Emit a JUMP to skip the function body
        int jumpPos = stackController.getIndex();
        stackController.writeCode(OpCodes.JUMP.getValue());
        stackController.writeCode((byte) 0); // Placeholder offset, updated later

        // Compile the function body
        int funcStart = stackController.getIndex();
        symbolTable.enterScope();
        for (Identifier param : func.getParameters()) {
            symbolTable.define(param.getValue());
        }
        for (Statement stmt : func.getBody().getStatements()) {
            compileStatement(stmt);
        }
        stackController.writeCode(OpCodes.RETURN.getValue());
        symbolTable.exitScope();

        // Update JUMP offset to skip the function body
        int afterFunc = stackController.getIndex();
        int offset = afterFunc - (jumpPos + 2); // Offset from after JUMP to after function body
        if (offset > 127 || offset < -128) {
            throw new RuntimeException("Function body too large for single-byte jump offset");
        }
        stackController.setCode(jumpPos + 1, (byte) offset);

        // Create and push the function object
        int funcIndex = stackController.getConstants().size();
        FunctionObjectCode funcObj = new FunctionObjectCode();
        funcObj.parameterCount = func.getParameters().size();
        funcObj.codeStart = funcStart;
        stackController.writeConstant(funcObj);
        stackController.writeCode(OpCodes.CONSTANT.getValue());
        stackController.writeCode((byte) funcIndex);
    }

    private void compileCallExpression(CallExpression call) {
        // Push arguments
        for (Expression arg : call.getArguments()) {
            compileExpression(arg);
        }

        // Look up function
        compileExpression(call.getFunction());

        // Emit CALL with argument count
        stackController.writeCode(OpCodes.CALL.getValue());
        stackController.writeCode((byte) call.getArguments().size());
    }

}

