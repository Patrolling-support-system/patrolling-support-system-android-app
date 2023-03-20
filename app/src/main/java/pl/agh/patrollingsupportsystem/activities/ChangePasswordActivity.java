package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.agh.patrollingsupportsystem.R;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText oldPassword, newPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        Button changePassword = findViewById(R.id.changePassword);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmailAddress = user.getEmail();

        changePassword.setOnClickListener(v -> {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(userEmailAddress, String.valueOf(oldPassword.getText()));

            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(String.valueOf(newPassword.getText())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Password updated");
                                            //TODO Add Toast
                                            startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                                        } else {
                                            Log.d(TAG, "Error password not updated");
                                            //TODO Add Toast
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error auth failed");
                                //TODO Add Toast
                            }
                        }
                    });
        });
    }
}