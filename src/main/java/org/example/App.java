package org.example;

import org.example.ast.Program;
import org.example.bytecode.Bytecode;
import org.example.compiler.Compiler;
import org.example.compiler.VM;
import org.example.lexer.Lexer;
import org.example.parser.Parser;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Lexer lexer = new Lexer("let x = 5; let y = x;");
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        Compiler compiler = new Compiler();
        Bytecode bytecode = compiler.compile(program);

        VM vm = new VM(bytecode);
        vm.run();

        List<Object> globals = vm.getGlobals();
        System.out.println(globals.get(0)); // 5
        System.out.println(globals.get(1)); // 5
    }
}
