package test.tuto_passport.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import test.tuto_passport.LoginActivity;
import test.tuto_passport.MainActivity;
import test.tuto_passport.R;
import test.tuto_passport.TokenManager;
import test.tuto_passport.network.ApiService;


public class FirebaseMessageReciever extends FirebaseMessagingService {
    TokenManager tokenManager ;

    public String getFirebaseMessageReciever(){

       return FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("msg rec" , remoteMessage.getData().toString());
        if(remoteMessage.getData().size()>0){
            showNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("message"));
        }

        if(remoteMessage.getNotification()!=null){
            showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }
    }

    public void showNotification(String title , String message){

        Intent intent = null ;
        intent = new Intent(this, LoginActivity.class);


        String channel_id = "l2p_channel";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri uri  = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),channel_id)
                .setSmallIcon(R.drawable.person_icon)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000,1000,1000,1000})
                .setContentIntent(pendingIntent);
       /* if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            builder = builder.setContent(getCustomDesign(title,message))
        }*/

        builder=builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.facebook_icon);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "l2p",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(uri,null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0,builder.build());
    }


}
