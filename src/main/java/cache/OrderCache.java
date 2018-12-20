package cache;

import controllers.OrderController;

import java.util.ArrayList;

import model.Order;
import utils.Config;

// TODO: Build this cache and use it : FIX

public class OrderCache {

    // Liste over ordrer
    private ArrayList<Order> orders;

    // Livstid på cache
    private long ttl;

    // Sætter hvornår cachen er blevet lavet
    private long created;

    public OrderCache() {this.ttl = Config.getOrderTtl();
    }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {

        // Hvis vi vil rydde cache, kan vi sætte force update
        // Ellers kigger vi på alderen af cachen, og finder ud af om vi skal opdatere.
        // Hvis listen er tom checkes der for nye ordre
        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L))
                || this.orders.isEmpty()) {

            // Siden der skal opdateres, for vi ordrene fra controlleren
            ArrayList<Order> orders = OrderController.getOrders();

            System.out.println("TestOrder");

            // Sætter ordre for instancen og sætter tiden
            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Returner dokumenterne
        return this.orders;
    }

    public Order getOrder(boolean forceUpdate, int orderID) {
        Order order = new Order();


        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis())) || this.orders==null) {

            // Hvis cache skal opdateres: Bruger ordercontroller for at få ordre fra database
            order = OrderController.getOrder(orderID);

            return order;
        } else {
            // Hvis cachen er ok, gå igennem arraylist indtil det rigtige id er fundet
            for (Order o : orders){
                if (orderID==o.getId())
                    order = o;
                return order;
            }
        }
        return null;
    }

}
