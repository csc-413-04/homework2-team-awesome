package main.java;
import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
      // staticFiles.externalLocation("public");
      // http://sparkjava.com/documentation
      port(4321);
      // calling get will make your app start listening for the GET path with the /hello endpoint
      get("/hello", (req, res) -> "Hello World");

    }
}
