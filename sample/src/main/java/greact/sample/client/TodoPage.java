package greact.sample.client;

import quickstart.Component;

import static greact.sample.Utils.render;

@Component(require = {TodoApp.class}) class TodoPage {
    public TodoPage() {
        render(/**
         * <!DOCTYPE html>
         * <html>
         *   <head>
         *     <title>TodoMVC</title>
         *     <link
         *       rel="stylesheet"
         *       type="text/css"
         *       href="https://unpkg.com/todomvc-app-css@2.2.0/index.css"
         *     />
         *   </head>
         *   <body>
         *     <TodoApp />
         *     <footer class="info">
         *       <p>Double-click to edit a todo</p>
         *       <p>Written by <a href="http://evanyou.me">Evan You</a></p>
         *       <p>Part of <a href="http://todomvc.com">TodoMVC</a></p>
         *     </footer>
         *   </body>
         * </html>
         */);
    }
}
