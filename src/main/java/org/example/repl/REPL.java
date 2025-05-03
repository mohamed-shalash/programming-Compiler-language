package org.example.repl;

import org.example.lexer.Lexer;
import org.example.parser.Parser;
import org.example.ast.Program;
import org.example.bytecode.Bytecode;
import org.example.compiler.Compiler;
import org.example.compiler.VM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class REPL {
    private static final String PROMPT = ">> ";

    public void start() {
        System.out.println("Welcome to the Monkey Bytecode REPL");
        System.out.println("Enter 'exit' to quit");
        Compiler compiler = new Compiler();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print(PROMPT);
                String input = reader.readLine();

                if (input == null || input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting REPL");
                    break;
                }

                try {
                    // Lexing and Parsing
                    Lexer lexer = new Lexer(input);
                    Parser parser = new Parser(lexer);
                    Program program = parser.parseProgram();

                    // Print parser errors if any
                    List<String> errors = parser.getErrors();
                    if (!errors.isEmpty()) {
                        for (String err : errors) {
                            System.out.println("Parser error: " + err);
                        }
                        continue;
                    }

                    // Compilation

                    Bytecode bytecode = compiler.compile(program);

                    // VM Execution
                    VM vm = new VM(bytecode);
                    vm.run();

                    // Display globals
                    System.out.println("Globals: " + vm.getGlobals());
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new REPL().start();
    }
}
