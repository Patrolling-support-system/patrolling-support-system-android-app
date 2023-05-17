package pl.agh.patrollingsupportsystem.recyclerViews.subtasks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;
import pl.agh.patrollingsupportsystem.recyclerViews.models.SubtaskExtended;

public class SubtaskListAdapter extends RecyclerView.Adapter<SubtaskListAdapter.ViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    RecyclerViewInterface recyclerViewInterface;
    List<SubtaskExtended> subtasks;

    public SubtaskListAdapter(Context context, List<SubtaskExtended> subtasks, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.subtasks = subtasks;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_extended, parent, false);
        return new SubtaskListAdapter.ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubtaskExtended currentSubtask = subtasks.get(position);

        holder.tvSubtaskName.setText(currentSubtask.getSubtaskName());
        holder.tvSubtaskDescription.setText(currentSubtask.getDescription());

    }


    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvSubtaskName;
        TextView tvSubtaskDescription;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            tvSubtaskName = itemView.findViewById(R.id.textViewSubtaskName);
            tvSubtaskDescription = itemView.findViewById(R.id.textViewSubtaskDescription);

            itemView.setOnClickListener(view -> {
                if(recyclerViewInterface != null){
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
