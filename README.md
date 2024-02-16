# GReact: typesafe and productive web for Java
Make Java great (fullstack) again! Use Java compiler plugin technology for AST -> AST transformations to achieve fluent API and typesafety

## UI written on java
- Java to JS transpiling (latest java versions: 17+)
- React-like component system with compile-time reactive (Svelte-like, but much more explicit)
- Typesafe web page markup (via anon inner classes)
- Libgrid: typesafe UIKit

## Client ⇨ server interactions over RPC
- Embedded RPC with closures
- Simple JSON data serialization
- DI context provided as first arg of closures
- Typesafe client-server interactions!

## Typesafe SQL database access
- Use JDBC for transport
- Compile-time query checking
- Minimal API (not JPA-based) for
  - reduce boilerplate for class to result-set mapping
  - reduce boilerplate for CRUD operations
  - be sql-like as much as possible

## Productivity
- Livereload for client and server code (in dev mode)
- Simple API as much as posible
- Library way (not framework)
- Compile-time type safety

## Hello world
```java
class HelloWorld implements Component<h1> {
    @Override public h1 render() {
        return new h1("Hello, world");
    }
}
```
  
## Simple Demo (also see sample subproject)
Built with:
  - SparkJava as http server
  - Oracle database
  - Libgrid
 
``` java
import dom.jstack.greact.HTMLNativeElements.*;
import jstack.greact.libgrid.Grid;
import jstack.greact.TypesafeSql.Id;
import jstack.greact.TypesafeSql.Table;

import static iias.web.Main.Server.server;

public class MainPage implements Component<div> {
    @Table("users") record User(long id, String name, int age, String sex) {}
   
    @Override public Grid<User> render() {
        var users = server(db -> {
            // this code will be executed on server-side
            return db.select(User.class, "order by id")
        });
        return new Grid<>(users) {{
            onRowChange = u -> server(db -> db.update(u));
            onRowDelete = u -> server(db -> db.delete(u));
            onRowAdd = u -> server(db -> db.insert(u));
        }};
    }
}
  
```

```java
public class Main {
    static class Server extends RPC<TypesafeSql> {
        public static <T> T server(Function<TypesafeSql, T> onServer) {
            throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
        }
    }

    public static void main(String[] args) throws IOException {
        var ds = new HikariDataSource() {{
          setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
          addDataSourceProperty("url", "jdbc:oracle:thin:@//[localhost][1521]/users");
          setUsername("app_owner");
          setPassword("test");
          setConnectionTestQuery("select 1 from dual");
        }};

        var db = new TypesafeSql(ds);
        var server = new Server();
        
        var bundle = Loader.bundle(MainPage.class, false /* not release */);
        
        Spark.get("/*", (req, res) ->
                bundle.handleResource(
                        req.pathInfo(), res::status,
                        res::type, res.raw().getOutputStream()
                )
        );

        Spark.post(server.rpcBaseUrl, (req, res) ->
                server.handle(db, res::status, res::type, req.raw().getInputStream())
        );
        
        Spark.init();
    }
}

```
