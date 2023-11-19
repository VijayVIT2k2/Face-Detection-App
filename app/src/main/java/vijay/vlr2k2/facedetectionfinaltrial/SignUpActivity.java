package vijay.vlr2k2.facedetectionfinaltrial;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtEmail,edtSignUser,edtSignPass;
    Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btnSignUp = findViewById(R.id.btnSignUp);
        edtEmail = findViewById(R.id.edtEmail);
        edtSignUser = findViewById(R.id.edtSignUser);
        edtSignPass = findViewById(R.id.edtSignPass);
        edtSignPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    onClick(btnSignUp);
                }
                return false;
            }
        });
        btnSignUp.setOnClickListener(SignUpActivity.this);
        if (ParseUser.getCurrentUser()!=null){
            ParseUser.getCurrentUser();
            ParseUser.logOut();
        }
    }

    public void goToLoginPage(View view) {
        Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    @Override
    public void onClick(View view) {
        if (edtEmail.getText().toString().equals("")|edtSignUser.getText().toString().equals("")|edtSignPass.getText().toString().equals("")){
            FancyToast.makeText(SignUpActivity.this,
                    "Contains Empty Fields",
                    FancyToast.LENGTH_SHORT,FancyToast.ERROR,
                    true).show();
        } else {
            final ParseUser appUser = new ParseUser();
            appUser.setEmail(edtEmail.getText().toString());
            appUser.setUsername(edtSignUser.getText().toString());
            appUser.setPassword(edtSignPass.getText().toString());

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Signing Up " + edtSignUser.getText().toString());
            progressDialog.show();

            appUser.signUpInBackground(e -> {
                if (e==null) {
                    FancyToast.makeText(SignUpActivity.this,
                            appUser.getUsername() + " is Signed Up",
                            FancyToast.LENGTH_SHORT, FancyToast.SUCCESS,
                            false).show();
                    transitionToFaceRecognitionActivity();
                }
                else
                    FancyToast.makeText(SignUpActivity.this,
                            "Error signing up user",
                            FancyToast.LENGTH_SHORT,FancyToast.ERROR,
                            true).show();
                progressDialog.dismiss();
            });
        }
    }
    public void rootLayoutTapped(View view){
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public  void transitionToFaceRecognitionActivity(){
        Intent intent = new Intent(SignUpActivity.this, FaceRecognitionActivity.class);
        intent.putExtra("name", "Face recognition");
        startActivity(intent);
        finish();
    }
}