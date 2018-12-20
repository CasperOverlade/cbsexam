package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import model.LineItem;
import model.Product;
import utils.Log;

public class LineItemController {

  private static DatabaseController dbCon;

  public LineItemController() {
    dbCon = new DatabaseController();
  }

  public static LineItem createLineItem(LineItem lineItem, int orderID) {

    // Skriver i log at vi er her
    Log.writeLog(ProductController.class.getName(), lineItem, "Actually creating a line item in DB", 0);

    // Check forbindelse
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Får produkt ID siden brugeren ikke sender det til os
    lineItem.getProduct().setId(ProductController.getProductBySku(lineItem.getProduct().getSku()).getId());

    // Opdatere produkt id

    // Insætter lineItem i DB
    int lineItemID = dbCon.insert(
            "INSERT INTO line_item(product_id, order_id, l_price, quantity) VALUES("
                    + lineItem.getProduct().getId()
                    + ", "
                    + orderID
                    + ", "
                    + lineItem.getPrice()
                    + ", "
                    + lineItem.getQuantity()
                    + ")");

    if (lineItemID != 0) {
      //Updatere lineItem id før det returneres
      lineItem.setId(lineItemID);
      return lineItem;

    } else{
      // Returner null hvis det ikke blev sat i DB
      return null;
    }
  }

  //createLineItem instanciere, initialisere og deklerere an LineItem objekt baseret på information fra resultset
  public static LineItem createLineItem(ResultSet rs, Product product) {
    try {
      LineItem lineItem = new LineItem(rs.getInt("l_id"),product,
              rs.getInt("quantity"),
              rs.getFloat("l_price"));

      return lineItem;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

}
