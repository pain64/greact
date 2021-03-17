package greact.sample.plainjs.demo;

import com.greact.model.MemberRef;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

class Column<T, U> {
    final String header;
    final String memberName;
    _00Control<U> editor = null;

    Column(String header, MemberRef<T, U> ref) {
        this.header = header;
        this.memberName = ref.memberName();
    }

    Column<T, U> editable(_00Control<U> editor) {
        this.editor = editor;
        return this;
    }
}
