package todoapp;

import quickstart.Component;
import quickstart.Handle;
import quickstart.Param;

import static quickstart.Utils.effect;
import static quickstart.server.TodoService.Todo;

/**
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
 */
@Component class TodoItem {
    @Param Todo td;
    @Param Handle onEdited = Handle::nop;
    @Param Handle onRemoved = Handle::nop;

    boolean editing = false;
    String oldTitle;
    String newTitle;

    void startEdit() {
        newTitle = td.title;
        oldTitle = td.title;
        effect(editing = true);
    }

    void doneEdit() {
        td.title = newTitle.trim();
        onEdited.apply();
        effect(editing = false);
    }

    void cancelEdit() {
        td.title = oldTitle;
        effect(editing = false);
    }
}
