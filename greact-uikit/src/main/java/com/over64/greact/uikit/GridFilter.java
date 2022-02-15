package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.Objects;
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

    public static int likeSplit = -1;

    public T[] getFilteredData(T[] data, ArrayList<Lexeme> opz) {
        data = Array.filter(data, v -> {
            var newOpz = new OPZSave(opz);
            for (int i = 0; i < newOpz.getSize(); i++) {
                if (newOpz.get(i).lexeme.equals("SYMBOL")) {
                    newOpz.get(i).value = deleteEcr(newOpz.get(i).value);
                }
            }
            if (newOpz.getSize() == 1) {
                var expr = newOpz.get(0).value;
                var type = getLikeType(expr);

                for (var col : conf.columns) {
                    var strVal = Grid.fetchValue(v, col.memberNames);
                    if (strVal == null) strVal = "";
                    strVal += "";
                    if (type == 1) {
                        expr = deleteLike(expr);
                        JSExpression.of("expr = expr.replace('\\%', '%')");
                        if (JSExpression.<Boolean>of("strVal.startsWith(expr)")) {
                            return true;
                        }
                    } else if (type == 2) {
                        expr = deleteLike(expr);
                        JSExpression.of("expr = expr.replace('\\%', '%')");
                        if (JSExpression.<Boolean>of("strVal.endsWith(expr)")) {
                            return true;
                        }
                    } else if (type == 3) {
                        expr = deleteLike(expr);
                        JSExpression.of("expr = expr.replace('\\%', '%')");
                        if (JSExpression.<Boolean>of("strVal.includes(expr)")) {
                            return true;
                        }
                    } else if (type == 4) {
                        JSExpression.of("var expr1 = expr.substring(0, com$over64$greact$uikit$GridFilter.likeSplit)");
                        JSExpression.of("var expr2 = expr.substring(com$over64$greact$uikit$GridFilter.likeSplit + 1, expr.length)");
                        JSExpression.of("expr1 = expr1.replace('\\%', '%')");
                        JSExpression.of("expr2 = expr2.replace('\\%', '%')");
                        if (JSExpression.<Boolean>of("strVal.startsWith(expr1) && strVal.endsWith(expr2)")) {
                            return true;
                        }
                    } else {
                        JSExpression.of("expr = expr.replace('\\%', '%')");
                        if (JSExpression.<Boolean>of("strVal == expr")) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                while (newOpz.getSize() != 1) {
                    for (int i = 2; i < newOpz.getSize(); i++) {
                        if (newOpz.checkById(i - 2, i - 1, i)) {
                            var flag1 = false;
                            var val1 = newOpz.get(i - 2).value;
                            var type = getLikeType(val1);
                            if (newOpz.get(i - 2).pos == -1) flag1 = true;
                            else if (newOpz.get(i - 2).pos == -2) flag1 = false;
                            else {
                                flag1 = isFlag1(v, flag1, val1, type);
                            }
                            var flag2 = false;
                            val1 = newOpz.get(i - 1).value;
                            type = getLikeType(val1);
                            if (newOpz.get(i - 1).pos == -1) flag2 = true;
                            else if (newOpz.get(i - 1).pos == -2) flag2 = false;
                            else {
                                flag2 = isFlag1(v, flag2, val1, type);
                            }

                            var flagEnd = true;
                            if (newOpz.get(i).value.equals("&")) flagEnd = flag1 && flag2;
                            else flagEnd = flag1 || flag2;

                            newOpz.deleteById(i);
                            newOpz.deleteById(i - 1);
                            newOpz.get(i - 2).pos = flagEnd ? -1 : -2;
                            break;
                        }
                    }
                }
                return newOpz.get(0).pos == -1;
            }
        });

        return data;
    }

    public boolean isFlag1(T v, boolean flag1, String val1, int type) {
        for (var col : conf.columns) {
            var strVal = Grid.fetchValue(v, col.memberNames);
            if (strVal == null) strVal = "";
            strVal += "";
            if (type == 1) {
                val1 = deleteLike(val1);
                JSExpression.of("val1 = val1.replace('\\%', '%')");
                if (JSExpression.<Boolean>of("strVal.startsWith(val1)")) {
                    flag1 = true;
                }
            } else if (type == 2) {
                val1 = deleteLike(val1);
                JSExpression.of("val1 = val1.replace('\\%', '%')");
                if (JSExpression.<Boolean>of("strVal.endsWith(val1)")) {
                    flag1 = true;
                }
            } else if (type == 3) {
                val1 = deleteLike(val1);
                JSExpression.of("val1 = val1.replace('\\%', '%')");
                if (JSExpression.<Boolean>of("strVal.includes(val1)")) {
                    flag1 = true;
                }
            } else if (type == 4) {
                JSExpression.of("var expr1 = val1.substring(0, com$over64$greact$uikit$GridFilter.likeSplit)");
                JSExpression.of("var expr2 = val1.substring(com$over64$greact$uikit$GridFilter.likeSplit + 1, val1.length)");
                JSExpression.of("expr1 = expr1.replace('\\%', '%')");
                JSExpression.of("expr2 = expr2.replace('\\%', '%')");
                if (JSExpression.<Boolean>of("strVal.startsWith(expr1) && strVal.endsWith(expr2)")) {
                    flag1 = true;
                }
            } else {
                JSExpression.of("val1 = val1.replace('\\%', '%')");
                if (JSExpression.<Boolean>of("strVal == val1")) {
                    flag1 = true;
                }
            }
        }
        return flag1;
    }

    public static String deleteLike(String expr) {
        var result = "";
        if (expr.charAt(0) != '%') result += expr.charAt(0);
        for (int i = 1; i < expr.length(); i++) {
            if (expr.charAt(i) == '%' && !String.valueOf(expr.charAt(i - 1)).equals("\\\\")) {
            } else {
                result += expr.charAt(i);
            }
        }
        return result;
    }

    private int getLikeType(String expr) { // %\\%
        if (expr.charAt(0) == '%' && expr.length() >= 2 && expr.charAt(expr.length() - 1) == '%' && !String.valueOf(expr.charAt(expr.length() - 2)).equals("\\\\")) {
            return 3;
        }
        if (expr.charAt(0) == '%') return 2;
        if (expr.length() >= 2 && expr.charAt(expr.length() - 1) == '%' && !String.valueOf(expr.charAt(expr.length() - 2)).equals("\\\\")) {
            return 1;
        }

        for (int i = 1; i < expr.length(); i++) {
            if (expr.charAt(i) == '%' && !String.valueOf(expr.charAt(i - 1)).equals("\\\\")) {
                likeSplit = i;
                return 4;
            }
        }

        return 0;
    }

    public static String deleteEcr(String value) {
        JSExpression.of("value = value.replace('\\&', '&')");
        JSExpression.of("value = value.replace('\\(', '(')");
        JSExpression.of("value = value.replace('\\)', ')')");
        JSExpression.of("value = value.replace('\\|', '|')");
        JSExpression.of("value = value.replace('\\ ', ' ')");
        JSExpression.of("value = value.replace('\\\\', '\\')");
        return value;
    }

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                String reg = filterValue;
                flag_ = checkValid(reg);

                T[] filtered = !filterValue.isEmpty() && flag_ ?
                        getFilteredData(data, OPZ) : data;

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
                if (!flag_ && filterEnabled && !filterValue.equals("")) {
                    new div() {{
                        new h5(errorText) {{
                            this.className = "error";
                            this.style.color = "red";
                        }};
                    }};
                }
                var hint = pageData;
                new slot<>(conf.pageView, new GridTable<>(pageData, conf, onRowSelect, () -> {
                    effect(filterEnabled = !filterEnabled);
                    effect(currentPage = 1);
                    effect(filterValue = "");
                }));
            }};
        }};
    }

    public static class Lexeme {
        String lexeme;
        String value;
        public Integer pos;

        Lexeme(String lexeme, String value, Integer pos) {
            this.lexeme = lexeme;
            this.value = value;
            this.pos = pos;
        }

        Lexeme(String lexeme, Character value, Integer pos) {
            this.lexeme = lexeme;
            this.value = value.toString();
            this.pos = pos;
        }

        @Override
        public String toString() {
            return this.lexeme + " : " + this.value;
        }
    }

    public static class ArrayList<T> {
        private final int INIT_SIZE = 16;
        private final int CUT_RATE = 4;
        private Object[] array = new Object[INIT_SIZE];
        private int pointer = 0;

        public void add(T item) {
            if (pointer == array.length - 1)
                resize(array.length * 2);
            array[pointer++] = item;
        }

        public T get(int index) {
            return (T) array[index];
        }

        public void remove(int index) {
            for (int i = index; i < pointer; i++)
                array[i] = array[i + 1];
            array[pointer] = null;
            pointer--;
            if (array.length > INIT_SIZE && pointer < array.length / CUT_RATE)
                resize(array.length / 2);
        }

        public int size() {
            return pointer;
        }

        private void resize(int newLength) {
            Object[] newArray = new Object[newLength];
            for (int i = 0; i < pointer; i++) {
                newArray[i] = array[i];
            }
            array = newArray;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        public boolean contains(T pattern) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).equals(pattern)) return true;
            }
            return false;
        }

        public void addAll(ArrayList<T> terms) {
            for (int i = 0; i < terms.size(); i++) {
                this.add(terms.get(i));
            }
        }

        public void swap(int i1, int i2) {
            Object temp = array[i1];
            array[i1] = array[i2];
            array[i2] = temp;
        }
    }

    public ArrayList<Lexeme> sortByLexemeId(ArrayList<Lexeme> t) {
        var sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < t.size() - 1; i++) {
                if (t.get(i).pos > t.get(i + 1).pos) {
                    t.swap(i, i + 1);
                    sorted = false;
                }
            }
        }
        return t;
    }

    public static class StringBuilder {
        String value;

        public StringBuilder(String value) {
            this.value = value;
        }

        public StringBuilder() {
            this.value = "";
        }

        public int indexOf(String like) {
            for (int i = 0; i < value.length(); i++) {
                if (String.valueOf(value.charAt(i)).equals(like)) return i;
            }
            return -1;
        }

        public int length() {
            return value.length();
        }

        public boolean isEmpty() {
            return value.length() == 0;
        }

        public char charAt(int i) {
            return value.charAt(i);
        }

        public void append(char charAt) {
            value += charAt;
        }

        public void deleteCharAt(int i) {
            var s1 = new StringBuilder(value).subSequence(0, i).toString();
            var s2 = new StringBuilder(value).subSequence(i + 1, value.length()).toString();
            value = s1 + s2;
        }

        public StringBuilder subSequence(int i, int i1) {
            var newVal = "";
            for (int j = i; j < i1; j++) {
                newVal += value.charAt(j);
            }
            return new StringBuilder(newVal);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static boolean flag_ = false;
    static String errorText = "";
    static int errorPos = 0;

    public static ArrayList<Lexeme> OPZ;

    public void printError(String text, Integer pos) {
        errorText = text;
        errorPos = pos;
    }

    public static class Pattern {
        String el1;
        String el2;

        @Override
        public boolean equals(Object o) {
            Pattern pattern = (Pattern) o;
            return el1.equals(pattern.el1) && el2.equals(pattern.el2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(el1, el2);
        }

        public Pattern(String el1, String el2) {
            this.el1 = el1;
            this.el2 = el2;
        }
    }

    public static class StackLexeme {
        public ArrayList<Lexeme> stack;

        StackLexeme() {
            stack = new ArrayList<>();
        }

        public boolean isEmpty() {
            return stack.size() == 0;
        }

        public void add(Lexeme lexeme) {
            stack.add(lexeme);
        }

        public Lexeme peek() {
            return stack.get(stack.size() - 1);
        }

        public Lexeme pop() {
            var temp = peek();
            stack.remove(stack.size() - 1);
            return temp;
        }
    }

    public boolean checkValid(String expr) {
        var data = lexAnalyze(expr);
        if (data.isEmpty()) return false;

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).lexeme.equals("SYMBOL")) {
                var controlSym = data.get(i).value.length();
                for (int j = 0; j < data.get(i).value.length() - 1; ) {
                    if (String.valueOf(data.get(i).value.charAt(j)).equals("\\\\")) {
                        var char_ = String.valueOf(data.get(i).value.charAt(j + 1));
                        if (!char_.equals(" ") && !char_.equals("&") && !char_.equals("|") && !char_.equals("(") && !char_.equals(")") && !char_.equals("%") && !char_.equals("\\\\")) {
                            printError("Ошибка в выражении с обратным слешем - видимо этот символ нельзя экранировать", 0);
                            return false;
                        }
                        j += 2;
                        controlSym -= 2;
                    } else {
                        j++;
                        controlSym--;
                    }
                }
                if (controlSym != 0 && String.valueOf(data.get(i).value.charAt(data.get(i).value.length() - 1)).equals("\\\\")) {
                    printError("Ошибка в выражении с обратным слешем - видимо этот символ нельзя экранировать", 0);
                    return false;
                }

                if (data.get(i).value.length() >= 2) {
                    if (String.valueOf(data.get(i).value.charAt(data.get(i).value.length() - 1)).equals("\\\\")) {
                        var char_ = String.valueOf(data.get(i).value.charAt(data.get(i).value.length() - 2));
                        if (!char_.equals(" ") && !char_.equals("&") && !char_.equals("|") && !char_.equals("(") && !char_.equals(")") && !char_.equals("%") && !char_.equals("\\\\")) {
                            printError("В конце объявлен лишний обратный слеш", 0);
                            return false;
                        }
                    }
                }

                if (data.get(i).value.equals("\\\\")) {
                    printError("Выражение не может состоять из обратного слеша", 0);
                    return false;
                }
            }
        }
        var patternsArr = new ArrayList<Pattern>();
        patternsArr.add(new Pattern("OP_AND", "B_CLOSE"));
        patternsArr.add(new Pattern("OP_OR", "B_CLOSE"));
        patternsArr.add(new Pattern("B_OPEN", "B_CLOSE"));
        patternsArr.add(new Pattern("B_CLOSE", "B_OPEN"));
        patternsArr.add(new Pattern("B_OPEN", "OP_AND"));
        patternsArr.add(new Pattern("B_OPEN", "OP_OR"));
        patternsArr.add(new Pattern("OP_OR", "OP_OR"));
        patternsArr.add(new Pattern("OP_OR", "OP_AND"));
        patternsArr.add(new Pattern("OP_AND", "OP_AND"));
        patternsArr.add(new Pattern("OP_AND", "OP_OR"));
        patternsArr.add(new Pattern("SYMBOL", "SYMBOL"));

        Lexeme old = null;
        for (int i = 0; i < data.size(); i++) {
            Lexeme lex = data.get(i);
            if (old != null) {
                var flag = false;
                for (int j = 0; j < patternsArr.size(); j++) {
                    if (patternsArr.get(j).equals(new Pattern(old.lexeme, lex.lexeme))) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    printError("Ошибка в подряд идущих операторах", lex.pos);
                    return false;
                }
            }
            old = lex;
        }

        for (int i = 0; i < data.size(); i++) {
            Lexeme lex = data.get(i);
            if (lex.lexeme.equals("OP_AND")) {
                if (!"&".equals(lex.value)) {
                    printError("Ожидалось '&', а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme.equals("OP_OR")) {
                if (!"|".equals(lex.value)) {
                    printError("Ожидалось |, а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme.equals("B_OPEN")) {
                if (!"(".equals(lex.value)) {
                    printError("Ожидалось (, а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme.equals("B_CLOSE")) {
                if (!")".equals(lex.value)) {
                    printError("Ожидалось ), а имеем " + lex.value, lex.pos);
                    return false;
                }
            } else if (lex.lexeme.equals("SYMBOL")) {
                var like = "%";
                if (new StringBuilder(lex.value).indexOf(like) != -1) {
                    if (lex.value.length() == 1) {
                        printError("Ожидалось выражение, а получили %", lex.pos);
                        return false;
                    }
                    ArrayList<Integer> likePos = getLikePos(lex.value);
                    if (likePos.size() == 2) {
                        if (lex.value.equals("%%")) {
                            printError("Операторы like не могут стоять подряд", 0);
                            return false;
                        }
                        if (abs(likePos.get(0) - likePos.get(1)) != lex.value.length() - 1) {
                            printError("Неверное расположение операторов like", likePos.get(0));
                            return false;
                        }
                    } else if (likePos.size() > 2) {
                        printError("Слишком много операторов like", likePos.get(0));
                        return false;
                    }
                }
            } else if (lex.lexeme.equals("ERROR")) {
                printError(lex.value, 0);
                return false;
            } else {
                printError("Неизвестный тип оператора", lex.pos);
                return false;
            }
        }

        var calStack = new StackLexeme();
        OPZ = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            Lexeme lexeme = data.get(i);
            if (lexeme.lexeme.equals("SYMBOL")) {
                OPZ.add(lexeme);
            } else if (lexeme.lexeme.equals("OP_AND")) {
                calStack.add(lexeme);
            } else if (lexeme.lexeme.equals("OP_OR")) {
                if (calStack.isEmpty() || calStack.peek().lexeme.equals("B_OPEN") || calStack.peek().lexeme.equals("OP_OR"))
                    calStack.add(lexeme);
                else {
                    while (true) {
                        if (calStack.isEmpty()) break;
                        var nextLex = calStack.peek();
                        if (nextLex.lexeme.equals("OP_AND")) {
                            OPZ.add(calStack.pop());
                        } else {
                            break;
                        }
                    }
                    calStack.add(lexeme);
                }
            } else if (lexeme.lexeme.equals("B_OPEN")) {
                calStack.add(lexeme);
            } else if (lexeme.lexeme.equals("B_CLOSE")) {
                while (true) {
                    if (calStack.isEmpty()) {
                        printError("Неверно расположены скобки", 0);
                        return false;
                    }
                    var lexEl = calStack.pop();
                    if (lexEl.lexeme.equals("B_OPEN")) break;
                    OPZ.add(lexEl);
                }
            } else {
                printError("Неопознанный тип", 0);
                return false;
            }
        }

        while (!calStack.isEmpty()) {
            if (calStack.peek().lexeme.equals("B_CLOSE") || calStack.peek().lexeme.equals("B_OPEN")) {
                printError("Неверно расположены скобки", 0);
                return false;
            }
            OPZ.add(calStack.pop());
        }

        if (OPZ.size() == 1 && !OPZ.get(0).lexeme.equals("SYMBOL")) {
            printError("Ожидалось высказывание, а имеем оператор", OPZ.get(0).pos);
            return false;
        } else if (OPZ.size() == 2 || OPZ.size() == 0) {
            printError("Выражение не может состоять из двух элементов", 0);
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

            if (newOpz.getSize() != 1 || !newOpz.get(0).lexeme.equals("SYMBOL")) {
                printError("Выражение не валидно. Пожалуйста прочтите вкладку Помощь", 0);
                return false;
            }
        }
        return true;
    }

    private int abs(int i) {
        if (i >= 0) return i;
        return -1 * i;
    }

    public static class OPZSave {
        ArrayList<Lexeme> data;

        public OPZSave(ArrayList<Lexeme> data) {
            this.data = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                var lexeme = data.get(i);
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
            return get(a1).lexeme.equals("SYMBOL") && get(a2).lexeme.equals("SYMBOL") && (get(a3).lexeme.equals("OP_AND") || get(a3).lexeme.equals("OP_OR"));
        }

        int getSize() {
            return data.size();
        }
    }

    public ArrayList<Integer> getLikePos(String val) {
        var value = new StringBuilder(val);
        var res = new ArrayList<Integer>();
        if (value.charAt(0) == '%') res.add(0);

        for (int i = 1; i < value.length(); i++) {

            if (value.charAt(i) == '%' && !(value.charAt(i - 1) + "" + value.charAt(i)).equals("\\\\%")) res.add(i);
        }

        return res;
    }

    public ArrayList<Lexeme> lexAnalyze(String textExpr) {
        if (textExpr == null) return new ArrayList<>();
        var exprBuilder = new StringBuilder(textExpr);
        var result = new ArrayList<Lexeme>();
        if (textExpr.isEmpty()) return result;
        if (String.valueOf(textExpr.charAt(0)).equals(" ")) {
            result.add(new Lexeme("ERROR", "Текст не должен начинаться с пробела", 0));
            return result;
        }
        if (textExpr.length() == 1) {
            if (textExpr.equals("%") || textExpr.equals(")") || textExpr.equals("(") || textExpr.equals("&") ||
                    textExpr.equals("|")) {
                result.add(new Lexeme("ERROR", "Этот символ не ожидался", 0));
                return result;
            }
            result.add(new Lexeme("SYMBOL", textExpr, 0));
            return result;
        }

        var sp = new String[10];
        var old_ = "";
        JSExpression.of("sp = textExpr.replaceAll('\\ ', 'a').split(' ')");
        for (int i = 0; i < sp.length; i++) {
            if (!sp[i].equals("")) {
                if (!old_.equals("")) {
                    if (!spaceValid(sp[i], old_)) {
                        result.add(new Lexeme("ERROR", "Пробелы внутри фраз необходимо экранировать", 0));
                        return result;
                    }
                }
                old_ = sp[i];
            }
        }

        var spaceArr = new ArrayList<Integer>();
        for (int pos = 0; pos < exprBuilder.length(); pos++) {
            if (exprBuilder.charAt(pos) == ' ' && !((exprBuilder.charAt(pos - 1) + "" + exprBuilder.charAt(pos)).equals("\\\\ ")))
                spaceArr.add(pos);
        }

        var pos = 0;
        for (int i = 0; i < spaceArr.size(); i++) {
            var space = spaceArr.get(i);
            exprBuilder.deleteCharAt(space - pos);
            pos++;
        }
        var rest = new ArrayList<String>();
        rest.add("\\\\&");
        rest.add("\\\\|");
        rest.add("\\\\(");
        rest.add("\\\\)");
        rest.add("\\\\ ");
        var specialSym = findAllWithLengthTwo(exprBuilder, rest);

        var splitTermInd = new ArrayList<Integer>();
        for (int i = 0; i < exprBuilder.length(); i++) {
            if (!specialSym.contains(i)) {
                if (exprBuilder.charAt(i) == '&') {
                    splitTermInd.add(i);
                    result.add(new Lexeme("OP_AND", '&', i));
                } else if (exprBuilder.charAt(i) == '|') {
                    splitTermInd.add(i);
                    result.add(new Lexeme("OP_OR", '|', i));
                } else if (exprBuilder.charAt(i) == '(') {
                    splitTermInd.add(i);
                    result.add(new Lexeme("B_OPEN", '(', i));
                } else if (exprBuilder.charAt(i) == ')') {
                    splitTermInd.add(i);
                    result.add(new Lexeme("B_CLOSE", ')', i));
                }
            }
        }

        result.addAll(getTerms(exprBuilder, splitTermInd));
        result = sortByLexemeId(result);

        return result;
    }

    public static boolean spaceValid(String s, String s1) {
        if (s.equals("&") || s.equals("|") || s.equals("(") || s.equals(")")) return true;
        if (s1.equals("&") || s1.equals("|") || s1.equals("(") || s1.equals(")")) return true;
        return false;
    }

    public ArrayList<Lexeme> getTerms(StringBuilder exprBuilder, ArrayList<Integer> splitTermInd) {
        var res = new ArrayList<Lexeme>();
        if (splitTermInd.size() == 0) {
            res.add(new Lexeme("SYMBOL", exprBuilder.toString(), 1));
            return res;
        }

        var local = new StringBuilder();
        for (int i = 0; i < exprBuilder.length(); i++) {
            if (!splitTermInd.contains(i)) local.append(exprBuilder.charAt(i));
            else {
                if (!local.isEmpty()) {
                    res.add(new Lexeme("SYMBOL", local.toString(), i - 1));
                    local = new StringBuilder();
                }
            }
        }

        if (!local.isEmpty()) res.add(new Lexeme("SYMBOL", local.toString(), exprBuilder.length() - 1));

        return res;
    }

    private ArrayList<Integer> findAllWithLengthTwo(StringBuilder exprBuilder, ArrayList<String> data) {
        var res = new ArrayList<Integer>();
        for (int i = 1; i < exprBuilder.length(); i++) {
            if (equalsWithOne(exprBuilder.subSequence(i - 1, i + 1).toString(), data)) res.add(i);
        }
        return res;
    }

    private boolean equalsWithOne(String toString, ArrayList<String> s) {
        for (int i = 0; i < s.size(); i++) {
            var s1 = s.get(i);
            if (s1.equals(toString)) return true;
        }
        return false;
    }
}