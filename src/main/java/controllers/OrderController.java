package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }


  public static Order getOrder(int orderId) {

    ResultSet rs = null;
    try{
      // check for connection
      if (dbCon == null || dbCon.getConnection().isClosed() ) {
        dbCon = new DatabaseController();
      }


      // Build SQL string to query
      String sql = "SELECT * FROM orders " +
              "inner join user ON orders.user_id = user.u_id " +
              "inner join line_item ON orders.o_id = line_item.order_id " +
              "inner join address AS ba ON orders.billing_address_id = ba.a_id " +
              "inner join address as sa ON orders.shipping_address_id = sa.a_id " +
              "inner join product ON line_item.product_id  = product.p_id " +
              "where orders.o_id = ? ";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql);
      preparedStatement.setInt(1, orderId);

      rs = preparedStatement.executeQuery();

      // New order object
      Order order=null;
      // User object
      User user;
      // New lineitem object
      LineItem lineItem;
      //New LineitemList
      ArrayList<LineItem> lineItemsList = new ArrayList<>();
      //New productlist
      Product product;
      // New adress object
      Address billingAddress;
      // New adress object
      Address shippingAddress;


      while (rs.next()) {

        //Setting the different variables needed to create an order - if you have no order created aldready.
        if (order == null) {
          user = UserController.formUser(rs);
          product = ProductController.createProduct(rs);

          lineItem = LineItemController.createLineItem(rs, product);
          lineItemsList.add(lineItem);

          billingAddress = AddressController.createBillingAddress(rs);
          shippingAddress = AddressController.createShippingAddress(rs);

          //creating the order
          order = createOrder1(rs, user, lineItemsList, billingAddress, shippingAddress);

        }else {
          //Should the resultset be more than one line, it means there are several bought products.
          //This will add them to the order.
          product = ProductController.createProduct(rs);
          lineItem = LineItemController.createLineItem(rs, product);
          order.getLineItems().add(lineItem);
        }
      }
      // Returns the build order
      return order;
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }finally {
      try {
        rs.close();

      } catch (SQLException h) {
        h.printStackTrace();
        try {
          dbCon.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static ArrayList<Order> getOrders() {
    ResultSet rs = null;

    try {
      // check for connection
      if (dbCon == null || dbCon.getConnection().isClosed()) {
        dbCon = new DatabaseController();
      }

      // Build SQL string to query
      String sql = "SELECT * FROM orders " +
              "inner join user ON orders.user_id = user.u_id " +
              "inner join line_item ON orders.o_id = line_item.order_id " +
              "inner join address AS ba ON orders.billing_address_id = ba.a_id " +
              "inner join address as sa ON orders.shipping_address_id = sa.a_id " +
              "inner join product ON line_item.product_id  = product.p_id " +
              "order by orders.o_id";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql);

      rs = preparedStatement.executeQuery();


      //Initialize a new arraylist
      ArrayList<Order> orders = new ArrayList<Order>();

      while (rs.next()) {

        // Declare User object
        User user;
        // Declare New lineitem object
        LineItem lineItem;
        // Declare New adress object
        Address billingAddress;
        // Declare New adress object
        Address shippingAddress;
        // Declare new product object
        Product product;
        //Initializing New LineitemList
        ArrayList<LineItem> lineItemsList = new ArrayList<>();

        //Setting the different variables needed to create an order - if you have no order created aldready.
        //Or the orderID has changed
        if (orders.isEmpty() || rs.getInt("o_id") != orders.get(orders.size() - 1).getId()) {

          user = UserController.formUser(rs);
          product = ProductController.createProduct(rs);

          lineItem = LineItemController.createLineItem(rs, product);
          lineItemsList.add(lineItem);

          billingAddress = AddressController.createBillingAddress(rs);
          shippingAddress = AddressController.createShippingAddress(rs);

          //Creating the order and adding it to arraylist
          Order order = createOrder1(rs, user, lineItemsList, billingAddress, shippingAddress);
          orders.add(order);

          //Next if-block checks for, if an order has multiple products, and adds to lineitemslist, and adds them to order.
        } else if (rs.getInt("o_id") == orders.get(orders.size() - 1).getId()) {

          product = ProductController.createProduct(rs);
          lineItem = LineItemController.createLineItem(rs, product);

          lineItemsList.add(lineItem);
          orders.get(orders.size() - 1).getLineItems().add(lineItem);
        }

      }

      if (orders != null)//return the build orders as arraylist.
        return orders;

    } catch (SQLException e){
      e.printStackTrace();
    } finally {
      try {
        rs.close();
      } catch (SQLException h) {
        h.printStackTrace();
        try {
          dbCon.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }


  public static Order createOrder(Order order) {


    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    try {
// Check for DB Connection
      if (dbCon == null || dbCon.getConnection().isClosed()) {
        dbCon = new DatabaseController();
      }

      //We set the autocommit to false, making the way to use transactions
      DatabaseController.getConnection().setAutoCommit(false);

      //Setting the IDs of billing- and shippingAddress to the order
      //in other words: Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));


      //Setting the ID of the user to the order.
      order.setCustomer(UserController.getUser(order.getCustomer().getId()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts: FIX

      //Building SQL statement and executing query
      String sql = "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, order_created_at, order_updated_at) VALUES(?,?,?,?,?,?)";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, order.getCustomer().getId());
      preparedStatement.setInt(2, order.getBillingAddress().getId());
      preparedStatement.setInt(3, order.getShippingAddress().getId());
      preparedStatement.setFloat(4, order.calculateOrderTotal());
      preparedStatement.setLong(5, order.getCreatedAt());
      preparedStatement.setLong(6, order.getUpdatedAt());

      int orderID = preparedStatement.executeUpdate();

      // Get our key back in order to apply it to an object as ID
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      if (generatedKeys.next()&&orderID==1) {
        order.setId(generatedKeys.getInt(1));
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database with the respective order id
      for (LineItem item : order.getLineItems()) {
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      //Add line items to the order, commit and return the order
      order.setLineItems(items);
      DatabaseController.getConnection().commit();
      return  order;

      // adding nullpointerexception, since we are using getUser() instead of createUser() - we would like people to be
      // logged in before they create an order - like Amazon.
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      if (dbCon.getConnection()!=null) {
        try {
          //If exception was catched, we roll our statements to the database back.
          System.out.println("rolling back");
          DatabaseController.getConnection().rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
    } finally {
      try {
        dbCon.getConnection().close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return  order;
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

