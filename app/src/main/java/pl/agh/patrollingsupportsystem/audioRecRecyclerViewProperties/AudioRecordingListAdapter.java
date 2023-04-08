package pl.agh.patrollingsupportsystem.audioRecRecyclerViewProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.agh.patrollingsupportsystem.R;

public class AudioRecordingListAdapter extends RecyclerView.Adapter<AudioRecordingListAdapter.AudioRecordingViewHolder>{

    RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<AudioRecordingGeneral> list;

    public AudioRecordingListAdapter(Context context, ArrayList<AudioRecordingGeneral> list, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public AudioRecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.audio_recording_list_item, parent, false);
        return new AudioRecordingViewHolder(v, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioRecordingViewHolder holder, int position) {
        AudioRecordingGeneral audioRecordingGeneral= list.get(position);
        holder.fileName.setText(audioRecordingGeneral.getFileName());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class AudioRecordingViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;

        public TextView getFileName() {
            return fileName;
        }

        public AudioRecordingViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            fileName = itemView.findViewById(R.id.tvFileName);

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
