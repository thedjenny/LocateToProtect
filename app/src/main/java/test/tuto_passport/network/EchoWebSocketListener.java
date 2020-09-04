package test.tuto_passport.network;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;


import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class EchoWebSocketListener extends WebSocketListener{

    ArrayList<String> messageList = new ArrayList<>();

    public OkHttpClient client;
    EchoWebSocketListener listener;

        private static final int NORMAL_CLOSURE_STATUS = 4000;

        ArrayList<String> mesList;


        public EchoWebSocketListener() {
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
           Log.i("Listener" , "I am lisetning");

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.i(">>inComingMessage", "onMessage: " + text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
        }



        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        }
    }


