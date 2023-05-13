package pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.nestedSubtaskRecyclerViewProperties.SubtaskAdapter;
import pl.agh.patrollingsupportsystem.models.SubtaskModel;

public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.ViewHolder>{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    CheckpointRvInterface checkpointRvInterface;


    public CheckpointAdapter(Context context, List<GeoPoint> checkpoints, CheckpointRvInterface checkpointRvInterface) {
        this.context = context;
        this.checkpoints = checkpoints;
        this.checkpointRvInterface = checkpointRvInterface;
    }


    private List<GeoPoint> checkpoints;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_list, parent, false);
        return new ViewHolder(view, checkpointRvInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeoPoint checkpoint = checkpoints.get(position);
        holder.rvSubtasks.setLayoutManager(new LinearLayoutManager(context));
        holder.rvSubtasks.setHasFixedSize(true);


        holder.textViewCheckpointName.setText(String.valueOf(checkpoint.getLatitude()) + " | " + String.valueOf(checkpoint.getLongitude()));
        List<SubtaskModel> subtasks = new ArrayList<>();

        db.collection("CheckpointSubtasks").whereEqualTo("checkpoint", checkpoint)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            SubtaskModel subtask = document.toObject(SubtaskModel.class);
                            subtasks.add(subtask);
                            //documentList.add(document.getId());
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    SubtaskAdapter subtaskAdapter = new SubtaskAdapter(subtasks, holder.rvSubtasks.getContext());
                    holder.rvSubtasks.setAdapter(subtaskAdapter);
                });

    }


    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

//    @Override
//    public void onItemClick(int position) {
////        Intent i = new Intent(context, SubtaskActivity.class);
////        context.startActivity(i);
//        System.out.println(position);
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textViewCheckpointName;
        RecyclerView rvSubtasks;

        public ViewHolder(@NonNull View itemView, CheckpointRvInterface checkpointRvInterface){
            super(itemView);
            textViewCheckpointName = itemView.findViewById(R.id.textViewCheckpointName);
            rvSubtasks = itemView.findViewById(R.id.recyclerViewSubtask);

            itemView.setOnClickListener(view -> {
                if(checkpointRvInterface != null){
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        checkpointRvInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}

