package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import pl.agh.patrollingsupportsystem.R;

public class MenuActivity extends AppCompatActivity {

    Button btnAccountDetails;
    Button btnTaskList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Layout elements
        btnAccountDetails = findViewById(R.id.buttonAccountDetails);
        btnTaskList = findViewById(R.id.buttonTaskList);

        btnAccountDetails.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, AccountDetailsActivity.class)));
        btnTaskList.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TaskListActivity.class)));
    }
}