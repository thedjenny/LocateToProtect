package test.tuto_passport.network;
import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketClient {

    private WebSocketClient mWebSocketClient;

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.43.31:8090/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("hi");

            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", "message reçu");

             System.out.println("hellllloooooo shik : " + s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
       
    }





}
