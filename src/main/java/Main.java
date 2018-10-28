package main.java;
//import com.mongodb.MongoClient;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("REST2");

        //Creating Collections
        MongoCollection<Document> userCollect = db.getCollection("users");
      // staticFiles.externalLocation("public");
      // http://sparkjava.com/documentation
      port(4321);
      // calling get will make your app start listening for the GET path with the /hello endpoint
      get("/hello", (req, res) -> "Hello World");

      //newuser
      //get("/newuser", (req, res) -> "okay");
      get("/newuser", (req, res)-> {
          //Getting the username value
          String username = req.queryParams("username");
          System.out.print(username);
          //Getting the pasword value
          String password = req.queryParams("password");
          Document document = new Document("username", username);
          document.append("username", username).append("password", password);
          userCollect.insertOne(document);
          return "Added " + "Username: " + username + " Password: " + password;

      });
      //user - Login
      get("/user", (req, res) -> req.params("user"));
    }
}
