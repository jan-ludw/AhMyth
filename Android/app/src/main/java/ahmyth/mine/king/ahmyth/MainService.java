package ahmyth.mine.king.ahmyth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {
    private static Context contextOfApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        contextOfApplication = getApplicationContext();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }
} 