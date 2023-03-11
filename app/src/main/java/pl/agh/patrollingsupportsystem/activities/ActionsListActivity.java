package pl.agh.patrollingsupportsystem.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViewProperties.ActionGeneral;
import pl.agh.patrollingsupportsystem.recyclerViewProperties.ActionListAdapter;
import pl.agh.patrollingsupportsystem.recyclerViewProperties.RecyclerViewInterface;

public class ActionsListActivity extends AppCompatActivity implements RecyclerViewInterface {

    RecyclerView recyclerView;
    FirebaseFirestore db;
    ActionListAdapter actionListAdapter;
    ArrayList<ActionGeneral> list;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions_list);

        recyclerView = findViewById(R.id.actionList);
        db = FirebaseFirestore.getInstance();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        actionListAdapter = new ActionListAdapter(this, list, this);
        recyclerView.setAdapter(actionListAdapter);

        EventChangeListener();


    }

    private void EventChangeListener() {
        db.collection("ActionList")
                .whereArrayContains("members", FirebaseAuth.getInstance().getUid())
                //.orderBy("actionName", Query.Direction.ASCENDING) //Temporary not needed and requires index
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.e("Firestore error ", error.getMessage());
                            return ;
                        }
                        for (DocumentChange dc: value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                list.add(dc.getDocument().toObject(ActionGeneral.class));
                            }

                            actionListAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Toast t = Toast.makeText(this, FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_LONG);
        t.show();
    }
}

