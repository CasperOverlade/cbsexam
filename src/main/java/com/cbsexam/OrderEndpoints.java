package com.cbsexam;

import cache.OrderCache;
import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Order;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {

  private static OrderCache orderCache = new OrderCache();

  public static boolean forceUpdate = true;

  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}")
  public Response getOrder(@PathParam("idOrder") int idOrder) {

    // Kalder vores controller lag for at få ordren fra databasen
    Order order = orderCache.getOrder(forceUpdate, idOrder);

    // TODO: Add Encryption to JSON : FIX
    // Her konverteres java objektet til json med GSON biblioteket importeret i Maven
    String json = new Gson().toJson(order);

    //Tilføjer kryptering
    json = Encryption.encryptDecryptXOR(json);

    // Returnere dataen til brugeren hvis der var en ordre
    if (order != null) {
      // Returner svar med status 200 og JSOn som type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      // Returner svar med status 404 og beskeden i teksten
      return Response.status(404).entity("Kunne ikke finde ordren").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getOrders() {

    // Kalder vores controller lag for at få ordre fra databasen
    ArrayList<Order> orders = orderCache.getOrders(forceUpdate);

    // TODO: Add Encryption to JSON : FIX
    // Her konverteres java objektet til json med GSON biblioteket importeret i Maven
    String json = new Gson().toJson(orders);

    //tilføjer kryptering
    json = Encryption.encryptDecryptXOR(json);

    this.forceUpdate = false;
    // Returnere dataen til brugeren hvis der var ordrer
    if (orders != null) {


      // Returner svar med status 200 og JSOn som type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    } else {
      // Returner svar med status 404 og beskeden i teksten
      return Response.status(404).entity("Kunne ikke finde ordre").build();
    }
  }


  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Læser JSON fra body og overfører det to en order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Bruger controlleren til at tilføje ordren
    Order createdOrder = OrderController.createOrder(newOrder);

    // For ordren tilbage med ID og returnerer det to ordren
    String json = new Gson().toJson(createdOrder);

    // Returner dataen til ordren
    if (createdOrder != null) {

      this.forceUpdate = true;
      // Returner svar med status 200 og JSON som type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Returner svar med status 400 og beskeden i teksten
      return Response.status(400).entity("Kunne ikke oprette ordre").build();
    }
  }
}