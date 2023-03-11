package jstack.ssql;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QueryBuilder {

    @AllArgsConstructor public static class PositionalArgument {
        public int javaArgumentIndex;
        public int sqlParameterNumber;
    }

    @AllArgsConstructor public static class FieldArgument<FI> {
        public FI field;
        public int sqlParameterNumber;
    }

    @AllArgsConstructor public static class FieldResult<FI> {
        public int sqlColumnNumber;
        public FI field;
    }

    @AllArgsConstructor public static class PositionalToVoidQuery {
        public String text;
        public List<PositionalArgument> arguments;
    }

    @AllArgsConstructor public static class PositionalToScalarQuery<CI> {
        public String text;
        public List<PositionalArgument> arguments;
        public CI resultClass;
    }

    @AllArgsConstructor public static class PositionalToTupleQuery<FI> {
        public String text;
        public List<PositionalArgument> arguments;
        public List<FieldResult<FI>> results;
    }

    @AllArgsConstructor public static class TupleToTupleQuery<FI> {
        public String text;
        public List<FieldArgument<FI>> arguments;
        public List<FieldResult<FI>> results;
    }

    static String camelToSnakeCase(String str) {
        var result = new StringBuilder();

        result.append(Character.toLowerCase(str.charAt(0)));

        for (int i = 1; i < str.length(); i++) {
            var ch = str.charAt(i);
            if (Character.isUpperCase(ch)) result.append('_').append(Character.toLowerCase(ch));
            else result.append(ch);

        }

        return result.toString();
    }

    static int nDigits(int n) {
        int length = 0;
        long temp = 1;
        while (temp <= n) {
            length++;
            temp *= 10;
        }
        return length;
    }

    record ArgOffset(int argIdx, int offset) { }
    public record ExprAndArguments(String newExpr, List<PositionalArgument> arguments) { }

    public static ExprAndArguments mapQueryArgs(String expr, int argumentCount) {
        var offsets = new ArrayList<ArgOffset>();

        for (var i = argumentCount - 1; i >= 0; i--) {
            for (; ; ) {
                var offset = expr.indexOf(":" + (i + 1));
                var iLength = nDigits(i + 1);
                if (offset == -1) break;
                expr = expr.substring(0, offset) +
                    "?" + " ".repeat(iLength) + // NB! keep offsets
                    expr.substring(offset + iLength + 1);
                offsets.add(new ArgOffset(i, offset));
            }
        }

        offsets.sort(Comparator.comparingInt(ArgOffset::offset));

        var arguments = new ArrayList<PositionalArgument>();
        for (var i = 0; i < offsets.size(); i++)
            arguments.add(new PositionalArgument(offsets.get(i).argIdx, i + 1));

        return new ExprAndArguments(expr, arguments);
    }

    public static PositionalToVoidQuery forExec(String text, int argumentCount) {
        var mapped = mapQueryArgs(text, argumentCount);
        return new PositionalToVoidQuery(mapped.newExpr, mapped.arguments);
    }

    public static <CI> PositionalToScalarQuery<CI> forQueryScalar(
        CI toClass, String expr, int argumentCount
    ) {
        var mapped = mapQueryArgs(expr, argumentCount);
        try {
            return new PositionalToScalarQuery<>(
                mapped.newExpr, mapped.arguments, toClass
            );
        } catch (NoClassDefFoundError ex) {
            System.out.println("NO_CLASS_DEF_FOUND INTERCEPTED");
            throw ex;
        }
    }

    public static <FI> PositionalToTupleQuery<FI> forQueryTuple(
        List<FI> fields, String expr, int argumentCount
    ) {
        var mapped = mapQueryArgs(expr, argumentCount);
        var results = new ArrayList<FieldResult<FI>>();

        for (var i = 0; i < fields.size(); i++)
            results.add(new FieldResult<>(i + 1, fields.get(i)));

        return new PositionalToTupleQuery<>(
            mapped.newExpr, mapped.arguments, results
        );
    }
}
