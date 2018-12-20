package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;
import utils.Token;

@Path("user")
public class UserEndpoints {

  //cashen vi gemmer brugere i
  private static UserCache userCache = new UserCache();
  public static boolean forceUpdate = true;

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Bruger ID for at få brugeren fra controlleren
      User user = userCache.getUser(forceUpdate, idUser);

    // TODO: Add Encryption to JSON : FIX
    // Konvertere brugeren til JSON for at returnere objektet
    String json = new Gson().toJson(user);

      //Tilføjer kryptering
      json = Encryption.encryptDecryptXOR(json);

    // TODO: What should happen if something breaks down? : FIX
      // Returner svar med status 200 og JSON som type
      if (user != null) {
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      } else {
          // Returner svar med status 400 og beskeden i teksten
          return Response.status(400).entity("Brugeren findes ikke").build();
      }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Skriver til log'en at vi er her
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // For en liste med brugere
    ArrayList<User> users = userCache.getUsers(forceUpdate);

    // TODO: Add Encryption to JSON : FIX
    // Konvertere bruger til JSON for at returnere dem
    String json = new Gson().toJson(users);

      //Tilføjer kryptering
      json = Encryption.encryptDecryptXOR(json);

      // Returnere brugerne med status 200 ved succes eller 404 med fejl
      if (users != null) {
          // På grund af cachen behøver vi ikke at forceupdate.
          this.forceUpdate = false;
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      } else {
          return Response.status(404).entity("Kunne ikke finde brugere").build();
      }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Læser json fra body and overfører det til en bruger klasse
    User newUser = new Gson().fromJson(body, User.class);

    // Bruger controlleren til at tilføje en bruger
    User createUser = UserController.createUser(newUser);

    // Får brugeren tilbage med tilføjet id og returnere det til brugeren
    String json = new Gson().toJson(createUser);

    // Returnere dataen til brugeren
    if (createUser != null) {

        this.forceUpdate = true;
      // Returnere brugerne med status 200 ved succes eller 404 med fejl
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Kunne ikke oprette bruger").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

      // Læser json fra body and overfører det til en bruger klasse
      User userToCome = new Gson().fromJson(body, User.class);

      // Bruger email og kode til at verificere brugern i controlleren. Dette giver også en token.
      User user = UserController.login(userToCome);

      // Returnere brugerne med status 200 ved succes og token eller 401 med fejl
      if (user != null) {
          //Welcoming the user and providing him/her with the token they need in order to delete or update their user.
          String msg = "Hej igen " + user.getFirstname() + "! Du er logget ind og modtager nu din token. Gem denne!" +
                  "da du skal bruge den senere. Her er din token:\n\n" + user.getToken() + "\n\nSkulle du " +
                  "miste din token, kan du altid logge på igen for at få en ny";
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(msg).build();
      } else {
          return Response.status(401).entity("Vi kunne ikke finde brugeren").build();
      }
  }

  // TODO: Make the system able to delete users : FIX
  @DELETE
  @Path("/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
      public Response deleteUser(@PathParam("idUser") int idUser, String body) {

      //Sætter en bruger fra informationen, notere ændringer til userobjektet. Der er tilføjet token som instans variabel
      User userToDelete = new Gson().fromJson(body, User.class);

      // Srkiver i log'en at vi er her
      Log.writeLog(this.getClass().getName(), this, "Deleting a user", 0);

      // bruger ID og token til at verificere om det er muligt at slette brugeren fra databasen via controller
      if (Token.verifyToken(userToDelete.getToken(), userToDelete)) {
          boolean deleted = UserController.deleteUser(idUser);

          //Hvis brugeren bliver slette cachen opdatere og brugeren skal vide det var en succes med status 200
          if (deleted) {
              forceUpdate = true;
              // Returnere med status 200 ved succes og besked
              return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Bruger slettet").build();
          } else {
              // Returnere med status 400 ved fejl og besked
              return Response.status(400).entity("Kunne ikke slette brugeren. Prøv igen").build();
          }
      } else {
          // Hvis token ikke kan verificeres
          return Response.status(401).entity("Du er ikke tilladt at foretage dette").build();
      }
  }

  // TODO: Make the system able to update users : FIX
  @PUT
  @Path("/update/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
          public Response updateUser(@PathParam("idUser") int idUser, String body) {

            //Sætter en bruger fra informationen, notere ændringer til userobjektet. Der er tilføjet token som instans variabel
              User userToUpdate = new Gson().fromJson(body, User.class);

              // Skriver i log'en at vi er her
              Log.writeLog(this.getClass().getName(), this, "Updating a user", 0);

              //bruger ID og token til at verificere om det er muligt at opdatere brugeren fra databasen via controller
              if (Token.verifyToken(userToUpdate.getToken(), userToUpdate)) {
                  boolean affected = UserController.updateUser(userToUpdate);

                  //Hvis brugeren bliver opdateret skal cachen opdateres og brugeren skal vide det var en succes med status 200
                  if (affected) {
                      forceUpdate = true;
                      String json = new Gson().toJson(userToUpdate);

                      //returnere svar til brugeren
                      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
                  } else {
                      return Response.status(400).entity("Kunne ikke opdatere bruger").build();
                  }
              } else {
                  //Hvis token ikke kan verificeres
                  return Response.status(401).entity("Du er ikke tilladt at gøre dette ").build();
              }
          }
}

