package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import model.Product;
import utils.Log;

public class ProductController {

  private static DatabaseController dbCon;

  public ProductController() {
    dbCon = new DatabaseController();
  }


  public static Product getProduct(int id) {

    ResultSet rs = null;
    try {
      // check for connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      //Building SQL statement and executing query
      String sql = "SELECT * FROM product where p_id = ?";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql);
      preparedStatement.setInt(1, id);

      rs = preparedStatement.executeQuery();

      Product product = null;

      // Get first row and initialize through formProduct() the object and return it
      if (rs.next()) {
        product = createProduct(rs);

        // Return the product
        return product;
      } else {
        System.out.println("No product found");
      }
    }catch (SQLException e){
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
    // Return empty object
    return null;
  }


  public static Product getProductBySku(String sku) {

    ResultSet rs = null;

    try {
      // check for connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      //Building SQL statement and executing query
      String sql = "SELECT * FROM product where sku = ?";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql);
      preparedStatement.setString(1, sku);

      rs = preparedStatement.executeQuery();

      Product product = null;

      // Get first row and initialize through formProduct() the object and return it
      if (rs.next()) {
        product = createProduct(rs);
        return product;
      } else {
        System.out.println("No product found");
      }
    }catch (SQLException e){
      e.printStackTrace();
    }
    return null;
  }


  public static ArrayList<Product> getProducts() {

    ResultSet rs = null;

    try {
      //Check for connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      //Building SQL statement and executing query
      String sql = "SELECT * FROM product";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql);

      rs = preparedStatement.executeQuery();

      // TODO: Use caching layer. FIX - See caching
      ArrayList<Product> products = new ArrayList<Product>();

      // Get first row and initialize through formProduct() the object. Then second row and so on. Add to arraylist
      //and return.
      while (rs.next()) {
        Product product = createProduct(rs);
        products.add(product);
      }
      return products;
    }catch(SQLException ex){
      System.out.println(ex.getMessage());
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

  public static Product createProduct(Product product) {
    try {
      // Write in log that we've reach this step
      Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

      // Set creation time for product.
      product.setCreatedTime(System.currentTimeMillis());


      // Check for DB Connection
      if (dbCon == null || dbCon.getConnection().isClosed()) {
        dbCon = new DatabaseController();
      }

      //Building SQL statement and executing query
      String sql = "INSERT INTO product(product_name, sku, price, description, stock, product_created_at) VALUES(?,?,?,?,?,?)";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, product.getName());
      preparedStatement.setString(2, product.getSku());
      preparedStatement.setFloat(3, product.getPrice());
      preparedStatement.setString(4, product.getDescription());
      preparedStatement.setLong(5, product.getStock());
      preparedStatement.setLong(6, product.getCreatedTime());

      int rowsAffected = preparedStatement.executeUpdate();

      // Get our key back in order to apply it to an object as ID
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      if (generatedKeys.next()&&rowsAffected==1) {
        product.setId(generatedKeys.getInt(1));
        return product;

      } else {
        // Return null if product has not been inserted into database
        return null;
      }
    }catch (SQLException e){
      e.printStackTrace();
    } finally {
      try {
        dbCon.getConnection().close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    // Return product
    return product;
  }

  public static Product createProduct(ResultSet rs) {
    try {
      Product product = new Product(rs.getInt("p_id"),
              rs.getString("product_name"),
              rs.getString("sku"),
              rs.getFloat("price"),
              rs.getString("description"),
              rs.getInt("stock"));

      return product;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
