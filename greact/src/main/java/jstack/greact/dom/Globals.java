package jstack.greact.dom;

import jstack.greact.html.Component0;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Async;
import org.jetbrains.annotations.Nullable;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");
    public static HTMLElement gReactElement;

    public static <T extends HTMLElement> void gReactMount(T dest, Component0<T> element,
                                                           Object... args) {
        gReactElement = dest;
        JSExpression.of(":1 instanceof Function ? :1(...:2) : :1.mount(...:2)", element, args);
    }

    public static <T extends HTMLElement> T gReactReturn(Fragment.Renderer renderer) {
        renderer.render();
        return null;
    }

    @FunctionalInterface
    public interface ErrorHandler {
        void handle(String message, @Nullable String stackTrace);
    }

    public static java.lang.Runnable rpcBeforeSend = () -> { };
    public static java.lang.Runnable rpcAfterSend = () -> { };
    public static java.lang.Runnable rpcAfterSuccess = () -> { };
    public static ErrorHandler rpcAfterError = (msg, stacktrace) -> { };

    @Async public static <T> T doRemoteCall(String url, String endpoint, Object... args) {
        // FIXME: migrate to java version for try/catch/finally
        JSExpression.ofAsync("""
            this.rpcBeforeSend();
            try {
              var resp = await fetch(:1, {
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  body: JSON.stringify({ endpoint: :2, args: :3})
              });
              var data = await resp.json()
              if (resp.status == 500) throw data.error;
              
              this.rpcAfterSuccess();
              return data;
            } catch(ex) {
              this.rpcAfterError(data.msg, data.stackTrace);
              throw ex;
            } finally {
              this.rpcAfterSend();
            }""", url, endpoint, args);

        return null;
    }
}
