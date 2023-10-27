package sdx.talk;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {
    public static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context applicationContext() {
        return instance.getApplicationContext();
    }
}
