package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.Consumer;

class GridFilter<T> implements Component0<div> {
    int[] pageSizes = new int[]{10, 20, 50, 100}; // FIXME: move to config ???
    int currentPage = 1;
    int currentSize = pageSizes[0];
    boolean filterEnabled = false;
    String filterValue = "";
    T[] pageData;

    final T[] data;
    final GridConfig2<T> conf;
    final Consumer<T> onRowSelect;

    GridFilter(T[] data, GridConfig2<T> conf, Consumer<T> onRowSelect) {
        this.data = data;
        this.conf = conf;
        this.onRowSelect = onRowSelect;
    }

    static <T> int calcNPages(T[] filtered, int currentSize) {
        var n = JSExpression.<Integer>of("Math.floor(filtered.length / currentSize)");
        if (filtered.length % currentSize != 0) return n + 1;
        else return n;
    }

    static int switchPage(int curr, int nPages, int diff) {
        var newCurrent = curr + diff;
        if (newCurrent < 1) return 1;
        if (newCurrent > nPages) return nPages;
        return newCurrent;
    }

    String[] stringSplit(String str, String delim) {
        return JSExpression.of("str.split(delim)");
    }
    int stringLength(String str) {
        return JSExpression.of("str.length");
    }

    void effectUnaffectedMe(Runnable ef) {
        ef.run();
    }

    @Override public div mount() {
        return new div() {{
            new div() {{
                var filterWords = Array.filter(
                        stringSplit(filterValue, " "),
                        s -> stringLength(s) != 0);

                T[] filtered = filterWords.length != 0 ? eval(data, parse(lex(filterValue), 0).token) : data;

                var nPages = calcNPages(filtered, currentSize);
                var offset = (currentPage - 1) * currentSize;
                effectUnaffectedMe(() ->
                        effect(pageData = JSExpression.<T[]>of("filtered.slice(offset, offset + this.currentSize)")));

                if (filtered.length > pageSizes[0] || conf.title != null)
                    new div() {{
                        className = "grid-filter";

                        new div() {{
                            if (filtered.length > pageSizes[0]) {
                                new select() {{
                                    onchange = ev -> {
                                        // FIXME: move to one effect
                                        effect(currentSize = Integer.parseInt(
                                                ((select) ev.target).value));
                                        effect(currentPage = 1);
                                    };

                                    for (var size : pageSizes)
                                        new option("" + size) {{
                                            value = "" + size;
                                            selected = size == currentSize;
                                        }};
                                    className = "grid-filter-select";
                                }};

                                new span("записей на странице " + currentPage + " из " + nPages);
                            }
                        }};

                        new span(conf.title) {{
                            className = "grid-filter-span";
                        }};

                        new div() {{
                            if (filtered.length > pageSizes[0]) {
                                new div() {{
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-left"><polyline points="15 18 9 12 15 6"/></svg>
                                        """;
                                    className = "page-turn";
                                    onclick = ev -> effect(currentPage = switchPage(currentPage, nPages, -1));
                                }};
                                new div() {{
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-right"><polyline points="9 18 15 12 9 6"/></svg>
                                        """;
                                    className = "page-turn";
                                    onclick = ev -> effect(currentPage = switchPage(currentPage, nPages, 1));
                                }};
                            }
                        }};
                    }};
            }};
            if (filterEnabled)
                new div() {{
                    className = "grid-filter-enabled";
                    new input() {{
//                        value = filterValue; // one wat bindind
                        placeholder = "фильтр...";
                        className = "grid-filter-input";
                        onkeyup = ev -> effect(filterValue = ((input) ev.target).value);
                    }};
                }};
            new div() {{
                var hint = pageData;
                new slot<>(conf.pageView, new GridTable<>(pageData, conf, onRowSelect, () -> {
                    effect(filterEnabled = !filterEnabled);
                    effect(currentPage = 1);
                    effect(filterValue = "");
                }));
            }};
        }};
    }

    public static void printError(String s, int i) {
        JSExpression.of("console.log(s)");
        throw new ArithmeticException();
    }

    public static class Lexeme {
        String lexeme;
        String value;
        Integer pos;

