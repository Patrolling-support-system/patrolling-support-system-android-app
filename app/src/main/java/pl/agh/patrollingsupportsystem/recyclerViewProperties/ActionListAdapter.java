package pl.agh.patrollingsupportsystem.recyclerViewProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.agh.patrollingsupportsystem.R;

public class ActionListAdapter extends RecyclerView.Adapter<ActionListAdapter.ActionViewHolder> {
    RecyclerViewInterface recyclerViewInterface;
    Context context;

    public ActionListAdapter(Context context, ArrayList<ActionGeneral> list, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    ArrayList<ActionGeneral> list;

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.action_list_item, parent, false);
        return new ActionViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        ActionGeneral actionGeneral= list.get(position);
        holder.name.setText(actionGeneral.getName());
        holder.someData.setText(actionGeneral.getLocation());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder{

        TextView name, someData;

        public TextView getName() {
            return name;
        }

        public ActionViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            name = itemView.findViewById(R.id.tvActionName);
            someData = itemView.findViewById(R.id.tvSomeData);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerViewInterface != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
