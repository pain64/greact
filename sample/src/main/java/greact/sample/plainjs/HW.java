package greact.sample.plainjs;

import org.over64.jscripter.std.js.HTMLInputElement;

import static org.over64.jscripter.std.js.Globals.document;

public class HW {
    public HW() {
        var view = document.getElementById("view");
        view.innerHTML = """
            <h1>hello, jScripter!</h1>
            <input id="n-users" value="0" />
            <button id="increment">increment</button>
            """;

        var elUsers = (HTMLInputElement) document.getElementById("n-users");
        var elIncrement = document.getElementById("increment");

        elIncrement.onclick = e ->
            elUsers.value = String.valueOf(Integer.parseInt(elUsers.value) + 1);
    }
}