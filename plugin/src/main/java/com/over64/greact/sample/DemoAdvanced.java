package com.over64.greact.sample;

import com.over64.greact.sample.Switch.CaseArgs;
import org.over64.jscripter.std.js.DocumentFragment;

import static com.over64.greact.GReact.effect;

import com.over64.greact.GReact;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.over64.greact.GReact.render;

public class DemoAdvanced {
    public @interface Buildable {
        Class rrr();
    }
    @FunctionalInterface
    interface UserCountLoader {
        void load(Consumer<Long> onUserLoad);
    }

    <T> void server(Supplier<T> onServer, Consumer<T> result) {

    }

    class Switch<T> {
        public Switch(T of) {

        }
        public void caseIf(T eq, Runnable runnable) {

        }

        void render(DocumentFragment dom) {

        }
    }

    class If {
        public If(boolean cond) {

        }
        void doThen(Runnable runnable) {

        }

        void doElse(Runnable runnable) {

        }
    }

    class list<T> {
        public list(T[] of) {

        }
        void item(Consumer<T> slot) {

        }
    }

    class h1 {
        public h1(String text) {
        }
    }

    class button {
        public button(String text) {

        }

        public Runnable onclick = () -> {
        };
    }

    class div {
    }

    enum Mode {M1, M2}

    Mode mode = Mode.M1;
    boolean showUsers = true;

    public DemoAdvanced(DocumentFragment dom) {
        var users = new String[]{"Ivan", "John", "Iborg"};

        Runnable toggle = () -> effect(showUsers = !showUsers);
        GReact.render(dom, """
            <h1>hello</h1>""");

        com.over64.greact.GReact.render(dom, """
            <h1>hello</h1>""");

        server(() -> {
            // sql.exe(di.conn, "select count(*) from users")
            return 42;
        }, count -> render(dom, "<h1>{count}</h1>"));

        UserCountLoader maleLoader = onLoaded ->
            server(() -> 42L /* sql.exe(di.conn, "select sex from user_male") */, onLoaded);

        server(UserService::userCount, count -> {

            // transform at compile time?
            new div() {{
                new Switch<>(mode) {{
                    caseIf(Mode.M1, () -> new h1("selected M1 mode"));
                    caseIf(Mode.M2, () -> new h1("selected M2 mode"));
                }};

                new button("toggle show users " + count) {{
                    onclick = toggle;
                }};

                new If(showUsers) {{
                    doThen(() ->
                        new list<>(users) {{
                            item(user -> new h1("name" + user));
                        }});
                    doElse(() -> new h1("user show disabled"));
                }};
            }};


            render(dom, """
                <Switch of={mode}>
                  <caseIf eq={Mode.M1}>
                    <h1>selected M1 mode</h1>
                  </caseIf>
                  <caseIf eq={Mode.M2}>
                    <h1>selected M2 mode</h1>
                  </caseIf>
                </Switch>
                            
                <button onclick={toggle}>
                  toggle show users {count}
                </button>
                            
                <If cond={showUsers}>
                  <doThen>
                    <List of={users}>
                      <item(user)><h1>{user}</h1></item>
                      <td><Joiner bind={user.male} loader={maleLoader} /></td>
                    </List>
                  </doThen>
                  <doElse><h1>user show disabled</h1></doElse>
                </If>""", List.class, Switch.class, If.class)
        });
    }
}
