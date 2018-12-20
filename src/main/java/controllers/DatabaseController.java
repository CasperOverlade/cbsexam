package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import utils.Config;
import model.User;

public class DatabaseController {

  private static Connection connection;

  public DatabaseController() {
    connection = getConnection();
  }

  /**
   * Get database connection
   *
   * @return a Connection object
   */
  public static Connection getConnection() {
    try {

      if (connection == null) {
        // Sætter database forbindelse med data fra config
        String url =
                "jdbc:mysql://"
                        + Config.getDatabaseHost()
                        + ":"
                        + Config.getDatabasePort()
                        + "/"
                        + Config.getDatabaseName()
                        + "?serverTimezone=CET";

        String user = Config.getDatabaseUsername();
        String password = Config.getDatabasePassword();

        // Registrer driver for at bruge den
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());

        // Opretter forbindelse til databasen
        connection = DriverManager.getConnection(url, user, password);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    return connection;
  }

  /**
   * Do a query in the database
   *
   * @return a ResultSet or Null if Empty
   */
  public ResultSet query(String sql) {

    // Checker om vi har forbindelse
    if (connection == null)
      connection = getConnection();


    // Vi sætter resultset som tom
    ResultSet rs = null;

    try {
      // Bygger statement som et prepared statement
      PreparedStatement stmt = connection.prepareStatement(sql);

      // Actually fire the query to the DB
      rs = stmt.executeQuery();

      // Returnere resultaterne
      return rs;
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Returnere resultset som på dette punkt vil være null
    return rs;
  }

  public int insert(String sql) {

    // Sætter 'key' til 0 som start
    int result = 0;

    // Checker at vi har forbindelse
    if (connection == null)
      connection = getConnection();

    try {
      // Bygger statement på en sikker måde
      PreparedStatement statement =
          connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      // Execute query
      result = statement.executeUpdate();

      // Får key tilbage for at updatere brugeren
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Returnere resultSet som vil være null
    return result;
  }

  //Opdatering i DB. Returnerer true hvis update og false hvis ikke
  public boolean update (String sql) {

    //
    if (connection == null)
      connection = getConnection();

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(sql);

      int rowaffected = preparedStatement.executeUpdate();

      if (rowaffected==1)
        return true;

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

    public boolean delete(String sql) {

        // Checker at vi har forbindelse
        if (connection == null)
            connection = getConnection();

        try {
            // Bygger statement som et prepared statement
            PreparedStatement stmt = connection.prepareStatement(sql);


            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                return true;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }
}
