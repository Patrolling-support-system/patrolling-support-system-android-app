package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import pl.agh.patrollingsupportsystem.R;

public class AccountDetailsActivity extends AppCompatActivity {

    TextView tvName, tvSurname, tvEmailAddress;
    Button btnChangePassword, btnLogout;
    FirebaseFirestore fbDb;
    FirebaseAuth fbAuth;
    FirebaseUser fbUser;

    String userId;
    String userEmailAddress;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        //Layout elements
        tvName = findViewById(R.id.textViewName);
        tvSurname = findViewById(R.id.textViewSurname);
        tvEmailAddress = findViewById(R.id.textViewEmailAddress);
        btnChangePassword = findViewById(R.id.buttonChangePassword);
        btnLogout = findViewById(R.id.buttonLogout);

        //Firebase instance and auth
        fbDb = FirebaseFirestore.getInstance();
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();

        //User information
        userId = fbAuth.getUid();
        userEmailAddress = fbUser.getEmail();

        //Values for fields
        tvEmailAddress.setText(userEmailAddress);

        //User collection query and values for fields
        fbDb.collection("User").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    String name = document.getString("name");
                    tvName.setText(name);

                    String surname = document.getString("surname");
                    tvSurname.setText(surname);
                }
            } else {
                Log.d(TAG, "Error getting user document: ", task.getException());
            }
        });

        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(AccountDetailsActivity.this, ChangePasswordActivity.class)));
        btnLogout.setOnClickListener(v -> {
            fbAuth.signOut();
            finish();
            startActivity(new Intent(AccountDetailsActivity.this, LoginActivity.class));
        });
    }
}