        Lexeme(String lexeme, String value, Integer pos) {
            this.lexeme = lexeme;
            this.value = value;
            this.pos = pos;
        }
    }

    public static Lexeme[] lex(String expr) {
        var res = new Lexeme[10]; // Array.push(res, acc);
        var acc = "";

        var prev = "";
        var ch = "";
        var next = "";

        for (int i = 0; i < expr.length(); i++) {
            if (i != 0) prev = String.valueOf(expr.charAt(i - 1));
            ch = String.valueOf(expr.charAt(i));
            if (i == expr.length() - 1) next = "";
            else next = String.valueOf(expr.charAt(i + 1));

            switch (ch) {
                case "\\\\":
                case " ":
                    if (prev.equals("\\\\")) acc += ch;
                    break;
                case "%":
                    if (prev.equals("\\\\")) acc += ch;
                    else if ((i == 0 || prev.equals(" ")) && (i == expr.length() - 1 || next.equals(" ")))
                        printError("unexpected %", i);
                    else acc += ".*";
                    break;
                case "&":
                case "|":
                case "(":
                case ")":
                    if (prev.equals("\\\\")) acc += "\\\\" + ch; // escape at regex
                    else {
                        if (!acc.equals("")) {
                            Array.push(res, new Lexeme("SYMBOL", acc, i));
                            acc = "";
                        }
                        var op = switch (ch) {
                            case "&" -> "OP_AND";
                            case "|" -> "OP_OR";
                            case "(" -> "B_OPEN";
                            case ")" -> "B_CLOSE";
                            default -> "";
                        };

                        Array.push(res, new Lexeme(op, ch, i));
                    }
                    break;
                default:  // any other char
                    if (prev.equals("\\\\")) printError("bad escape", i);
                    acc += ch;
                    break;
            }
        }

        if (!acc.equals("")) {
            Array.push(res, new Lexeme("SYMBOL", acc, expr.length() - 1));
        }

        return res;
    }

    public static class Token {
        String kind;
        String expr;
        Token token;
        boolean valueForExpr;

        Token leftToken;
        Token rightToken;

        public Token() {}

        public Token(String kind, String expr) {
            this.kind = kind;
            this.expr = expr;
        }

        public Token(String kind, Token token) {
            this.kind = kind;
            this.token = token;
        }

        public Token(String kind, Token leftToken, Token rightToken) {
            this.kind = kind;
            this.leftToken = leftToken;
            this.rightToken = rightToken;
        }
    }

    public static class Tree {
        int poz;
        Token token;

        public Tree(int poz, Token token) {
            this.poz = poz;
            this.token = token;
        }
    }

    public static Tree parse(Lexeme[] tokens, int i) {
        if(i >= tokens.length) throw new ArithmeticException();
        var current = tokens[i];

        var left = new Token();
        if(tokens[i].lexeme.equals("B_OPEN")) {
            var temp = new Tree(0, new Token());
            JSExpression.of("temp = com_over64_greact_uikit_GridFilter._parse(tokens, i + 1)");
            var nextI = temp.poz;
            var token = temp.token;
            temp = new Tree(0, new Token());
            if(!tokens[nextI].lexeme.equals("B_CLOSE")) throw new ArithmeticException();
            left = new Token("parens", token);
            i = nextI + 1;
        } else if(!current.lexeme.equals("B_CLOSE") &&
                !current.lexeme.equals("OP_AND")  &&
                !current.lexeme.equals("B_OR")) {
            left = new Token ("term", current.value);
            i++;
        }

        if(i >= tokens.length) return new Tree(i, left);

        var operator = tokens[i];
        if(!operator.lexeme.equals("OP_AND") && !operator.lexeme.equals("OP_OR")) return new Tree(i, left);

        var temp = new Tree(0, new Token());
        JSExpression.of("temp = com_over64_greact_uikit_GridFilter._parse(tokens, i + 1)");
        var nextI = temp.poz;
        var right = temp.token;
        temp = new Tree(0, new Token());
        return new Tree(nextI, new Token(operator.lexeme, left, right));
    }

    public T[] eval(T[] data, Token expr) {
        JSExpression.of("console.log(expr)");
        return data;
    }

    // Приоритет операций
    // Обработка ошибок
    // Реализация поиска
}