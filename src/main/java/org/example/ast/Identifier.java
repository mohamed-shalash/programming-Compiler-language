package org.example.ast;


import lombok.Getter;
import lombok.Setter;
import org.example.token.Token;

@Getter
@Setter
public class Identifier implements Expression {
    private Token token; // the IDENT token
    private String value;
    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }
    public String tokenLiteral() {
        return token.getLiteral();
    }
    public void expressionNode() {
        // empty
    }


    @Override
    public String toString() {
        return value;
    }
}