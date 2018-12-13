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

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), lineItem, "Actually creating a line item in DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Get the ID of the product, since the user will not send it to us.
    lineItem.getProduct().setId(ProductController.getProductBySku(lineItem.getProduct().getSku()).getId());

    // Update the ID of the product

    // Insert the line item in the DB
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
      //Update the line item id of the line item before returning
      lineItem.setId(lineItemID);
      return lineItem;

    } else{
      // Return null if line item has not been inserted into database
      return null;
    }
  }

  public static LineItem formLineItem (ResultSet rs, Product product) {
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
