package pl.agh.patrollingsupportsystem.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import pl.agh.patrollingsupportsystem.R;

public class UserAccountActivity extends AppCompatActivity {

    TextView userName, userSurname, userEmailAddress;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        userName = findViewById(R.id.userName);
        userSurname = findViewById(R.id.userSurname);
        userEmailAddress = findViewById(R.id.userEmailAddress);
        Button changePasswordButton = findViewById(R.id.changePasswordButton);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = authUser.getUid();
        String userEmailAddressText = authUser.getEmail();
        Task a = authUser.updatePassword("TEST");

        CollectionReference collectionRef = db.collection("User");
        Query query = collectionRef.whereEqualTo("userId", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String name = document.getString("name");
                        userName.setText(name);

                        String surname = document.getString("surname");
                        userSurname.setText(surname);
                    }
                } else {
                    Log.d(TAG, "Error getting user document: ", task.getException());
                }
            }
        });

        userEmailAddress.setText(userEmailAddressText);

        changePasswordButton.setOnClickListener(v ->{
            startActivity(new Intent(UserAccountActivity.this, ChangePasswordActivity.class));
        });
    }
}