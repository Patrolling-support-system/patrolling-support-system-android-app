package pl.agh.patrollingsupportsystem.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;
import pl.agh.patrollingsupportsystem.recyclerViews.models.SubtaskExtended;
import pl.agh.patrollingsupportsystem.recyclerViews.subtasks.SubtaskListAdapter;

public class SubtaskListActivity extends AppCompatActivity implements RecyclerViewInterface {

    TextView tvCheckpointCoordinates;
    RecyclerView rvSubtasks;
    FirebaseFirestore fbDb;
    List<SubtaskExtended> subtaskList;
    List<String> subtaskDocumentList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask);

        fbDb = FirebaseFirestore.getInstance();
        subtaskList = new ArrayList<>();
        subtaskDocumentList = new ArrayList<>();

        Bundle documentExtras = getIntent().getExtras();
        Double latitude = null;
        Double longitude = null;
        if (documentExtras != null) {
            latitude = documentExtras.getDouble("checkpoint_latitude");
            longitude = documentExtras.getDouble("checkpoint_longitude");
        }
        System.out.println(latitude + " " + longitude);

        rvSubtasks = findViewById(R.id.recyclerViewSubtaskList);
        rvSubtasks.setLayoutManager(new LinearLayoutManager(this));
        SubtaskListAdapter subtaskListAdapter = new SubtaskListAdapter( this, subtaskList, this);

        tvCheckpointCoordinates = findViewById(R.id.textViewCheckpointCoordinates);
        tvCheckpointCoordinates.setText(latitude + " | " + longitude);
        GeoPoint checkpoint = new GeoPoint(latitude, longitude);


        fbDb.collection("CheckpointSubtasks").whereEqualTo("checkpoint", checkpoint)
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        Log.e("Firestore error ", error.getMessage());
                        return ;
                    }
                    for (DocumentChange dc: value.getDocumentChanges()){
                        if (dc.getType() == DocumentChange.Type.ADDED){
                            //tvCheckpointCoordinates.setText(dc.getDocument().get("SubtaskName").toString());
                            subtaskList.add(dc.getDocument().toObject(SubtaskExtended.class));
                            subtaskDocumentList.add(dc.getDocument().getId());
                            subtaskListAdapter.notifyDataSetChanged();
                        }

                    }
                });

        rvSubtasks.setAdapter(subtaskListAdapter);

    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(SubtaskListActivity.this, SubtaskDetailsActivity.class);
        i.putExtra("subtask_document", subtaskDocumentList.get(position));
        startActivity(i);
    }
}