package test.tuto_passport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.entities.ApiError;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

public class RegisterActivity extends AppCompatActivity  {

    private static final String TAG = "RegisterActivity";

    @BindView(R.id.til_name)
    TextInputLayout tilName;
    @BindView(R.id.til_email)
    TextInputLayout tilEmail;
    @BindView(R.id.til_password)
    TextInputLayout tilPassword;

    private Button pickimg, saveimg;
    private ImageView nimage;
    private final int IMG_REQUEST = 1;
    private Bitmap bitmapsimg;
    ApiService service;
    Call<AccessToken> call;
    AwesomeValidation validator;
    TokenManager tokenManager;
    public Uri imageUri;
    private static String firebaseImg;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseImg ="https://firebasestorage.googleapis.com/v0/b/fir-messagesapp-3bcef.appspot.com/o/images%2Fd42830d1-76f5-44bf-8e17-38345ff377ea?alt=media&token=60263cea-09b2-42a2-95c7-c8ca0294fb09";
        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        setupRules();
        pickimg = (Button) findViewById(R.id.getimage);
        storage = FirebaseStorage.getInstance("gs://fir-messagesapp-3bcef.appspot.com");
        storageReference = storage.getReference();
        firebaseImg ="https://firebasestorage.googleapis.com/v0/b/fir-messagesapp-3bcef.appspot.com/o/images%2Fd42830d1-76f5-44bf-8e17-38345ff377ea?alt=media&token=60263cea-09b2-42a2-95c7-c8ca0294fb09";



        if(tokenManager.getToken().getAccessToken() != null){
            startActivity(new Intent(RegisterActivity.this, PostActivity.class));
            finish();
        }
    }

    @OnClick(R.id.getimage)
    void uploadImg(){
        choosePicture();
    }

    @OnClick(R.id.btn_register)
    void register(){

        String name = tilName.getEditText().getText().toString();
        String email = tilEmail.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        validator.clear();

        if(validator.validate()) {

            call = service.register(name, email, password ,java.util.UUID.randomUUID().toString() , firebaseToken , firebaseImg);
            Log.i(TAG,"my token :: "+firebaseToken);
            call.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                    Log.w(TAG, "onResponse: " + response);

                    if (response.isSuccessful()) {
                        Log.w("registered successfull ", "onResponse: " + response.body() );
                        tokenManager.saveToken(response.body());
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                      handleErrors(response.errorBody());
                    }

                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Log.w("failed to register", "onFailure: " + t.getMessage());
                }
            });
        }
    }

    @OnClick(R.id.go_to_login)
    void goToRegister(){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }


    private void handleErrors(ResponseBody response){

        ApiError apiError = Utils.converErrors(response);

        for(Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()){
            if(error.getKey().equals("name")){
                tilName.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("email")){
                tilEmail.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("password")){
                tilPassword.setError(error.getValue().get(0));
            }
        }

    }

    public void setupRules(){

        validator.addValidation(this, R.id.til_name, RegexTemplate.NOT_EMPTY, R.string.err_name);
        validator.addValidation(this, R.id.til_email, Patterns.EMAIL_ADDRESS, R.string.err_email);
        validator.addValidation(this, R.id.til_password, "[a-zA-Z0-9]{6,}", R.string.err_password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(call != null) {
            call.cancel();
            call = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data != null && data.getData()!=null){
            imageUri = data.getData();

            picToFirebase();
        }
    }

    private void picToFirebase() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("uploading Image ...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();

        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                firebaseImg = uri.toString();

                            }
                        });
                        Snackbar.make(findViewById(android.R.id.content),"Image uploaded",Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i("exception upload ",exception.toString());
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"failed to upload image ",Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                pd.setMessage("percentage: " + (int) progressPercent + "%");
            }
        });



    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }
}
