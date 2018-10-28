//package main.java
import static spark.Spark.*;
import com.mongodb.*;
public class Main {

    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost",27017); //with default server and port adress
        DB db = client.getDB( "REST2" );
        DBCollection users = db.getCollection("usrs");
        DBCollection auth = db.getCollection("auth");
        // staticFiles.externalLocation("public");
        // http://sparkjava.com/documentation
        port(4321);
        // calling get will make your app start listening for the GET path with the /hello endpoint
        get("/hello", (req, res) -> "Hello World");
        //newuser
        get("/newuser/:user", (req, res) -> {
           // System.out.println(req.)
            return "hello " + req.params("user");
        });
        //user - Login
        get("/user", (req, res) -> "Login Failed");



    }
}
