package main;

import antlr4.js.ECMAScriptParser;
import antlr4.js.ECMAScriptBaseListener;

public class ECMAWorker extends ECMAScriptBaseListener {
    public void enterProgram(ECMAScriptParser.ProgramContext ctx) {
        System.out.println(ctx);
        System.out.println("Entering: " + ctx.getText());
    }

    public void exitProgram(ECMAScriptParser.ProgramContext ctx) {
        System.out.println("Exiting:" + ctx.getText());
    }
}