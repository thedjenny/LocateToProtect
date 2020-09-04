package test.tuto_passport.network;



import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class FeneralSocketMethod  {

    ArrayList<String> messageList = new ArrayList<>();

    private OkHttpClient client;
    EchoWebSocketListener listener;
    WebSocket ws;


    public void socketSend () {

        /**launching websocket here*/
        client = new OkHttpClient();
        start();
        /**end here*/



        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command", "register");
            jsonObject.put("from", "9");
            jsonObject.put("id", "45");
            jsonObject.put("message", "Hello response");
            ws.send(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }




    }


    private void start() {
        /**method to connect replace xxx.xxx.x.xx this ip address with yours*/
        Request request = new Request.Builder().url("ws://192.168.43.31:8090").build();
        /**Parameter here are just views*/
        listener = new EchoWebSocketListener();
        /*use this ws object to send message on button click or anywhere*/
        ws = client.newWebSocket(request, listener);
        /**end here*/
        //client.dispatcher().executorService().shutdown();
    }
}
