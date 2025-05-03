package org.example.test;
import java.util.*;


// SymbolTable.java
public class SymbolTable {
    private final Stack<Map<String, Symbol>> scopes = new Stack<>();
    private final Stack<Integer> indices = new Stack<>();

    public SymbolTable() {
        enterScope(); // Global scope
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
        indices.push(0); // Reset index for new scope
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
            indices.pop();
        }
    }

    public Symbol define(String name) {
        int currentIndex = indices.peek();
        Symbol symbol = new Symbol(name, currentIndex, scopes.size() - 1);
        scopes.peek().put(name, symbol);
        indices.set(indices.size() - 1, currentIndex + 1);
        return symbol;
    }


    public Symbol resolve(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    public record Symbol(String name, int index, int depth) {}
}
/*
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

/*public class SymbolTable {
    private final Stack<Map<String, Symbol>> scopes = new Stack<>();
    private int nextIndex = 0;

    public SymbolTable() {
        enterScope(); // Global scope
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
        }
    }

    public Symbol define(String name) {
        Symbol symbol = new Symbol(name, nextIndex++, scopes.size() - 1);
        scopes.peek().put(name, symbol);
        return symbol;
    }

    public Symbol resolve(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        throw new RuntimeException("Undefined variable: " + name);
    }


    public record Symbol(String name, int index, int depth) {}
}

*/






/*public class SymbolTable {
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
}*/
