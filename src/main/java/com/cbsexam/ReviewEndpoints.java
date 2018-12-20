package com.cbsexam;
import com.google.gson.Gson;
import controllers.ReviewController;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Review;
import utils.Encryption;

@Path("search")
public class ReviewEndpoints {

  /**
   * @param reviewTitle
   * @return Responses
   */
  @GET
  @Path("/title/{title}")
  public Response search(@PathParam("title") String reviewTitle) {

    // Kalder vores controller lag for at hente review fra databasen
    ArrayList<Review> reviews = ReviewController.searchByTitle(reviewTitle);

    // TODO: Add Encryption to JSON : FIX
    // Konvetere java objektet til json med GSON bibliotektet importeret i Maven
    String json = new Gson().toJson(reviews);

    //Tilføjer kryptering
    json = Encryption.encryptDecryptXOR(json);

    // Returner svar med status 200 og JSON som type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }


}
