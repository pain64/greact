package greact.sample.plainjs;

import com.greact.model.Component;
import com.greact.model.Require;
import com.greact.model.View;

@Require({H1.class, Input.class, Button.class})
public class Demo1 extends Component {

    int nUsers = 0;

    @View String view = """
        <H1 text="hello, JScripter" />
        <Input value={nUsers} />
        <Button text="increment" onclick={addOneUser} />
        """;

    void addOneUser() { nUsers += 1; }
}
