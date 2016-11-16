package co.com.exile.piscix.notix;

import android.content.Context;


public class NotixFactory {

    public static Notix buildNotix(Context context) {
        Notix notix = Notix.getInstance();
        if (!notix.hasUser()) {
            notix.setUser(context);
        }
        return notix;
    }
}
