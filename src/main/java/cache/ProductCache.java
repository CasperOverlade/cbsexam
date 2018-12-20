package cache;

import controllers.ProductController;
import java.util.ArrayList;
import model.Product;
import utils.Config;

public class ProductCache {

  // Liste over produkter
  private static ArrayList<Product> products;

  // Livstid på cache
  private long timetolive;

  // Sætter hvornår cachen er blevet lavet
  private static long created;

  public ProductCache() {
    this.timetolive = Config.getProductTtl();
  }

  public ArrayList<Product> getProducts(Boolean forceUpdate) {

    // Hvis vi vil rydde cache, kan vi sætte force update
    // Ellers kigger vi på alderen af cachen, og finder ud af om vi skal opdatere.
    // Hvis listen er tom checkes der for nye produkter
    if (forceUpdate
        || ((this.created + this.timetolive) <= (System.currentTimeMillis() / 1000L))
        || this.products==null) {

      // Siden der skal opdateres, for vi ordrene fra controlleren
      ArrayList<Product> products = ProductController.getProducts();

      System.out.println("Cachen virker");


      // Sætter ordre for instancen og sætter tiden
      this.products = products;
      this.created = System.currentTimeMillis() / 1000L;
    }

    // Returner dokumenterne
    return this.products;
  }

  public Product getProduct(boolean forceUpdate, int productID) {
    Product product = new Product();

    if (forceUpdate
            || ((this.created + this.timetolive) <= (System.currentTimeMillis())) || this.products==null) {

      // Hvis cache skal opdateres: Bruges productcontroller for at få produkt fra database
      product = ProductController.getProduct(productID);

      return product;
    } else {
      // Hvis cachen er ok, gå igennem arraylist indtil det rigtige id er fundet
      for (Product p : products){
        if (productID==p.getId())
          product = p;
        return product;
      }
    }
    return null;
  }
}
