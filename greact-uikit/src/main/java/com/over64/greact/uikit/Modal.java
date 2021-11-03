package com.over64.greact.uikit;

import com.greact.model.CSS;
import com.over64.greact.dom.HTMLNativeElements.*;

/**
 * FIXME:
 * Необходимо сделать Generic версию, чтобы не заставлять пользователя вкладывать именно div и делать дополнительный враппер,
 * в случае если он хочет вложить в Modal что-то другое, например h1
 */
@CSS.Require("modal.css") public class Modal implements Component0<div> {
    final Component0<button> opener;
    final Component0<div> content;

    public Modal(Component0<button> opener, Component0<div> content) {
        this.opener = opener;
        this.content = content;
    }

    boolean opened = false;

    @Override public div mount() {
        return new div() {{
            if (opened)
                new div() {{
                    className = "modal";
                    new div() {{
                        className = "modal-body";
                        new div() {{
                            className = "modal-content";
                            new div() {{
                                className = "modal-header";
                                new h3("Title") {{
                                    className = "modal-title";
                                }};
                                new div() {{
                                    className = "modal-close";
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                                        """;
                                    onclick = ev -> {
                                        ev.stopPropagation();
                                        effect(opened = false);
                                    };
                                }};
                            }};
                            new slot<>(content);
                        }};
                    }};
                }};

            new div() {{
                onclick = ev -> {
                    ev.stopPropagation();
                    effect(opened = true);
                };
                new slot<>(opener);
            }};
        }};
    }
}
