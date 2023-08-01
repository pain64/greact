package jstack.greact.uikit;

import jstack.greact.html.*;
import jstack.greact.uikit.controls.Select;
import jstack.jscripter.transpiler.model.JSExpression;

import java.util.function.Consumer;

class GridFilter<T> implements Component0<div> {
    Integer[] pageSizes = new Integer[]{10, 20, 50, 100}; // FIXME: move to config ???
    int currentPage = 1;
    int currentSize = pageSizes[0];
    boolean filterEnabled = false;
    String filterValue = "";
    T[] pageData;
    boolean showHint = false;

    final T[] data;
    final GridConfig2<T> conf;
    final Consumer<T> onRowSelect;

    static boolean isError = false;
    static int errorPoz = -1;
    static String errorMessage;

    static class ParseException extends RuntimeException { }

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

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                T[] filtered;
                try {
                    filtered = !filterValue.equals("") ? eval(data, parse(addPriority(lex(filterValue)), 0).token) : data;
                } catch (ParseException e) {
                    filtered = data;
                }

                var nPages = calcNPages(filtered, currentSize);
                var offset = (currentPage - 1) * currentSize;
                effectUnaffectedMe(() ->
                {
                    var x = 1;
                    effect(pageData = JSExpression.<T[]>of("filtered.slice(offset, offset + this.currentSize)"), x = 1);
                });

