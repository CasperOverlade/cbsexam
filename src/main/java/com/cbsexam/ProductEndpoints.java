package com.cbsexam;

import cache.ProductCache;
import com.google.gson.Gson;
import controllers.ProductController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Product;
import utils.Encryption;

@Path("product")
public class ProductEndpoints {

  //Dette er cachen vi gemmer produkterne i
  public static ProductCache cache = new ProductCache();

  //Fortæller om cachen skal opdateres eller ej
  private static boolean forceUpdate=true;

  /**
   * @param idProduct
   * @return Responses
   */
  @GET
  @Path("/{idProduct}")
  public Response getProduct(@PathParam("idProduct") int idProduct) {

    // Kalder vores controller lag for at få produktet fra databasen
    Product product = cache.getProduct(forceUpdate, idProduct);

    // TODO: Add Encryption to JSON : FIX
    // Konvetere Java objektet til json med GSON
    String json = new Gson().toJson(product);

    //tilføjer kryptering
    json = Encryption.encryptDecryptXOR(json);

    // Returnere data til brugeren
    if (product != null) {
      // Returnere med status 200 og json type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      //Returnere med status 404 og besked
      return Response.status(404).entity("Kunne ikke finde produkt").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getAllProducts() {

    // kalder vores controller lag for at hente produkter fra database
    ArrayList<Product> products = cache.getProducts(forceUpdate);

    // TODO: Add Encryption to JSON : FIX
    // Konveterer Java objektet til json med GSON
    String json = new Gson().toJson(products);

    // tilføjer kryptering
    json = Encryption.encryptDecryptXOR(json);

    // Returnere data til brugeren
    if (products != null) {
      //Vi behøver ikke at forceupdate siden produkterne ligger i cachen.
      this.forceUpdate = false;
      // Svarer med status 200 og json hvis der er succes. Status 404 ved fejl
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(404).entity("Kunne ikke hente produkter").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(String body) {

    // Læser json fra body og overfører det til produkt klasse
    Product newProduct = new Gson().fromJson(body, Product.class);

    // Bruger controlleren til at tilføje produktet.
    Product createdProduct = ProductController.createProduct(newProduct);

    // Får produktet tilbage med ID og returnere det til brugeren
    String json = new Gson().toJson(createdProduct);

    // Returnere data til brugeren
    if (createdProduct != null) {

        this.forceUpdate = true;
      // Returnere status 200 og json type ved succes. Status 400 ved fejl
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Kunne ikke oprette produkt").build();
    }
  }
}
