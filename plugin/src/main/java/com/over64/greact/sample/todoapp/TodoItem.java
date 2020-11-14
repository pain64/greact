package com.over64.greact.sample.todoapp;

import com.over64.greact.GReact;
import com.over64.greact.model.components.Component;
import com.over64.greact.model.components.HTMLNativeElements.*;
import org.over64.jscripter.std.js.HTMLElement;

import static com.over64.greact.GReact.classIf;
import static com.over64.greact.GReact.effect;
import static com.over64.greact.sample.todoapp.TodoApp.Todo;

public class TodoItem implements Component {
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

    @Override public void mount(HTMLElement dom) {
        GReact.mount(dom, new li() {{
            className = "todo " +
                classIf(item.completed, "completed") +
                classIf(editing, "editing");

            new div() {{
                className = "view";
                new input() {{
                    className = "toggle";
                    type = InputType.CHECKBOX;
                    value = "" + item.completed;
                    onchange = (is) -> {
                        effect(item.completed = Boolean.parseBoolean(is));
                        onChanged.apply();
                    };

                    new label(item.title);
                    new button() {{
                        className = "destroy";
                        onclick = () -> onRemoved.apply();
                    }};
                    new input() {{
                        className = "edit";
                        type = InputType.TEXT;
                        value = item.title;
                        onchange = (s) -> item.title = s;
                        ondblclick = TodoItem.this::startEdit;
                        onblur = TodoItem.this::doneEdit;
                        onkeyup = (key) -> {
                            if (key == Key.ENTER) doneEdit();
                            else if (key == Key.ESC) cancelEdit();
                        };
                    }};
                }};
            }};
        }});
    }
}