                if (filtered.length > pageSizes[0] || conf.title != null) {
                    T[] finalFiltered = filtered;
                    T[] finalFiltered1 = filtered;
                    new div() {{
                        className = "grid-filter";

                        new div() {{
                            style.display = "flex";
                            style.alignItems = "center";
                            style.gap = "16px";

                            if (finalFiltered.length > pageSizes[0]) {
                                new Select<>(pageSizes) {{
                                    value = currentSize;
                                    onchange = value -> {
                                        effect(currentSize = value, currentPage = 1);
                                    };
                                }};

                                new span("записей на странице " + currentPage + " из " + nPages);
                            }
                        }};

                        new span(conf.title) {{
                            className = "grid-filter-span";
                        }};

                        new div() {{
                            style.display = "flex";
                            style.alignItems = "center";

                            if (finalFiltered1.length > pageSizes[0]) {
                                new div() {{
                                    new div("grid-i-chevron-left");
                                    className = "page-turn";
                                    onclick = ev -> {
                                        effect(currentPage = switchPage(currentPage, nPages, -1));
                                    };
                                }};
                                new div() {{
                                    new div("grid-i-chevron-right");
                                    className = "page-turn";
                                    onclick = ev -> {
                                        effect(currentPage = switchPage(currentPage, nPages, 1));
                                    };
                                }};
                            }
                        }};
                    }};
                }
            }};
            if (filterEnabled) {
                if (showHint) {
                    new h4("Помощь в использовании фильтра") {{
                        style.color = "#0c3383"; style.paddingLeft = "10px";
                        style.marginBottom = "10px";
                    }};
                    new div() {{
                        style.textAlign = "left";
                        style.padding = "20px";
                        style.backgroundColor = "#dff8ff";
                        new h5("- слово сопоставляется полностью: если ищем слово abc, то ищем полное совпадение (не abcd)");
                        new h5("- % - последовательность любых символов: abc% или %abc или %abc%");
                        new h5("- операторы: & и |") {{ }};
                        new h5("- приоритет задается через скобки: a & (b | (c | d))");
                        new h5("- экранирование спецсимволов: \\\\( или \\\\) или \\\\& или \\\\| или \\\\% или \\\\\\\\  или \\\\пробел");
                        new h5("- ошибки выражения поиска отображаются ниже фильтра");
                    }};
                }
                new div() {{
                    className = "grid-filter-enabled";
                    new input() {{
                        placeholder = "фильтр...";
                        className = "form-control";
                        onkeyup = ev -> {
                            effect(filterValue = ((input) ev.target).value);
                        };
                    }};

                    new div() {{
                        className = "grid-filter-hint";
                        new div("grid-i-question");
                        onclick = ev -> effect(showHint = !showHint);
                    }};
                }};
            }
            new div() {{
                if (isError && filterEnabled && !filterValue.equals("")) {
                    new div() {{
                        new h5(errorMessage) {{
                            this.className = "error";
                            this.style.color = "red";
                        }};
                    }};
                }
                var hint = pageData;
                new slot<>(
                    conf.pageView,
                    new GridTable<>(pageData, conf, filterEnabled, onRowSelect, () -> {
                        effect(filterEnabled = !filterEnabled, currentPage = 1, filterValue = "");
                    })
                );
            }};
        }};
    }

    private Lexeme[] addPriority(Lexeme[] lex) {
        var ind = 0;
        while (ind != -1) {
            ind = -1;
            for (int i = 2; i < lex.length; i++) {
                if (lex[i - 2].lexeme.equals("SYMBOL") && lex[i - 1].lexeme.equals("OP_AND") && lex[i].lexeme.equals("SYMBOL")) {
                    if (i - 3 < 0 || i + 1 == lex.length) {
                        ind = i;
                        break;
                    } else if (!lex[i - 3].lexeme.equals("B_OPEN") || !lex[i + 1].lexeme.equals("B_CLOSE")) {
                        ind = i;
                        break;
                    }
                }
            }
            if (ind != -1) {
                var temp = new Lexeme[lex.length + 2];

                for (int i = 0; i <= ind - 3; i++) {
                    temp[i] = lex[i];
                }

                temp[ind - 2] = new Lexeme("B_OPEN", "(", ind);
                temp[ind - 1] = lex[ind - 2];
                temp[ind] = lex[ind - 1];
                temp[ind + 1] = lex[ind];
                temp[ind + 2] = new Lexeme("B_CLOSE", ")", ind);

                for (int i = ind + 3; i < lex.length + 2; i++) {
                    temp[i] = lex[i - 2];
                }

                lex = temp;
            }
        }
        JSExpression.of("console.log(lex)");
        return lex;
    }

    public static void printError(String s, int i) { // TODO: static calls?
        isError = true;
        errorPoz = i;
        errorMessage = s;
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
                    else if ((i == 0 || prev.equals(" ")) && (i == expr.length() - 1 || next.equals(" "))) {
                        JSExpression.of("jstack_greact_uikit_GridFilter._printError('unexpected %', i)");
                        JSExpression.of("throw new Error()");
                    } else acc += ".*";
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
                    if (prev.equals("\\\\")) {
                        JSExpression.of("jstack_greact_uikit_GridFilter._printError('bad escape', i)");
                        JSExpression.of("throw new Error()");
                    }
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

        Token leftToken;
        Token rightToken;

        public Token() {
        }

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
        if (i >= tokens.length) {
            JSExpression.of("jstack_greact_uikit_GridFilter._printError('Ошибка выражения поиска', i - 1)");
            JSExpression.of("throw new Error()");
        }
        var current = tokens[i];

        var left = new Token();
        if (tokens[i].lexeme.equals("B_OPEN")) {
            var temp = new Tree(0, new Token());
            temp = parse(tokens, i + 1);
            var nextI = temp.poz;
            var token = temp.token;
            temp = new Tree(0, new Token());
            if (!tokens[nextI].lexeme.equals("B_CLOSE")) {
                JSExpression.of("jstack_greact_uikit_GridFilter._printError('Ожидали )', tokens[nextI].pos)");
                JSExpression.of("throw new Error()");
            }
            left = new Token("parens", token);
            i = nextI + 1;
        } else if (!current.lexeme.equals("B_CLOSE") &&
            !current.lexeme.equals("OP_AND") &&
            !current.lexeme.equals("B_OR")) {
            left = new Token("term", current.value);
            i++;
        }

        if (i >= tokens.length) return new Tree(i, left);

        var operator = tokens[i];
        if (!operator.lexeme.equals("OP_AND") && !operator.lexeme.equals("OP_OR"))
            return new Tree(i, left);
        var temp = new Tree(0, new Token());
        temp = parse(tokens, i + 1);
        var nextI = temp.poz;
        var right = temp.token;
        temp = new Tree(0, new Token());
        return new Tree(nextI, new Token(operator.lexeme, left, right));
    }

    public T[] eval(T[] data, Token expr) {
        isError = false;
        return Array.filter(data, v -> evalStatus(v, expr));
    }

    private boolean evalStatus(T strVal, Token expr) {
        switch (expr.kind) {
            case "OP_AND":
                var op1 = evalStatus(strVal, expr.leftToken);
                var op2 = evalStatus(strVal, expr.rightToken);
                return op1 && op2;
            case "OP_OR":
                var opOr1 = evalStatus(strVal, expr.leftToken);
                var opOr2 = evalStatus(strVal, expr.rightToken);
                return opOr1 || opOr2;
            case "parens":
                return evalStatus(strVal, expr.token);
            case "term":
                var flag = false;
                for (var col : conf.columns) {
                    var str = Grid.fetchValue(strVal, col.memberNames);
                    if (str == null) str = "";
                    str += ""; // FIXME: cast to string!!!

                    if (JSExpression.of("expr.expr.startsWith('.*') && expr.expr.endsWith('.*')")) {
                        if (JSExpression.of("str.indexOf(expr.expr.replaceAll('.*', '')) != -1"))
                            flag = true;
                    } else if (JSExpression.of("expr.expr.startsWith('.*')")) {
                        if (JSExpression.of("str.endsWith(expr.expr.replaceAll('.*', ''))"))
                            flag = true;
                    } else if (JSExpression.of("expr.expr.endsWith('.*')")) {
                        if (JSExpression.of("str.startsWith(expr.expr.replaceAll('.*', ''))"))
                            flag = true;
                    } else {
                        if (JSExpression.of("str === expr.expr")) flag = true;
                    }
                }
                return flag;
        }
        return false;
    }
}