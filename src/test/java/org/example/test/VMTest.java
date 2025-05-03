package org.example.test;

import junit.framework.TestCase;
import org.example.ast.IfElseIfExpression;
import org.example.ast.Program;
import org.example.lexer.Lexer;
import org.example.object.*;
import org.example.object.Object;
import org.example.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class VMTest extends TestCase {

    public void testArithmeticExpression(String input,String variable, Object myresult) throws Exception {
        // 1. Set up input and compile

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        SymbolTable symbolTable = new SymbolTable();
        StackController stackController = new StackController();
        Compiler compiler = new Compiler(stackController, symbolTable);
        compiler.compileProgram(program);

        // 2. Execute the bytecode
        VM vm = new VM(stackController);
        vm.run();

        // 3. Verify the result
        SymbolTable.Symbol symbol = symbolTable.resolve(variable);
        assertNotNull("Variable 'result' should exist", symbol);

        // Access private globals field via reflection
        Field globalsField = VM.class.getDeclaredField("globals");
        globalsField.setAccessible(true);
        List<Object> globals = (List<Object>) globalsField.get(vm);


        if( myresult instanceof IntegerObject) {
            IntegerObject result = (IntegerObject) globals.get(symbol.index());
            assertEquals(((IntegerObject) myresult).getValue(), result.getValue());
        }
        else if(myresult instanceof StringObject){
            StringObject result = (StringObject) globals.get(symbol.index());
            assertEquals(((StringObject) myresult).getValue(), result.getValue());
        }
        else if(myresult instanceof BooleanObject){
            BooleanObject result = (BooleanObject) globals.get(symbol.index());
            assertEquals(((BooleanObject) myresult).getValue(), result.getValue());
        }  else{
            Assertions.fail("result should be an IntegerObject");
        }
    }


    @Test
    public void testCalculator(){
        String input = "let result = 5*2-3;";
        Testing [] testings = new Testing[]{
                new Testing("let result = 5*2-3;","result",new IntegerObject(7)),
                new Testing("let result = 5*2+4;","result",new IntegerObject(14)),
                new Testing("let result = \"mas\"+\"shalsh\";","result",new StringObject("masshalsh")),
                new Testing("let result = \"mas\"<\"shalsh\";","result",new BooleanObject(true)),
                new Testing("let result = \"sha\"+\"lsh\"==\"shalsh\";","result",  new BooleanObject(true)),
                new Testing("let result = \"mas\"+\"tas\">\"shalsh\"+\"tas\";","result",  new BooleanObject(false)),
                new Testing("let result = \"mas\"*2 < \"shalsh\";","result",new BooleanObject(false)),
                new Testing("let result = true<false;","result",new BooleanObject(false)),
                new Testing("let result = true<!false;","result",new BooleanObject(false)),
                new Testing("let result = true==!false;","result",new BooleanObject(true)),
                new Testing("let result = !!true==!false;","result",new BooleanObject(true)),
                new Testing("let result = 5*2-3; result = result+3","result",new IntegerObject(10)),
                new Testing("let result = 100; if (true) { result =10 };","result",new IntegerObject(10)),
                new Testing("let result = 1000;if (5 > 3) { result =5 } else { result=3 };","result",new IntegerObject(5)),
                new Testing("""
                        let result = 5;
                        if (result == 3) { result =3*3 } 
                        else if(result ==4 ){ result=4*4 }
                        else if(result ==5 ){ result=5*5 }
                        else { result = 3*4 };
                        ""","result",new IntegerObject(25)),
                new Testing("""
                        let result = 0;
                        let x = 6;
                        while(x > 3 ){
                            result = result +x;
                            x = x - 1;
                        }
                        ""","result",new IntegerObject(15)),
                new Testing("""
                        let add = fn(a, b) { return a + b };
                        let result = add(2, 3);
                        ""","result",new IntegerObject(5)),
                new Testing("""
                        let adder = fn(a, b) { return a + b };
                        let add = fn(a, b) { return adder(a,b); };
                        let result = add(2, 3);
                        ""","result",new IntegerObject(5)),
                new Testing("""
                        let adder = fn(a, b) { if (a > b) { return a } else { return b } };
                        let add = fn(a, b) { return adder(a,b); };
                        let result = add(2, 3);
                        ""","result",new IntegerObject(3)),
                new Testing("""
                            let adder = fn(a, b) { 
                                while(a > 0) {
                                    a = a - 1;
                                    b = b + a;
                                }
                                return b; 
                            };
                            let add = fn(a, b) { return adder(a,b); };
                            let result = add(2, 3);
                            ""","result",new IntegerObject(4)),

                new Testing("""
                            let adder = fn(a, b) { 
                                while(a > 0) {
                                    a = a - 1;
                                    b = b + a;
                                }
                                return b; 
                            };
                            let adders = fn(a, b) { 
                                while(a > 0) {
                                    a = a - 1;
                                    b = b + a;
                                }
                                return b; 
                            };
                            let add = fn(a, b) { return adder(a,b)+adders(a,b)*3; };
                            let result = add(2, 3);
                            ""","result",new IntegerObject(16)),
                new Testing("""
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
                ""","x",new IntegerObject(2)),
                new Testing("""
                         let x = {5:3,"m":5,true:"mas"};
                         let y = x[true];
                        ""","y",new StringObject("mas")),
                new Testing("""
                         let x = [5,"m",3,true,"bas"];
                         let y = x[0];
                        ""","y",new IntegerObject(5)),
                new Testing("""
                         let x = [5,"m",3,true,"bas"];
                         let y = x[4];
                        ""","y",new StringObject("bas")),
                new Testing("""
                          let x = {5:3,"m":5,true:"mas"};
                         let y = x["m"];
                        ""","y",   new IntegerObject(5))


        };
        try {
            for (Testing testing : testings) {
                testArithmeticExpression(testing.input,testing.variable,testing.myresult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private record Testing(String input,String variable,Object myresult){}
}