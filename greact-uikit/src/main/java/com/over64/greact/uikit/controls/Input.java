package com.over64.greact.uikit.controls;

import com.over64.greact.dom.HTMLNativeElements.*;

public abstract class Input<T> extends Control<T> {
    boolean required = true;
    int maxWidth = 0;
    int maxLength = 0;
    final String type;

    protected Input(String type) {this.type = type;}
    protected abstract T parseValueOpt(input src);
    protected String valueToHtmlValue() {
        return  value.toString();
    }

    @Override public Control child() { return null; }

    @Override public div mount() {
        var self = this;
//        if(self.value == null && self._optional) {
//            self.ready = true; // FIXME: наверное, проверка на _optional должна быть не здесь
//            self.onReadyChanged.run();
//        }

        return new div() {{
            new style("""
                      @supports (-webkit-appearance: none) or (-moz-appearance: none) {
                        input[type=checkbox],
                      input[type=radio] {
                          --active: black;
                          --active-inner: #fff;
                          --focus: 2px rgba(39, 94, 254, .3);
                          --border: #BBC1E1;
                          --border-hover: #275EFE;
                          --background: #fff;
                          --disabled: #F6F8FF;
                          --disabled-inner: #E1E6F9;
                          -webkit-appearance: none;
                          -moz-appearance: none;
                          height: 16px;
                          outline: none;
                          display: inline-block;
                          vertical-align: top;
                          position: relative;
                          margin: 0;
                          cursor: pointer;
                          border: 1px solid var(--bc, var(--border));
                          background: var(--b, var(--background));
                          transition: background 0.3s, border-color 0.3s, box-shadow 0.2s;
                          display: inline-block;
                          margin-top:1.5px;
                        }
                        input[type=checkbox]:after,
                      input[type=radio]:after {
                          content: "";
                          display: block;
                          left: 0;
                          top: 0;
                          position: absolute;
                          transition: transform var(--d-t, 0.3s) var(--d-t-e, ease), opacity var(--d-o, 0.2s);
                        }
                        input[type=checkbox]:checked,
                      input[type=radio]:checked {
                          --b: var(--active);
                          --bc: var(--active);
                          --d-o: .3s;
                          --d-t: .6s;
                          --d-t-e: cubic-bezier(.2, .85, .32, 1.2);
                        }
                        input[type=checkbox]:disabled,
                      input[type=radio]:disabled {
                          --b: var(--disabled);
                          cursor: not-allowed;
                          opacity: 0.9;
                        }
                        input[type=checkbox]:disabled:checked,
                      input[type=radio]:disabled:checked {
                          --b: var(--disabled-inner);
                          --bc: var(--border);
                        }
                        input[type=checkbox]:disabled + label,
                      input[type=radio]:disabled + label {
                          cursor: not-allowed;
                        }
                        input[type=checkbox]:hover:not(:checked):not(:disabled),
                      input[type=radio]:hover:not(:checked):not(:disabled) {
                          --bc: var(--border-hover);
                        }
                        input[type=checkbox]:focus,
                      input[type=radio]:focus {
                          box-shadow: 0 0 0 var(--focus);
                        }
                        input[type=checkbox]:not(.switch),
                      input[type=radio]:not(.switch) {
                          width: 16px;
                        }
                        input[type=checkbox]:not(.switch):after,
                      input[type=radio]:not(.switch):after {
                          opacity: var(--o, 0);
                        }
                        input[type=checkbox]:not(.switch):checked,
                      input[type=radio]:not(.switch):checked {
                          --o: 1;
                        }
                        input[type=checkbox] + label,
                      input[type=radio] + label {
                          font-size: 14px;
                          line-height: 16px;
                          display: inline-block;
                          vertical-align: top;
                          cursor: pointer;
                          margin-left: 4px;
                        }
                      
                        input[type=checkbox]:not(.switch) {
                          border-radius: 3px;
                        }
                        input[type=checkbox]:not(.switch):after {
                          width: 5px;
                          height: 9px;
                          border: 2px solid var(--active-inner);
                          border-top: 0;
                          border-left: 0;
                          left: 5px;
                          top: 2px;
                          transform: rotate(var(--r, 20deg));
                        }
                        input[type=checkbox]:not(.switch):checked {
                          --r: 43deg;
                        }
                        input[type=checkbox].switch {
                          width: 38px;
                          border-radius: 11px;
                        }
                        input[type=checkbox].switch:after {
                          left: 2px;
                          top: 2px;
                          border-radius: 50%;
                          width: 15px;
                          height: 15px;
                          background: var(--ab, var(--border));
                          transform: translateX(var(--x, 0));
                        }
                        input[type=checkbox].switch:checked {
                          --ab: var(--active-inner);
                          --x: 17px;
                        }
                        input[type=checkbox].switch:disabled:not(:checked):after {
                          opacity: 0.6;
                        }
                      
                        input[type=radio] {
                          border-radius: 50%;
                        }
                        input[type=radio]:after {
                          width: 19px;
                          height: 19px;
                          border-radius: 50%;
                          background: var(--active-inner);
                          opacity: 0;
                          transform: scale(var(--s, 0.7));
                        }
                        input[type=radio]:checked {
                          --s: .5;
                        }
                      }
                      
                      
                """);
//            style.alignItems = "center";
            //style.padding = "0px 2px";
         //   style.margin = "0px 10px 0px 0px";
            new label() {{
                style.display = "flex";
                style.alignItems = "center";
                style.whiteSpace = "nowrap";
//                if(self instanceof CheckBox)
//                    style.margin = "3px 0px 0px 0px";
//                if(self instanceof CheckBox)
//                    style.display = "inline-block";

//                style.height = "100%";

                new span(_label) {{
//                    style.display = "inline-flex";
//                    style.alignItems = "center";
//                    style.whiteSpace = "nowrap";
                    //style.margin = "0px 5px 0px 0px";
                }};
                new input() {{
                    //className = "form-check-input";
                    style.width = "100%";
                    type = self.type;
                    value = self.value == null ? null : valueToHtmlValue();
                    // FIXME: вот это вот - костыль для CheckBox
                    checked = (Boolean) self.value;
                    onchange = ev -> {
                        self.value = parseValueOpt(ev.target);
                        self.ready = self._optional || self.value != null;
                        self.onReadyChanged.run();
                    };
                }};
            }};
        }};
    }
}