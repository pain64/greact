package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("grid.css")
class GridRowEdit<T> implements Component0<tr> {
    final T data;
    final GridConfig2<T> conf;
    final Runnable onFinishRowEdit;

    GridRowEdit(T data, GridConfig2<T> conf, Runnable onFinishRowEdit) {
        this.data = data;
        this.conf = conf;
        this.onFinishRowEdit = onFinishRowEdit;
    }

    @Override public tr mount() {
        return new tr() {{
            className = "grid-row-edit";

            for (var col : conf.columns) {
                if(col.hidden) continue;
                if (col.editor != null)
                    new td() {{
                        Grid.setEditorValueFromRowValue(col, data);
                        new slot<>(col.editor);
                    }};
                else
                    new slot<>(
                            (Component1<td, Object>) col.view,
                            Grid.fetchValue(data, col.memberNames));
            }

            new td() {{ /* toolbox */
                className = "grid-row-edit-toolbox";
                new div() {{
                    style.display = "flex";
                    id = "grid-row-edit-toolbox-body";

                    new div() {{ /* save changes */
                        innerHTML = """
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-save"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                            """;
                        onclick = ev -> {
                            for (var col : conf.columns)
                                if (!col.hidden && col.editor != null)
                                    Grid.setValue(data, col.memberNames, col.editor.value);
                            conf.onRowChange.handle(data);
                            onFinishRowEdit.run();
                        };
                    }};
                    new div() {{ /* cancel save changes */
                        innerHTML = """
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                            """;
                        onclick = ev -> onFinishRowEdit.run();
                    }};
                }};
            }};
        }};
    }
}