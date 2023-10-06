package jstack.greact.uikit;

import jstack.greact.html.*;
import jstack.greact.uikit.GridConfig2.AsyncHandler;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.JSExpression;

@Require.CSS("grid.css")
public class GridRowAdd<T> implements Component0<tr> {
    T data;
    final GridConfig2<T> conf;
    final AsyncHandler<T> onConfirmRowAdd;
    final Runnable onCancelRowAdd;

    public GridRowAdd(GridConfig2<T> conf, AsyncHandler<T> onConfirmRowAdd, Runnable onCancelRowAdd) {
        data = JSExpression.of("{}"); // Fixme: instantiate with new()
        this.conf = conf;
        this.onConfirmRowAdd = onConfirmRowAdd;
        this.onCancelRowAdd = onCancelRowAdd;
    }

    @Override public tr render() {
        return new tr() {{
            className = "grid-row-add";

            if (conf.customRowAdder != null) {
                for (var control : conf.customRowAdder.controls)
                    new td() {{
                        colSpan = control.slots;
                        new slot<>(control);
                    }};
            } else
                for (var col : conf.columns) {
                    if(col.hidden) continue;
                    if (col.editor != null)
                        new td() {{
                            Grid.setEditorValueFromRowValue(col, data);
                            new slot<>(col.editor);
                        }};
                    else new td();
                }

            new td() {{ /* toolbox */
                className = "grid-row-add-toolbox";

                new div() {{
                    id = "grid-row-add-toolbox-body";
                    className = "toolbox";

                    new div() {{ /* confirm create row */
                        innerHTML = """
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-save"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                            """;
                        onclick = ev -> {
                            if (conf.customRowAdder != null)
                                data = conf.customRowAdder.loader.load();
                            else
                                for (var col : conf.columns)
                                    if (col.editor != null)
                                        Grid.setValue(data, col.memberNames, col.editor.value);

                            onConfirmRowAdd.handle(data);
                        };
                    }};
                    new div() {{ /* cancel create row */
                        innerHTML = """
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                            """;
                        onclick = ev -> onCancelRowAdd.run();
                    }};
                }};
            }};
        }};
    }
}
