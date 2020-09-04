package test.tuto_passport;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.FeneralSocketMethod;
import test.tuto_passport.network.RetrofitBuilder;

public class MapActivity extends AppCompatActivity {


    ApiService service;
    TokenManager tokenManager;
    Call<String> call;
    String ret;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ButterKnife.bind(this);

        /////////////////////////
        service = RetrofitBuilder.createService(ApiService.class);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        /////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        System.out.println("rani f map");
        WebView view = findViewById(R.id.view);
        view.setWebViewClient(new WebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
        view.loadUrl("http://192.168.43.31:8001/map2/8") ;//+ myId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //view.loadUrl("http://192.168.43.31/user/map");
/*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

    }

    private String myId(){

            call = service.getMyId();
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("my id ",  response.body());
                    ret = response.body();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("erreur id ",  t.toString());
                    ret = "";
                }
            });
            return ret;
    }
}
