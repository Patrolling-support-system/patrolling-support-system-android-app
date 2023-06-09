package pl.agh.patrollingsupportsystem.recyclerViews.audioRecordings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.agh.patrollingsupportsystem.R;
import pl.agh.patrollingsupportsystem.recyclerViews.models.AudioRecording;

public class AudioRecordingListAdapter extends RecyclerView.Adapter<AudioRecordingListAdapter.AudioRecordingViewHolder>{
    Context context;
    ArrayList<AudioRecording> recordingList;

    public AudioRecordingListAdapter(Context context, ArrayList<AudioRecording> recordingList) {
        this.context = context;
        this.recordingList = recordingList;
    }

    @NonNull
    @Override
    public AudioRecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.audio_recording_list_item, parent, false);
        return new AudioRecordingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioRecordingViewHolder holder, int position) {
        AudioRecording audioRecording = recordingList.get(position);
        holder.tvFileName.setText(audioRecording.getFileName());

    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    public static class AudioRecordingViewHolder extends RecyclerView.ViewHolder{
        TextView tvFileName;

        public AudioRecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.textViewFileName);
        }
    }

}
