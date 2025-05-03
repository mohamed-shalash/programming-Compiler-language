package org.example.bytecode;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private int scopeDepth = 0;

    public Symbol define(String name)  {
        Symbol symbol = new Symbol(name, symbols.size(), scopeDepth);
        symbols.put(name, symbol);
        return symbol;
    }

    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol == null) {
            throw new RuntimeException("Undefined symbol: " + name);
        }
        return symbol;
    }


    public record Symbol(String name, int index, int depth) {}
}
