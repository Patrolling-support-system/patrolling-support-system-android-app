package pl.agh.patrollingsupportsystem.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.agh.patrollingsupportsystem.R;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etOldPassword;
    EditText etNewPassword;
    Button btnConfirmChangePassword;
    FirebaseUser fbUser;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //Layout elements
        etOldPassword = findViewById(R.id.editTextOldPassword);
        etNewPassword = findViewById(R.id.editTextNewPassword);
        btnConfirmChangePassword = findViewById(R.id.buttonConfirmChangePassword);

        //Firebase user for instance
        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        btnConfirmChangePassword.setOnClickListener(v -> {
            String newPassword = String.valueOf(etNewPassword.getText());
            String oldPassword = String.valueOf(etOldPassword.getText());
            if (TextUtils.isEmpty(oldPassword)) {
                etOldPassword.setError("Old Password cannot be empty");
                etOldPassword.requestFocus();
            } else if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("New Password cannot be empty");
                etNewPassword.requestFocus();
            } else {
            AuthCredential credential = EmailAuthProvider.getCredential(fbUser.getEmail(), oldPassword);

            //Reauthentication to change password for logged user
            fbUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            fbUser.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT);
                                    startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, "Password update failed", Toast.LENGTH_LONG);
                                }
                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Authentication error", Toast.LENGTH_LONG);
                        }
                    });
            }
        });
    }
}