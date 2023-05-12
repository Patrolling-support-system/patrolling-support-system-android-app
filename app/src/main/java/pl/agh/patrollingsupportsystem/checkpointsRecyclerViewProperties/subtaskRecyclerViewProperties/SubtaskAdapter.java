package pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.subtaskRecyclerViewProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.checkpointsRecyclerViewProperties.CheckpointRvInterface;
import pl.agh.patrollingsupportsystem.models.SubtaskModel;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.ViewHolder>{

    private List<SubtaskModel> subtasksList;
    Context context;
    CheckpointRvInterface checkpointRvInterface;

    public SubtaskAdapter(List<SubtaskModel> subtasksList, Context context, CheckpointRvInterface checkpointRvInterface) {
        this.subtasksList = subtasksList;
        this.context = context;
        this.checkpointRvInterface = checkpointRvInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask_list, parent, false);
        return new ViewHolder(v, checkpointRvInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubtaskModel currentItem = subtasksList.get(position);
        holder.tvSubtaskName.setText(currentItem.getSubtaskName());

    }

    @Override
    public int getItemCount() {
        return subtasksList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvSubtaskName;

        public ViewHolder(@NonNull View itemView, CheckpointRvInterface checkpointRvInterface) {
            super(itemView);
            tvSubtaskName = itemView.findViewById(R.id.textViewSubtaskName);

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
