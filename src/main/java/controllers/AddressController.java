package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Address;
import utils.Log;

public class AddressController {

  private static DatabaseController dbCon;

  public AddressController() {
    dbCon = new DatabaseController();
  }

  public static Address createAddress(Address address) {


    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), address, "Actually creating an address in DB", 0);

    try {
      // Check for DB Connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      //Building SQL statement and executing query
      String sql = "INSERT INTO address(name, city, zipcode, street_address) VALUES(?,?,?,?)";

      PreparedStatement preparedStatement = dbCon.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, address.getName());
      preparedStatement.setString(2, address.getCity());
      preparedStatement.setString(3, address.getZipCode());
      preparedStatement.setString(4, address.getStreetAddress());

      int rowsAffected = preparedStatement.executeUpdate();

      // Get our key back in order to apply it to an object as ID
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      if (generatedKeys.next()&&rowsAffected==1) {
        address.setId(generatedKeys.getInt(1));
        return address;
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    // Return null if address has not been inserted into database
    return address;

  }

  //createbillingadress deklarere og instantiere et adresse objekt baseret på information fra resultsettet
  public static Address createBillingAddress(ResultSet rs) {
    try {
      Address address = new Address(rs.getInt("ba.a_id"),
              rs.getString("ba.name"),
              rs.getString("ba.street_address"),
              rs.getString("ba.city"),
              rs.getString("ba.zipcode")
      );

      return address;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  ////createshippingadress deklarere og instantiere et adresse objekt baseret på information fra resultsettet
  public static Address createShippingAddress(ResultSet rs) {
    try {
      Address address = new Address(rs.getInt("sa.a_id"),
              rs.getString("sa.name"),
              rs.getString("sa.street_address"),
              rs.getString("sa.city"),
              rs.getString("sa.zipcode")
      );

      return address;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }


}
