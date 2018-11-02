//package main.java;
//import com.mongodb.MongoClient;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.text.SimpleDateFormat;
import static spark.Spark.*;
import java.util.ArrayList;
import java.util.Date;
public class Main {
    public static int number = 0;

    // increments id for every username added to the database
    public static int AutoID() {
        number++;
        return number;
    }


    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("REST2");

        //Creating Collections
        MongoCollection<Document> userCollect = db.getCollection("users");
        MongoCollection<Document> authCollect = db.getCollection("auth");
      // staticFiles.externalLocation("public");
      // http://sparkjava.com/documentation
      port(4321);

      //Map<String, Object> map = new HashMap<String, Object>();
      //BasicBSONList map = new BasicBSONList();
      ArrayList<Object> map = new ArrayList<Object>();

      // calling get will make your app start listening for the GET path with the /hello endpoint
      get("/hello", (req, res) -> "Hello World");

      //newuser
      //get("/newuser", (req, res) -> "okay");
      get("/newuser", (req, res)-> {
          //Getting the username value
          int count = 0;
          String output = "";
          String username = req.queryParams("username");
          System.out.print(username);
          //Getting the pasword value
          String password = req.queryParams("password");
          Document document = new Document("username", username);
          Document dummy = new Document();
          //If there are no documents inside the collection yet...
          if (userCollect.count() == 0) {
            document.append("username", username).append("password", password).append("id", AutoID());
          }
          // if that's not the case then...
          else {
            // retrieve the last documents to get the latest id
            MongoIterable<Document> doc = userCollect.find();
            MongoCursor cursor = doc.iterator();
            // while there are still documents...
            while (cursor.hasNext()) {
                dummy = (Document) cursor.next();
                if ((int) dummy.get("id") > count) {
                    count = (int) dummy.get("id");
                }
            }
            document.append("username", username).append("password", password).append("id", count + 1);
          }
          document.append("friends", map);
          MongoIterable<Document> findUser = userCollect.find();
          MongoCursor<Document> userCursor = findUser.iterator();
          //checks if username already exist in the collection
          while (userCursor.hasNext()) {
            Document dummy_user = userCursor.next();
            if (dummy_user.get("username").equals(username)) {
                output = "username already exists";
                break;
            }
        }
        // if the output hasn't changed...
        if (output != "username already exists") {
            userCollect.insertOne(document);
            output = "okay";
        }
        return output;
      });
      //user - Login
       get("/login", (req, res) -> {

            String output = "";
            String username = req.queryParams("username");
            System.out.println(username);
            String password = req.queryParams("password");
            System.out.println(password);
            MongoIterable<Document> iterable = userCollect.find();
            MongoCursor<Document> cursor = iterable.iterator();
            output = "fail";
            //while there's still documents in the collection...
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                //System.out.println(doc);

                //
                if (doc.get("username") != null && doc.get("password") != null && doc.get("username").equals(username) && doc.get("password").equals(password)) {
                    System.out.println(doc.get("username"));
                    System.out.println(doc.get("password"));
                    output = "success";
                    break;
                }
            }
//IF the output = success...
            if (output == "success") {
                output = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                MongoIterable<Document> date_i = authCollect.find();
                MongoCursor<Document> date_c = date_i.iterator();
                //iterate through the authCollect to check for previous token for the user
                while (date_c.hasNext()) {
                    Document token = date_c.next();
                    System.out.println(token.get("user"));
                    // deletes the document if there are previous tokens
                    if (token.get("user").equals(username)) {
                        authCollect.findOneAndDelete(token);
                    }
                }

        // adds new token to authCollect
                Document token = new Document();
                token.append("token", output).append("user", username).append("password", password);
                authCollect.insertOne(token);
            }
            //Document document = new Document("username", username);
            //document.append("username", username).append("password",password);


            return output;
        });


        //add friend
        get("/addfriend", (req, res) -> {
            int count = 0;
            //query token
            String token = req.queryParams("token");
            //parse the friend value to an int
            int friend_id = Integer.parseInt(req.queryParams("friend"));
            //arraylist to store the updated "friends" key
            ArrayList<Integer> copy_f = new ArrayList<Integer>();
            //default output
            String output = "failed_authentication";
            //MongoIterable and Mongocursore to iterate through the autoCollect
            MongoIterable<Document> token_i = authCollect.find();
            MongoCursor<Document> token_c = token_i.iterator();
            // Document for updating purposes
            Document copy = new Document();
            // Document for deleting purposes
            Document delete = new Document();

            // While there are documents in authCollect...
            while (token_c.hasNext()) {
                Document token_identifier = token_c.next();
                // if the token matches, it will iterate through userCollect to see which one to add friend
                if (token_identifier.get("token").equals(token)) {
                    //output changes to okay
                    output = "fail_authentication";
                    //MongoIterable and MongoCursor to iterate through userCollect
                    MongoIterable<Document> user_i = userCollect.find();
                    MongoCursor<Document> user_c = user_i.iterator();
                    // checks which document in userCollect to update
                    while (user_c.hasNext()) {
                        Document user_identifier = user_c.next();
                        // dummy ArrayList
                        ArrayList<Object> doc = (ArrayList<Object>) user_identifier.get("friends");
                        // one for updating...
                        copy = user_identifier;
                        // one for deleting...
                        delete = user_identifier;
                        //System.out.println(doc);

                        /*if username is not null and the username matches... it first
                         *deletes the original document in the userCollect
                         *then update them on the "copy" document
                         */
                        if (user_identifier.get("username") != null && user_identifier.get("username").equals(token_identifier.get("user")) && userCollect.count() > 1) {
                            userCollect.findOneAndDelete(user_identifier);
                            MongoIterable<Document> friend_i = userCollect.find();
                            MongoCursor<Document> friend_c = friend_i.iterator();
                            while (friend_c.hasNext()) {
                                Document friend_doc = friend_c.next();
                                // adds the friend to dummy array if all the condition meets
                                if (friend_doc.get("id") != null && friend_doc.get("id") != user_identifier.get("id") && friend_doc.get("id").equals(friend_id)) {

                                    for (int i = 0; i < doc.size(); i++) {
                                        if (friend_doc.get("username").equals(doc.get(i))) {
                                            output = "You have already added this user into your friend list";
                                            break;
                                        }

                                    }
                                    if (output != "You have already added this user into your friend list") {
                                        count = 1;
                                        doc.add(friend_doc.get("username"));
                                        output = "okay";
                                        System.out.println(doc);
                                        break;
                                    }
                                }
                                // throws an error if the user adds him/herself into the friendlist

                                else {
                                    output = "failed_authentication";
                                }

                            }

                            // remove the friends key
                            copy.remove("friends");
                            // add the friends key with updated arraylist
                            copy.append("friends", doc);
                            System.out.println(copy);
                            break;
                        }
                    }

                }
            }
            //System.out.println(delete);
            //add the "copy" document to the userCollect
                userCollect.insertOne(copy);

            return output;

        });
        get("friends", (req, res) -> {
            String output = "";
            String token = req.queryParams("token");
            MongoIterable<Document> token_finder = authCollect.find();
            MongoCursor<Document> token_cursor = token_finder.iterator();
            while (token_cursor.hasNext()) {
                Document token_dummy = token_cursor.next();
                if (token_dummy.get("token").equals(token)) {
                    MongoIterable<Document> user_finder = userCollect.find();
                    MongoCursor<Document> user_cursor = user_finder.iterator();
                    while (user_cursor.hasNext()) {
                        Document user_dummy = user_cursor.next();
                        if (user_dummy.get("username").equals(token_dummy.get("user"))) {
                            ArrayList<Object> friend_list = (ArrayList<Object>) user_dummy.get("friends");
                            for (int i = 0; i < friend_list.size(); i++) {
                                output = output.concat((String) friend_list.get(i) + "<br/>");

                            }
                        }
                    }
                }
            }
            return output;
        });



    }
}
