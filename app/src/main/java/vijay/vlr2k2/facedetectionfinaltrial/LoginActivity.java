package vijay.vlr2k2.facedetectionfinaltrial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtUsername,edtPassword;
    private Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(LoginActivity.this);
        if (ParseUser.getCurrentUser()!=null){
            transitionToFaceRecognitionActivity();
        }

    }
    public void goToSignUpPage(View view) {
        Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        final ParseUser appUser = new ParseUser();
//
        appUser.setUsername(edtUsername.getText().toString());
        appUser.setPassword(edtPassword.getText().toString());
        if (edtUsername.getText().toString().equals("")|edtPassword.getText().toString().equals("")){
            FancyToast.makeText(LoginActivity.this,
                    "Contains Empty Fields",
                    FancyToast.LENGTH_SHORT,FancyToast.ERROR,
                    true).show();
        } else {
            ParseUser.logInInBackground(edtUsername.getText().toString(),
                    edtPassword.getText().toString(),
                    (user, e) -> {
                        if (user != null && e == null) {
                            FancyToast.makeText(LoginActivity.this,
                                    user.getUsername() + " is logged in",
                                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS,
                                    false).show();
                            transitionToFaceRecognitionActivity();
                        } else
                            FancyToast.makeText(LoginActivity.this,
                                    "Error logging in user",
                                    FancyToast.LENGTH_SHORT, FancyToast.ERROR,
                                    true).show();

                    });
        }
    }
    public void rootLayoutTappedLogin(View view){
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public  void transitionToFaceRecognitionActivity(){
        Intent intent = new Intent(LoginActivity.this, FaceRecognitionActivity.class);
        intent.putExtra("name", "Face recognition");
        startActivity(intent);
        finish();
    }
}