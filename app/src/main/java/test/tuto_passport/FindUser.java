package test.tuto_passport;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.basgeekball.awesomevalidation.AwesomeValidation;

import butterknife.ButterKnife;
import retrofit2.Call;
import test.tuto_passport.entities.AccessToken;

import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

public class FindUser extends AppCompatActivity {

    private static final String TAG = "FindUser";


    ApiService service;
    TokenManager tokenManager;
    AwesomeValidation validator;
    Call<AccessToken> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        if(tokenManager.getToken() == null){
            startActivity(new Intent(FindUser.this, LoginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}