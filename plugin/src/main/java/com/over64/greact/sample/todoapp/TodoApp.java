package com.over64.greact.sample.todoapp;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.over64.greact.GReact.classIf;

public class TodoApp implements Component<section> {
    public static class Todo {
        public String title;
        public boolean completed = false;

        public Todo(String title) {
            this.title = title;
        }
    }

    enum Mode {ALL, ACTIVE, COMPLETED}

    List<Todo> list;
    String newTodo = "";
    Mode mode = Mode.ALL;

    void add(String title) {
        effect(list.add(new Todo(title)));
    }

    boolean allDone() {
        return list.stream().allMatch(td -> td.completed);
    }

    List<Todo> forRender() {
        return list.stream().filter(item -> switch (mode) {
            case ALL -> true;
            case ACTIVE -> !item.completed;
            case COMPLETED -> item.completed;
        }).collect(Collectors.toList());
    }

    void removeOne(Todo item) {
        effect(list = list.stream()
            .filter((it) -> it.title.equals(item.title))
            .collect(Collectors.toList()));
    }

    void removeCompleted() {
        effect(list = list.stream()
            .filter(item -> !item.completed)
            .collect(Collectors.toList()));
    }


    @Override
    public section mount() {
        list = new ArrayList<>();

//        window.addEventListener("onhashchange", event ->
//            effect(this.mode = Mode.valueOf(
//                window.location.hash.replaceAll("#/?", ""))));

        return new section() {{
            className = "todoapp";
            new header() {{
                className = "header";
                new h1("todos");
                new input() {{
                    className = "new-todo";
                    autofocus = true;
                    autocomplete = Autocomplete.OFF;
                    placeholder = "What needs to be done?";
                    value = newTodo;
                    onchange = (v) -> newTodo = v;
                    onkeyup = (k) -> {
                        if (k == Key.ENTER) add(newTodo);
                    };
                }};
            }};

            if (!list.isEmpty()) {
                new section() {{
                    className = "main";
                    new input() {{
                        id = "toggle-all";
                        className = "toggle-all";
                        type = InputType.CHECKBOX;
                        value = "" + allDone();
                    }};
                    new label() {{
                        _for = "toggle-all";
                    }};
                    new ul() {{
                        className = "todo-list";
                        for (var item : forRender())
                            new TodoItem(item) {{
                                onRemoved = () -> removeOne(item);
                                onChanged = () -> effect(list);
                            }};
                    }};
                }};
                new footer() {{
                    className = "footer";
                    new span() {{
                        var remaining = list.stream()
                            .filter(item -> !item.completed).count();
                        new strong("" + remaining);
                        new span((remaining == 1 ? "item" : "items") + "left");
                    }};
                    new ul() {{
                        new li() {{
                            new a("All") {{
                                className = classIf(mode == Mode.ALL, "selected");
                                href = "#/all";
                            }};
                        }};
                        new li() {{
                            new a("Active") {{
                                className = classIf(mode == Mode.ACTIVE, "selected");
                                href = "#/active";
                            }};
                        }};
                        new li() {{
                            new a("Completed") {{
                                className = classIf(mode == Mode.COMPLETED, "selected");
                                href = "#/completed";
                            }};
                        }};
                    }};
                    // FIXME: if we have completed
                    new button("Clear completed") {{
                        className = "clear-completed";
                        onclick = TodoApp.this::removeCompleted;
                    }};
                }};
            }
        }};
    }
}