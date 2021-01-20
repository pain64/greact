package greact.sample.plainjs.demo.searchbox;

public class StrInput extends Input<String> {
    public StrInput(boolean required) {
        // FIXME: use super constructor
        this.required = required;
    }

    @Override String parseValueOpt(String src) {
        return src;
    }
}
