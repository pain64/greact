package com.over64.greact.jsx;


import com.over64.greact.jsx.lib.antlr4.js.ECMAScriptBaseListener;
import com.over64.greact.jsx.lib.antlr4.js.ECMAScriptParser;

public class ECMAWorker extends ECMAScriptBaseListener {
    public void enterProgram(ECMAScriptParser.ProgramContext ctx) {
        System.out.println(ctx);
        System.out.println("Entering: " + ctx.getText());
    }

    public void exitProgram(ECMAScriptParser.ProgramContext ctx) {
        System.out.println("Exiting:" + ctx.getText());
    }
}