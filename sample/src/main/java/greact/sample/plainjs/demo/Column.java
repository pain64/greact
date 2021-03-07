package greact.sample.plainjs.demo;

import com.greact.model.MemberRef;

class Column<T> {
    final String header;
    final String memberName;

    Column(String header, MemberRef<T, Object> ref) {
        this.header = header;
        this.memberName = ref.memberName();
    }
}
