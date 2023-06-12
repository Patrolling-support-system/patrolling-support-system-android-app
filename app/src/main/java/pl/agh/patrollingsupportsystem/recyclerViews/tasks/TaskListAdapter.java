package pl.agh.patrollingsupportsystem.recyclerViews.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.RecyclerViewInterface;
import pl.agh.patrollingsupportsystem.recyclerViews.models.Task;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ActionViewHolder> {
    Context context;
    ArrayList<Task> taskList;
    RecyclerViewInterface recyclerViewInterface;


    public TaskListAdapter(Context context, ArrayList<Task> taskList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.taskList = taskList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false);
        return new ActionViewHolder(v, recyclerViewInterface);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        Task taskModel = taskList.get(position);
        holder.tvTaskName.setText(taskModel.getName());
        holder.tvTaskLocation.setText(taskModel.getLocation());
        holder.tvTaskStartDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(taskModel.getStartDate().toDate()));

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder{

        TextView tvTaskName;
        TextView tvTaskLocation;
        TextView tvTaskStartDate;

        public ActionViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            tvTaskName = itemView.findViewById(R.id.textViewTaskName);
            tvTaskLocation = itemView.findViewById(R.id.textViewTaskLocation);
            tvTaskStartDate = itemView.findViewById(R.id.textViewTaskStartDate);

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
