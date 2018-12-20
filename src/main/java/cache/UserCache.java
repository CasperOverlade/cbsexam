package cache;

import controllers.UserController;
import java.util.ArrayList;
import model.User;
import utils.Config;

// TODO: Build this cache and use it : FIX


public class UserCache {

    // Liste over produkter
    private ArrayList<User> users;

    // Livstid på cache
    private long ttl;

    // Sætter hvornår cachen er blevet lavet
    private static long created;

    public UserCache() {this.ttl = Config.getUserTtl(); }

    public ArrayList<User> getUsers(Boolean forceUpdate) {

        // Hvis vi vil rydde cache, kan vi sætte force update
        // Ellers kigger vi på alderen af cachen, og finder ud af om vi skal opdatere.
        // Hvis listen er tom checkes der for nye users
        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis()))
                || this.users==null) {

            // Siden der skal opdateres, for vi userne fra controlleren
            ArrayList<User> users = UserController.getUsers();

            System.out.println("Cachen virker");

            // Sætter users for instancen og sætter tiden
            this.users = users;
            this.created = System.currentTimeMillis();
        }

        // Returner dokumenterne
        return this.users;
    }

    public User getUser(boolean forceUpdate, int userID) {
        User user = new User();

        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis())) || this.users==null) {

            // Hvis cache skal opdateres: Bruges productcontroller for at få user fra database
            user = UserController.getUser(userID);

            return user;
        } else {
            // Hvis cachen er ok, gå igennem arraylist indtil det rigtige id er fundet
            for (User u : users){
                if (userID==u.getId())
                    user = u;
                return user;
            }
        }

        return null;
    }
}
