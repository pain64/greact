package com.over64.greact.sample.todoapp;

import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static com.over64.greact.dom.HTMLNativeElements.style.id;
import static com.over64.greact.dom.Utils.classIf;


public class TodoApp implements Component0<section> {
    enum Mode {All, Active, Completed}
    static class Todo {
        String title;
        boolean completed = false;

        Todo(String title) { this.title = title; }
    }

    static class TodoItem implements Component0<li> {
        @FunctionalInterface public interface Handler {
            void apply();
        }

        public Handler onChanged = () -> { };
        public Handler onRemoved = () -> { };

        final Todo item;
        boolean editing = false;
        String newTitle = "";
        String oldTitle = "";

        public TodoItem(Todo item) { this.item = item; }

        void startEdit() {
            newTitle = item.title;
            oldTitle = item.title;
            effect(editing = true);
        }

        void doneEdit() {
            item.title = newTitle.trim();
            effect(editing = false);
            onChanged.apply();
        }

        void cancelEdit() {
            item.title = oldTitle;
            effect(editing = false);
        }

        @Override public li mount() {
            return new li() {{
                className = "todo " +
                    classIf(item.completed, id("completed")) +
                    classIf(editing, id("editing"));

                new div() {{
                    className = "view";
                    new input() {{
                        className = "toggle";
                        type = InputType.CHECKBOX;
                        value = "" + item.completed;
                        onchange = ev -> {
                            effect(item.completed = Boolean.parseBoolean(ev.target.value));
                            onChanged.apply();
                        };
                        new label(item.title);
                        new button() {{
                            className = "destroy";
                            onclick = ev -> onRemoved.apply();
                        }};
                    }};
                }};
                new input() {{
                    className = "edit";
                    type = InputType.TEXT;
                    value = item.title;
                    onchange = ev -> item.title = ev.target.value;
//                    ondblclick = ev -> startEdit();
//                    onblur = ev -> doneEdit();
                    onkeyup = key -> {
                        if (key == Key.ENTER) doneEdit();
                        else if (key == Key.ESC) cancelEdit();
                    };
                }};
            }};
        }
    }


    List<Todo> list;
    String newTodo = "";
    Mode mode = Mode.All;

    void add(String title) {
        effect(list.add(new Todo(title)));
    }

    boolean allDone() {
        return list.stream().allMatch(td -> td.completed);
    }

    List<Todo> forRender() {
        return list.stream().filter(item -> switch (mode) {
            case All -> true;
            case Active -> !item.completed;
            case Completed -> item.completed;
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
                    onchange = ev -> newTodo = ev.target.value;
                    onkeyup = k -> { if (k == Key.ENTER) add(newTodo); };
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
                    new label() {{ _for = "toggle-all"; }};
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
                        new span((remaining == 1 ? "item" : "items") + " left");
                    }};
                    new ul() {{
                        for (var the : Mode.values())
                            new li() {{
                                new a(the.name()) {{
                                    className = classIf(the == mode, "selected");
                                    href = "#/" + the.name().toLowerCase();
                                }};
                            }};
                    }};
                    // FIXME: if we have completed
                    new button("Clear completed") {{
                        className = "clear-completed";
                        onclick = ev -> removeCompleted();
                    }};
                }};
            }
        }};
    }
}