package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.*;
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
                var flag_ = checkValid(filterValue);
                JSExpression.of("console.log(flag)");
                var filterWords = Array.filter(
                        stringSplit(filterValue, " "),
                        s -> stringLength(s) != 0);

                T[] filtered = filterWords.length != 0 ?
                        Array.filter(data, v -> {
                            for (var col : conf.columns) {
                                var strVal = Grid.fetchValue(v, col.memberNames);
                                if (strVal == null) strVal = "";
                                strVal += ""; // FIXME: cast to string!!!
                                for (var fVal : filterWords)
                                    if (JSExpression.<Boolean>of("strVal.indexOf(fVal) != -1")) return true;
                            }
                            return false;
                        }) : data;

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
                                        effect(currentSize = Integer.parseInt(ev.target.value));
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

    // Написать свой ArrayList
    // Написать свой StringBuilder
    // Написать свой Stack

    private ArrayList<Lexeme> OPZ;

    private void printError(String text, Integer pos) {

    }

    private class Pattern {
        LexemeTypes el1;
        LexemeTypes el2;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pattern pattern = (Pattern) o;
            return el1 == pattern.el1 && el2 == pattern.el2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(el1, el2);
        }

        public Pattern(LexemeTypes el1, LexemeTypes el2) {
            this.el1 = el1;
            this.el2 = el2;
        }
    }

    private boolean checkValid(String expr) {
        var data = lexAnalyze(expr);
        if (data.isEmpty()) return false;

        // express check ----
        var patternsArr = new ArrayList<Pattern>();
        patternsArr.add(new Pattern(LexemeTypes.OP_AND, LexemeTypes.B_CLOSE));
        patternsArr.add(new Pattern(LexemeTypes.OP_OR, LexemeTypes.B_CLOSE));
        patternsArr.add(new Pattern(LexemeTypes.B_OPEN, LexemeTypes.B_CLOSE));
        patternsArr.add(new Pattern(LexemeTypes.B_CLOSE, LexemeTypes.B_OPEN));
        patternsArr.add(new Pattern(LexemeTypes.B_OPEN, LexemeTypes.OP_AND));
        patternsArr.add(new Pattern(LexemeTypes.B_OPEN, LexemeTypes.OP_OR));
        patternsArr.add(new Pattern(LexemeTypes.OP_OR, LexemeTypes.OP_OR));
        patternsArr.add(new Pattern(LexemeTypes.OP_OR, LexemeTypes.OP_AND));
        patternsArr.add(new Pattern(LexemeTypes.OP_AND, LexemeTypes.OP_AND));
        patternsArr.add(new Pattern(LexemeTypes.OP_AND, LexemeTypes.OP_OR));
        patternsArr.add(new Pattern(LexemeTypes.SYMBOL, LexemeTypes.SYMBOL));

        Lexeme old = null;
        for (Lexeme lex : data) {
            if (old != null) {
                if (patternsArr.contains(new Pattern(old.lexeme, lex.lexeme))) {
                    printError("Ошибка синтаксиса", lex.pos);
                    return false;
                }
            }
            old = lex;
        }
        // -------------

        for (Lexeme lex : data) {
            if (lex.lexeme == LexemeTypes.OP_AND) {
                if (!"&".equals(lex.value)) {
                    printError("Ожидалось '&', а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme == LexemeTypes.OP_OR) {
                if (!"|".equals(lex.value)) {
                    printError("Ожидалось |, а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme == LexemeTypes.B_OPEN) {
                if (!"(".equals(lex.value)) {
                    printError("Ожидалось (, а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme == LexemeTypes.B_CLOSE) {
                if (!")".equals(lex.value)) {
                    printError("Ожидалось ), а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme == LexemeTypes.SYMBOL) {
                var like = "%";
                if (new StringBuilder(lex.value).indexOf(like) != -1) { // BAD
                    if (lex.value.length() == 1) {
                        printError("Ожидалось выражение, а получили %", lex.pos);
                        return false;
                    }
                    // Знаем, что в строке больше одного символа и там содержиться like >=1
                    ArrayList<Integer> likePos = getLikePos(lex.value);
                    if (likePos.size() == 2) {
                        if (lex.value.equals("%%")) {
                            printError("Операторы like не могут стоять подряд", 0);
                            return false;
                        }
                        if (Math.abs(likePos.get(0) - likePos.get(1)) != lex.value.length() - 1) {
                            printError("Неверное расположение операторов like", likePos.get(0));
                            return false;
                        }
                    } else if (likePos.size() > 2) {
                        printError("Слишком много операторов like", likePos.get(0));
                        return false;
                    }
                }
            } else if (lex.lexeme == LexemeTypes.ERROR) {
                printError("Запрос не должен начинаться с пробела", 0);
                return false;
            } else {
                printError("Неизвестный тип", lex.pos);
                return false;
            }
        }

// Здесь мы знаем, что все <term> and <some_op> валидны, осталось проверить саму последовательность

        var calStack = new Stack<Lexeme>();
        OPZ = new ArrayList<>();

        for (Lexeme lexeme : data) {
            if (lexeme.lexeme == LexemeTypes.SYMBOL) {
                OPZ.add(lexeme);
            } else if (lexeme.lexeme == LexemeTypes.OP_AND) {
                calStack.add(lexeme);
            } else if (lexeme.lexeme == LexemeTypes.OP_OR) {
                if (calStack.isEmpty() || calStack.peek().lexeme == LexemeTypes.B_OPEN || calStack.peek().lexeme == LexemeTypes.OP_OR)
                    calStack.add(lexeme);
                else {
                    while (true) {
                        if (calStack.isEmpty()) break;
                        var nextLex = calStack.peek();
                        if (nextLex.lexeme == LexemeTypes.OP_AND) {
                            OPZ.add(calStack.pop());
                        } else {
                            break;
                        }
                    }
                    calStack.add(lexeme);
                }
            } else if (lexeme.lexeme == LexemeTypes.B_OPEN) {
                calStack.add(lexeme);
            } else if (lexeme.lexeme == LexemeTypes.B_CLOSE) {
                // Выталкиваем все операции, пока не встретим откр. скобку
                while (true) {
                    if (calStack.isEmpty()) {
                        printError("Неверно расположены скобки", 0); // bad
                        return false;
                    }
                    var lexEl = calStack.pop();
                    if (lexEl.lexeme == LexemeTypes.B_OPEN) break;
                    OPZ.add(lexEl);
                }
            } else {
                printError("Неопознанный тип", 0);
                return false;
            }
        }

        while (!calStack.isEmpty()) {
            if (calStack.peek().lexeme == LexemeTypes.B_CLOSE || calStack.peek().lexeme == LexemeTypes.B_OPEN) {
                printError("Неверно расположены скобки", 0);
                return false;
            }
            OPZ.add(calStack.pop());
        }

        // Check OPZ ---
        if (OPZ.size() == 1 && OPZ.get(0).lexeme != LexemeTypes.SYMBOL) {
            printError("Ожидалось высказывание", OPZ.get(0).pos);
            return false;
        } else if (OPZ.size() == 2 || OPZ.size() == 0) {
            printError("Неверное выражение", 0);
            return false;
        } else {
            var newOpz = new OPZSave(OPZ);
            var flag = true;

            while (flag) {
                flag = false;
                if (newOpz.getSize() >= 3) {
                    for (int i = 2; i < newOpz.getSize(); i++) {
                        if (newOpz.checkById(i - 2, i - 1, i)) {
                            newOpz.deleteById(i);
                            newOpz.deleteById(i - 1);
                            flag = true;
                            break;
                        }
                    }
                }
            }

            if (newOpz.getSize() != 1 || newOpz.get(0).lexeme != LexemeTypes.SYMBOL) {
                printError("Ошибка операторов", 0);
                return false;
            }
        }

        return true;
    }

    private class OPZSave {
        ArrayList<Lexeme> data;

        public OPZSave(ArrayList<Lexeme> data) {
            this.data = new ArrayList<>();
            for (Lexeme lexeme : data) {
                this.data.add(new Lexeme(lexeme.lexeme, lexeme.value, 0));
            }
        }

        void deleteById(int id) {
            data.remove(id);
        }

        Lexeme get(int id) {
            return data.get(id);
        }

        boolean checkById(int a1, int a2, int a3) {
            return get(a1).lexeme == LexemeTypes.SYMBOL && get(a2).lexeme == LexemeTypes.SYMBOL && (get(a3).lexeme == LexemeTypes.OP_AND) || get(a3).lexeme == LexemeTypes.OP_OR;
        }

        int getSize() {
            return data.size();
        }
    }

    private ArrayList<Integer> getLikePos(String val) {
        var value = new StringBuilder(val);
        var res = new ArrayList<Integer>();
        if (value.charAt(0) == '%') res.add(0);

        for (int i = 1; i < value.length(); i++) {

            if (value.charAt(i) == '%' && !(value.charAt(i - 1) + "" + value.charAt(i)).equals("\\%")) res.add(i);
        }

        return res;
    }

    private enum LexemeTypes {
        OP_AND,
        OP_OR,
        B_OPEN,
        B_CLOSE,
        SYMBOL,
        ERROR
    }

    private class Lexeme {
        LexemeTypes lexeme;
        String value;
        Integer pos;

        Lexeme(LexemeTypes lexeme, String value, Integer pos) {
            this.lexeme = lexeme;
            this.value = value;
            this.pos = pos;
        }

        Lexeme(LexemeTypes lexeme, Character value, Integer pos) {
            this.lexeme = lexeme;
            this.value = value.toString();
            this.pos = pos;
        }

        @Override
        public String toString() {
            return this.lexeme.toString() + " : " + this.value;
        }
    }

    private List<Lexeme> lexAnalyze(String textExpr) {
        var exprBuilder = new StringBuilder(textExpr);
        var result = new ArrayList<Lexeme>();
        if (textExpr.isEmpty()) return result; // Ошибка в этом случае будет бесить
        if (textExpr.startsWith(" ")) {
            result.add(new Lexeme(LexemeTypes.ERROR, "Текст не должен начинаться с пробела", 0));
            return result;
        }
        if (textExpr.length() == 1) {
            result.add(new Lexeme(LexemeTypes.SYMBOL, textExpr, 0));
            return result;
        }

        var spaceArr = new ArrayList<Integer>();
        for (int pos = 0; pos < exprBuilder.length(); pos++) {
            if (exprBuilder.charAt(pos) == ' ' && !((exprBuilder.charAt(pos - 1) + "" + exprBuilder.charAt(pos)).equals("\\ "))) spaceArr.add(pos);
        }

        var pos = 0;
        for (Integer space : spaceArr) {
            exprBuilder.deleteCharAt(space - pos);
            pos++;
        }
        // На этот момент мы удалили все неэкранированные пробелы знаем что в нашем запросе минимум 2 символа

        var specialSym = new ArrayList<>(findAllWithLengthTwo(exprBuilder, "\\&", "\\|", "\\(", "\\)", "\\ "));
        // Теперь мы знаем индексы всех спецсимволов

        var splitTermInd = new ArrayList<Integer>();
        for (int i = 0; i < exprBuilder.length(); i++) {
            if (!specialSym.contains(i)) {
                if (exprBuilder.charAt(i) == '&') {
                    splitTermInd.add(i);
                    result.add(new Lexeme(LexemeTypes.OP_AND, '&', i));
                } else if (exprBuilder.charAt(i) == '|') {
                    splitTermInd.add(i);
                    result.add(new Lexeme(LexemeTypes.OP_OR, '|', i));
                } else if (exprBuilder.charAt(i) == '(') {
                    splitTermInd.add(i);
                    result.add(new Lexeme(LexemeTypes.B_OPEN, '(', i));
                } else if (exprBuilder.charAt(i) == ')') {
                    splitTermInd.add(i);
                    result.add(new Lexeme(LexemeTypes.B_CLOSE, ')', i));
                }
            }
        }
        // splitTermInd - индексы, разделив по которым мы получим термы
        result.addAll(getTerms(exprBuilder, splitTermInd));
        result.sort(Comparator.comparingInt(a -> a.pos));

        return result;
    }

    private ArrayList<Lexeme> getTerms(StringBuilder exprBuilder, ArrayList<Integer> splitTermInd) {
        var res = new ArrayList<Lexeme>();
        if (splitTermInd.size() == 0) {
            res.add(new Lexeme(LexemeTypes.SYMBOL, exprBuilder.toString(), 1));
            return res;
        }

        var local = new StringBuilder();
        for (int i = 0; i < exprBuilder.length(); i++) {
            if (!splitTermInd.contains(i)) local.append(exprBuilder.charAt(i));
            else {
                if (!local.isEmpty()) {
                    res.add(new Lexeme(LexemeTypes.SYMBOL, local.toString(), i - 1));
                    local = new StringBuilder();
                }
            }
        }

        if (!local.isEmpty()) res.add(new Lexeme(LexemeTypes.SYMBOL, local.toString(), exprBuilder.length() - 1));

        return res;
    }

    private Collection<Integer> findAllWithLengthTwo(StringBuilder exprBuilder, String... s) {
        var res = new ArrayList<Integer>();
        for (int i = 1; i < exprBuilder.length(); i++) {
            if (equalsWithOne(exprBuilder.subSequence(i - 1, i + 1).toString(), s)) res.add(i);
        }
        return res;
    }

    private boolean equalsWithOne(String toString, String[] s) {
        for (String s1 : s) {
            if (s1.equals(toString)) return true;
        }
        return false;
    }
}