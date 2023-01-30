package jstack.greact;

public class GReactExceptions {
    public static class GReactCompileException extends RuntimeException {}
    public static class NewClassDeniedHere extends GReactCompileException {
        final String at;
        public NewClassDeniedHere(String at) {
            this.at = at;

        }
        @Override public String toString() {
            return """
                Unexpected view root here %s
                Expected one of:
                  Mount method:
                    class X implements Component<div> {
                      @Override public div mount() {
                        /* some code */
                        return new div() {{ /* <--- this is view root */
                        }};
                      }
                    }
                  Lambda component (expression):
                    class Some {
                      Component<div> comp = () ->
                        new h1("hello"); /* <--- this is view root */
                    }
                    
                  Lambda component (statement):
                    class Some {
                      Component<div> comp = () -> {
                        /* some code */
                        return new h1("hello"); /* <--- this is view root */
                      }
                    }
                """.formatted(at);
        }
    }
}
