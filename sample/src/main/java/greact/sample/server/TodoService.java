package greact.sample.server;

import java.util.List;

public class TodoService {
    public static class Todo {
        public String title;
        public boolean completed;

        public Todo(String newTodo, boolean b) {

        }
    }

    static TypesafeSql sql;

    public static List<Todo> load() {
        return sql.list("select * from todo", Todo.class);
    }

    public static void persist(List<Todo> list) {
        list.forEach(todo ->
                sql.exe("insert into todo values($title, $completed)", todo));
    }
}
