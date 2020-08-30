package greact.sample;

import com.greact.model.Component;

@Component(require = {TodoApp.class})
public class TodoPage {
    /**
     * <body>
     *     <h1>Hello, world</h1>
     *     <h1>it works!</h1>
     * </body>
     */
    String view;
}
