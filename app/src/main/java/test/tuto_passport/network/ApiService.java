package test.tuto_passport.network;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.squareup.moshi.Json;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.entities.PostResponse;
import test.tuto_passport.entities.Room;
import test.tuto_passport.entities.RoomResponse;
import test.tuto_passport.entities.User;
import test.tuto_passport.entities.UserResponse;
import test.tuto_passport.entities.UuidResponse;

public interface ApiService {

    @POST("register")
    @FormUrlEncoded
    Call<AccessToken> register(@Field("name") String name,
                               @Field("email") String email,
                               @Field("password") String password ,
                               @Field("uid_phone") String uuid_phone,
                               @Field("firebase_token") String firebase_token,
                                @Field("firebase_img") String firebase_img);

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("username") String username, @Field("password") String password);

    @POST("social_auth")
    @FormUrlEncoded
    Call<AccessToken> socialAuth(@Field("name") String name,
                                 @Field("email") String email,
                                 @Field("provider") String provider,
                                 @Field("provider_user_id") String providerUserId);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @GET("posts")
    Call<PostResponse> posts();

    @GET("uuid")
    Call<UuidResponse> getUUID();

    @GET("rooms")
    Call<RoomResponse> getRooms();


    @GET("me")
    Call<String> getMyId();

    @POST("updatePassword")
    @FormUrlEncoded
    Call<AccessToken> update(@Field("email") String email, @Field("password") String password);

    @POST("logout")
    @FormUrlEncoded
    Call<Boolean> logout(@Field("access_token") String accessToken);

   @POST("currentUser")
   @FormUrlEncoded
    Call<String> getCurrentUser(@Field("uid_phone") String uid_phone);

    @POST("currentUserImg")
    @FormUrlEncoded
    Call<String> getCurrentFirebaseImg(@Field("uid_phone") String uuid2);
}
