package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pl.agh.patrollingsupportsystem.R;

public class ActionsListActivity extends AppCompatActivity {
//Activity presents list of active actions assigned to logged user (probably implemented as buttons made by iteration)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions_list);

    }
}

