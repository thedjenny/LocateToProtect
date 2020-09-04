package test.tuto_passport.network;

import androidx.transition.TransitionManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.entities.Room;
import test.tuto_passport.entities.RoomResponse;

public class RoomListner {


    ApiService service;

    Call<RoomResponse> call;
    List<Room> rooms;

    private static final String TAG = "RoomListner";



    public List<Room> checkRooms() {

        service = RetrofitBuilder.createService(ApiService.class);


        call = service.getRooms();
        call.enqueue(new Callback<RoomResponse>() {
            @Override
            public void onResponse(Call<RoomResponse> call, Response<RoomResponse> response) {

                Log.w(TAG, "onResponse: " + response );

                if(response.isSuccessful()){

                    List<Room> rooms = response.body().getRooms();

                }else {


                }

            }

            @Override
            public void onFailure(Call<RoomResponse> call, Throwable t) {

                System.out.println("exceptionn +" + t);

            }
        });

        return rooms;
    }


}