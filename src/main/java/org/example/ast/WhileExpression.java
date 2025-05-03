package org.example.ast;

import lombok.Getter;
import lombok.Setter;
import org.example.token.Token;

@Getter
@Setter
public class WhileExpression implements Expression{
    private Token token;
    private Expression condition;
    private BlockStatement body;

    public WhileExpression(Token token, Expression condition, BlockStatement body) {
        this.token = token;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString() {
        return "while (" + condition.toString() + ") " + body.toString();
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String tokenLiteral() {
        return null;
    }
}
