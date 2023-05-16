package pl.agh.patrollingsupportsystem.recyclerViews.chatRecyclerViewProperties;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.agh.patrollingsupportsystem.databinding.ItemContainerReceivedMessageBinding;
import pl.agh.patrollingsupportsystem.databinding.ItemContainerSentMessageBinding;
import pl.agh.patrollingsupportsystem.recyclerViews.models.ChatMessage;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final String senderId;
    public final String receiverId;

    private static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, String receiverId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position){
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        } else{ //chatMessages.get(position).receiverId.equals(coordinator)
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(@NonNull ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
        }
    }

    static class ReceivedMessageViewHolder extends  RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
        }
    }
}
