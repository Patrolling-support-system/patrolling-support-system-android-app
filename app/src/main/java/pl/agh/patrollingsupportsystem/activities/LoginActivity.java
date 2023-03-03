package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import pl.agh.patrollingsupportsystem.R;

public class LoginActivity extends AppCompatActivity {

    EditText etLoginEmail;
    EditText etLoginPassword;
    Button btn;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.editTextEmailAddress);
        etLoginPassword = findViewById(R.id.editTextPassword);
        btn = (Button)findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();

        btn.setOnClickListener(v -> {
            //startActivity(new Intent(LoginActivity.this, MenuActivity.class));
            String email = etLoginEmail.getText().toString();
            String password = etLoginPassword.getText().toString();

            if (TextUtils.isEmpty(email)){
                etLoginEmail.setError("Email cannot be empty");
                etLoginEmail.requestFocus();
            }else if (TextUtils.isEmpty(password)){
                etLoginPassword.setError("Password cannot be empty");
                etLoginPassword.requestFocus();
            }else{
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

}