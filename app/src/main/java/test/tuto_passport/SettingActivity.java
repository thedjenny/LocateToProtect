package test.tuto_passport;

import android.content.DialogInterface;
import android.content.Intent;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";


    @BindView(R.id.til_newPassword)
    TextInputLayout NewPassword;
    @BindView(R.id.til_ConfirmPassword)
    TextInputLayout ConfirmPassword;

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
            startActivity(new Intent(SettingActivity.this, LoginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @OnClick(R.id.btn_submit)
    void update(){
        System.out.println("hello ba");
        String newPass = NewPassword.getEditText().getText().toString();
        String confirmPass = ConfirmPassword.getEditText().getText().toString();


        NewPassword.setError(null);
        ConfirmPassword.setError(null);




            if(newPass.equals(confirmPass) && newPass!=null){
           this.confirmDecision();
            call = service.update(LoginActivity.getMail(), newPass);
            call.enqueue(new Callback<AccessToken>() {
                        @Override
                        public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                            Log.w(TAG, "onResponse: " + response);
                            if (response.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "password changed successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SettingActivity.this, MainActivity.class));
                                finish();
                            }


                        }

                        @Override
                        public void onFailure(Call<AccessToken> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "operation failed", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "onFailure: " + t.getMessage());
                        }
                    });

                }else{
                Toast.makeText(getApplicationContext(), "Password unmatched or incorrect", Toast.LENGTH_SHORT).show();
            }
        }




            @OnClick
    public void confirmDecision(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm password change!");
        builder.setMessage("You are about to change your password . Do you really want to proceed ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "password change cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


}