package greact.sample.client;


import greact.sample.Component;
import greact.sample.server.TodoService;


import static greact.sample.Utils.*;
import static greact.sample.server.TodoService.Todo;

@Component(require = {TodoItem.class}) class TodoApp {
//    enum Mode {ALL, ACTIVE, COMPLETED}
//
//    Array<Todo> list;
//    String newTodo = "";
//    Mode mode = Mode.ALL;
//
//    void add() {
//        effect(list.push(new Todo(newTodo, false)));
//        TodoService.persist(toList(list));
//    }
//
//    void removeCompleted() {
//        effect(list = list.filter(td -> !td.completed));
//        TodoService.persist(toList(list));
//    }
//
//    Array<Todo> activeList() { return list.filter(td -> td.completed); }
//    boolean allDone() { return list.every(td -> td.completed, null); }
//    int remaining() { return list.filter(td -> !td.completed).length; }
//
//    TodoApp() {
//        list = fromList(TodoService.load());
//        window.addEventListener("onhashchange", event ->
//            effect(this.mode = Mode.valueOf(
//                window.location.hash.replaceAll("#/?", ""))));
//
//        render(/**
//         *     <section class="todoapp">
//         *       <header class="header">
//         *         <h1>todos</h1>
//         *         <input
//         *           class="new-todo"
//         *           autofocus
//         *           autocomplete="off"
//         *           placeholder="What needs to be done?"
//         *           value="{newTodo}"
//         *           onkeyup.enter="{add}"
//         *         />
//         *       </header>
//         *       <if cond="{!list.isEmpty()}">
//         *         <section class="main">
//         *           <input
//         *             id="toggle-all"
//         *             class="toggle-all"
//         *             type="checkbox"
//         *             value="{allDone}"
//         *           />
//         *           <label for="toggle-all" />
//         *           <ul class="todo-list">
//         *             <each var="td" in="{activeList}">
//         *               <TodoItem value="{td}" />
//         *             </each>
//         *           </ul>
//         *         </section>
//         *         <footer class="footer">
//         *           <span class="todo-count">
//         *             <strong>{remaining}</strong> {list.length == 1 ? "item" : "items"} left
//         *           </span>
//         *           <ul class="filters">
//         *             <li>
//         *               <a href="#/all" class="{mode == Mode.ALL ? "selected" : ""}">All</a>
//         *             </li>
//         *             <li>
//         *               <a href="#/active" class="{mode == Mode.ACTIVE ? "selected" : ""}"
//         *                 >Active</a>
//         *             </li>
//         *             <li>
//         *               <a
//         *                 href="#/completed"
//         *                 class="{mode == Mode.COMPLETED ? "selected" : ""}"
//         *                 >Completed</a>
//         *             </li>
//         *           </ul>
//         *           <if cond="{!list.isEmpty()}">
//         *             <button
//         *               class="clear-completed"
//         *               onclick="{removeCompleted}"
//         *               />Clear completed</button>
//         *           </if>
//         *         </footer>
//         *       </if>
//         *     </section>
//         */);
//    }
}
