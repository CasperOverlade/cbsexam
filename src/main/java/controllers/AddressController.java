package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import model.Address;
import utils.Log;

public class AddressController {

  private static DatabaseController dbCon;

  public AddressController() {
    dbCon = new DatabaseController();
  }

  public static Address createAddress(Address address) {

    // Skriver i log at vi er her
    Log.writeLog(ProductController.class.getName(), address, "Actually creating a line item in DB", 0);

    // Checker forbindelse til DB
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Indsætter produktet i DB
    int addressID = dbCon.insert(
        "INSERT INTO address(name, city, zipcode, street_address) VALUES('"
            + address.getName()
            + "', '"
            + address.getCity()
            + "', '"
            + address.getZipCode()
            + "', '"
            + address.getStreetAddress()
            + "')");

    if (addressID != 0) {
      //Update the productid of the product before returning
      address.setId(addressID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product, will be null at this point
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
