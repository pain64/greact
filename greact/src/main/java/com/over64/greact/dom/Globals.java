package com.over64.greact.dom;

import com.greact.model.JSExpression;
import com.greact.model.async;

import java.util.function.Consumer;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");
    public static HTMLElement gReactElement;

    public static <T extends HTMLElement> void gReactMount(T dest, HTMLNativeElements.Component0<T> element,
                                                           Object... args) {
        gReactElement = dest;
        JSExpression.of("element instanceof Function ? element(...args) : element.mount(...args)");
    }

    public static <T extends HTMLElement> T gReactReturn(Fragment.Renderer renderer) {
        renderer.render();
        return null;
    }

    public static java.lang.Runnable rpcBeforeSend = () -> {};
    public static java.lang.Runnable rpcAfterSend = () -> {};
    public static java.lang.Runnable rpcAfterSuccess = () -> {};
    public static Consumer<String> rpcAfterError = err -> {};

    @async public static <T> T doRemoteCall(String url, String endpoint, Object... args) {
        // FIXME: migrate to java version for try/catch/finally
        JSExpression.ofAsync("""
            try {
              this.rpcBeforeSend();
              var resp = await fetch(url, {
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  body: JSON.stringify({ endpoint: endpoint, args: args})
              });
              var data = await resp.json()
              if(resp.status == 500) { this.rpcBeforeSend(data.error); throw data.error; }
              
              this.rpcAfterSuccess();
              return data;
            } catch(ex) {
              this.rpcAfterError(ex);
              throw ex;
            } finally {
              this.rpcAfterSend();
            }""");

        return null;
    }
}
