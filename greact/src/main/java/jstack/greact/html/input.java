package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class input extends HTMLElement implements HTMLElementAsComponent<input> {
    public boolean autofocus = false;
    public boolean required;
    public boolean readOnly;
    public enum Autocomplete {OFF, ON}
    public Autocomplete autocomplete;
    public enum InputType {CHECKBOX, TEXT}
    //public InputType type;
    public String name;
    public String type;
    public String minLength;
    public String maxLength;
    public String placeholder;
    public String value;
    public String minlength;
    public String maxlength;
    public long valueAsNumber;
    public Boolean checked;
    public String min;
    public String max;
    public String step;
    public native void setCustomValidity(String text);
}
