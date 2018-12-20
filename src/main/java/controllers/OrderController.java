package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int orderId) {
    // checker forbindelse
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders\n" +
            "inner join\n" +
            "user ON orders.user_id = user.u_id\n" +
            "inner join \n" +
            "line_item ON orders.o_id = line_item.order_id \n" +
            "inner join \n" +
            "address AS ba ON orders.billing_address_id = ba.a_id\n" +
            "inner join \n" +
            "address as sa ON orders.shipping_address_id = sa.a_id\n" +
            "inner join \n" +
            "product ON line_item.product_id  = product.p_id \n" +
            "where orders.o_id = " + orderId;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    // Nyt order object
    Order order = null;
    // User object
    User user = null;
    // Nyt lineitem object
    LineItem lineItem = null;
    //Nyt LineitemList
    ArrayList<LineItem> lineItemsList = new ArrayList<>();
    //Nyt productlist
    Product product = null;
    // Nyt adress object
    Address billingAddress = null;
    // Nyt adress object
    Address shippingAddress = null;

    try {
      while (rs.next()) {
        if (order == null) {
          user = UserController.formUser(rs);

          product = ProductController.createProduct(rs);

          lineItem = LineItemController.createLineItem(rs, product);

          lineItemsList.add(lineItem);

          // Creating new billingAddress
          billingAddress = AddressController.createBillingAddress(rs);

          // Creating new Shippingaddress
          shippingAddress = AddressController.createShippingAddress(rs);

          order = createOrder1(rs, user, lineItemsList, billingAddress, shippingAddress);

        }else {
          product = ProductController.createProduct(rs);
          lineItem = LineItemController.createLineItem(rs, product);
          order.getLineItems().add(lineItem);
        }
      }
      // Returns the build order
      return order;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    return null;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {
    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    // Orders instead of order in sql statement

    String sql = "SELECT * FROM orders\n" +
            "inner join\n" +
            "             user ON orders.user_id = user.u_id\n" +
            "             inner join \n" +
            "             line_item ON orders.o_id = line_item.order_id \n" +
            "             inner join \n" +
            "             address AS ba ON orders.billing_address_id = ba.a_id\n" +
            "             inner join \n" +
            "             address as sa ON orders.shipping_address_id = sa.a_id\n" +
            "             inner join \n" +
            "             product ON line_item.product_id  = product.p_id\n" +
            "             order by orders.o_id";

    ArrayList<Order> orders = new ArrayList<Order>();
    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    // New order object
    // Order order = null;

    try {
      while(rs.next()) {

        // User object
        User user = null;
        // New lineitem object
        LineItem lineItem = null;
        // New adress object
        Address billingAddress = null;
        // New adress object
        Address shippingAddress = null;
        // new product object
        Product product = null;
        //New LineitemList
        ArrayList<LineItem> lineItemsList = new ArrayList<>();

        if (orders.isEmpty() || rs.getInt("o_id") != orders.get(orders.size()-1).getId()) {

          // Creating new user object
          user = UserController.formUser(rs);

          product = ProductController.createProduct(rs);

          lineItem = LineItemController.createLineItem(rs, product);

          lineItemsList.add(lineItem);

          // Creating new billingAddress
          billingAddress = AddressController.createBillingAddress(rs);
          // Creating new shippingAddress
          shippingAddress = AddressController.createShippingAddress(rs);

          // Creating new order
          Order order = createOrder1(rs, user, lineItemsList, billingAddress, shippingAddress);

          // Adding order to arraylist
          orders.add(order);
        } else if (rs.getInt("o_id") == orders.get(orders.size()-1).getId()){
          product = ProductController.createProduct(rs);

          lineItem = LineItemController.createLineItem(rs, product);
          lineItemsList.add(lineItem);

          orders.get(orders.size()-1).getLineItems().add(lineItem);
        }

      }
      return orders;
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;

  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    try { //We set the autocommit to false, making the way to use transactions
          dbCon.getConnection().setAutoCommit(false);

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    order.setCustomer(UserController.createUser(order.getCustomer()));

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.

    // Insert the product in the DB
    int orderID = dbCon.insert(
        "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, order_created_at, order_updated_at) VALUES("
            + order.getCustomer().getId()
            + ", "
            + order.getBillingAddress().getId()
            + ", "
            + order.getShippingAddress().getId()
            + ", "
            + order.calculateOrderTotal()
            + ", "
            + order.getCreatedAt()
            + ", "
            + order.getUpdatedAt()
            + ")");

    if (orderID != 0) {
      //Update the productid of the product before returning
      order.setId(orderID);
    }

    // Create an empty list in order to go trough items and then save them back with ID
    ArrayList<LineItem> items = new ArrayList<LineItem>();

    // Save line items to database
    for(LineItem item : order.getLineItems()){
      item = LineItemController.createLineItem(item, order.getId());
      items.add(item);
    }

    //Add line items to the order, commit and return the order
    order.setLineItems(items);
    dbCon.getConnection().commit();
    return order;

    // adding nullpointerexception, since we are using getUser() and not createUser() - we want people to be
    // logged in before they make an order. like Amazon or other sites.
  } catch (SQLException | NullPointerException e) {
    System.out.println(e.getMessage());
    try {
      //If the exception is catched, we roll back our statements to the database.
      System.out.println("rolling back");
      dbCon.getConnection().rollback();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
    finally {
    try {
      //Sets autocommit to true.
      dbCon.getConnection().setAutoCommit(true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
    return null;
    }

  public static Order createOrder1(ResultSet rs, User user, ArrayList<LineItem> lineItemsList, Address billingsAddress, Address shippingsAddres) {
    try {
      Order order = new Order(
              rs.getInt("o_id"),
              user,
              lineItemsList,
              billingsAddress,
              shippingsAddres,
              rs.getFloat("order_total"),
              rs.getLong("order_created_at"),
              rs.getLong("order_updated_at"));

      return order;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}

