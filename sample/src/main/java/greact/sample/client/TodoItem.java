package greact.sample.client;



import greact.sample.Component;

import java.util.Optional;

import static greact.sample.Utils.effect;
import static greact.sample.Utils.render;
import static greact.sample.server.TodoService.Todo;

@Component
class TodoItem {
    final Todo td;
    final Optional<Consumer<TodoItem>> onEdited;
    final Optional<Consumer<TodoItem>> onRemoved;

    boolean editing = false;
    String oldTitle;
    String newTitle;

    public TodoItem(Todo td,
                    Optional<Consumer<TodoItem>> onEdited,
                    Optional<Consumer<TodoItem>> onRemoved) {
        this.td = td;
        this.onEdited = onEdited;
        this.onRemoved = onRemoved;

        render(/**
         *  <li
         *    class="todo"
         *    class="{classIf(td.completed, "completed")}"
         *    class="{classIf(editing, "editing")}"
         *    >
         *    <div class="view">
         *      <input
         *        class="toggle"
         *        type="checkbox"
         *        value="{td.completed}"
         *        onchange="{(is) -> effect(td.completed = is)}"/>
         *      <label>{td.title}</label>
         *      <button onclick="{onRemoved}" class="destroy" />
         *    </div>
         *    <input
         *      class="edit"
         *      type="text"
         *      value="{td.title}"
         *      onchange={s -> effect(td.title = s)}
         *      ondblcick={startEdit}
         *      onblur={doneEdit}
         *      onkeyup.enter={doneEdit}
         *      onkeyup.esc={cancelEdit}
         *    />
         *  </li>
         */);

    }

    void startEdit() {
        newTitle = td.title;
        oldTitle = td.title;
        effect(editing = true);
    }

    void doneEdit() {
        td.title = newTitle.trim();
        onEdited.ifPresent(on -> on.consume(this));
        effect(editing = false);
    }

    void cancelEdit() {
        td.title = oldTitle;
        effect(editing = false);
    }
}
