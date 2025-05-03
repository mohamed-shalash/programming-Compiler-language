package org.example.test;

import org.example.ast.*;

import org.example.lexer.Lexer;
import org.example.parser.Parser;
import org.example.token.Token;


public class Runner {
    public static void main( String[] args )
    {
        Lexer lexer = new Lexer("let x = 20-3*4; let y = x;");
        //Lexer lexer = new Lexer("let x = 5>3 && 7<10-1; x = x+true;");
        /*Lexer lexer = new Lexer("""
                let x = 5+3 ; 
                x = x+2;
                """);*/
        /*Lexer lexer = new Lexer("""
                if (x > 5) {
                     let y = 10;
                } else if (x < 0) {
                     let y = -1;
                } else {
                      let y = 0;
                }
                """);*/
        /*Lexer lexer = new Lexer("""
                let x = 30;
                if (x == 5) {
                     let y = 10;
                     x = x+ y;
                }else if (x == 4) {
                    let y = !"";
                    x = y;
                }else if (x == 3) {
                    let y = 200;
                    x = y;
                }else {
                    let y = 0;
                    x = y+2;
                }
                """);*/
        /*Lexer lexer = new Lexer("""
                        let x = 5;
                        let y = 0;
                        while (x > 0) {
                            x = x - 1;

                            y = y + x;
                        }
                       
                        """);//*/
        /*Lexer lexer = new Lexer("""
                        let x = 5;
                        let y = 0;
                        while (x > 0) {
                            x = x - 1;
                            
                            y = y + x;
                        }
                       
                        """);*/
        /*Lexer lexer = new Lexer("""
                let add = fn(a, b) { return a + b };
                let adder = fn(a, b) { return add(5, 3); };
                let x = adder(5, 3);
                        """);*/
        //Lexer lexer = new Lexer("let x = \"mas\"==\"shalash\"; let y = x;");
        //todo add array hashes goto continue forloop
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        SymbolTable symbolTable =new SymbolTable();
        StackController stackController =new StackController();
        Compiler compiler =new Compiler(stackController,symbolTable);

        System.out.println(program.getStatements().get(0) instanceof IfElseIfExpression);

        compiler.compileProgram(program);
        System.out.println(stackController);

        VM vm =new VM(stackController);

        vm.run();

        vm.printResults();

        vm.printResult("x",symbolTable);
        //vm.printResult("y",symbolTable);

        func((a,b)->a+b/2);
    }

    private interface test {double apply(double a,double b);}
    private static void func(test t){
        double a =9.0;
        double b =1.3;
        System.out.println(t.apply(a,b));
    }
}
