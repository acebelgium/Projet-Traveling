package com.example.traveling.share;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.R;
import com.example.traveling.databinding.ActivityGroupChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {
    private ActivityGroupChatBinding binding;
    private final FirestoreRepository repository = new FirestoreRepository();
    private final List<Map<String, Object>> messagesList = new ArrayList<>();
    private MessageAdapter adapter;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");
        binding.tvChatTitle.setText(groupName);

        setupRecyclerView();
        listenForMessages();

        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                repository.sendGroupMessage(groupId, text);
                binding.etMessage.setText("");
            }
        });

        binding.btnMembers.setOnClickListener(v -> showMembersDialog());
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messagesList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);
    }

    private void listenForMessages() {
        FirebaseFirestore.getInstance().collection("groups").document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messagesList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            messagesList.add(doc.getData());
                        }
                        adapter.notifyDataSetChanged();
                        binding.rvMessages.scrollToPosition(messagesList.size() - 1);
                    }
                });
    }

    private void showMembersDialog() {
        repository.getGroupMembersQuery(groupId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> names = new ArrayList<>();
            List<String> uids = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("userName");
                String uid = doc.getString("userId");
                if (name != null && uid != null) {
                    names.add(name);
                    uids.add(uid);
                }
            }
            
            String[] namesArray = names.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Membres du groupe")
                    .setItems(namesArray, (dialog, which) -> {
                        Intent intent = new Intent(this, UserProfileActivity.class);
                        intent.putExtra("userId", uids.get(which));
                        startActivity(intent);
                    })
                    .setPositiveButton("Fermer", null)
                    .show();
        });
    }

    private static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private final List<Map<String, Object>> messages;
        private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        MessageAdapter(List<Map<String, Object>> messages) { this.messages = messages; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Utiliser un layout personnalisé pour mieux afficher le nom
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> msg = messages.get(position);
            String text = (String) msg.get("text");
            String senderId = (String) msg.get("senderId");
            String senderName = (String) msg.get("senderName");
            
            holder.text1.setText(senderName != null ? senderName : "Inconnu");
            holder.text2.setText(text);

            boolean isMe = senderId.equals(currentUserId);
            holder.itemView.setTextAlignment(isMe ? View.TEXT_ALIGNMENT_VIEW_END : View.TEXT_ALIGNMENT_VIEW_START);
            holder.text1.setTextColor(isMe ? holder.itemView.getContext().getResources().getColor(R.color.primary) : 0xFF888888);
        }

        @Override
        public int getItemCount() { return messages.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) { 
                super(v); 
                text1 = v.findViewById(android.R.id.text1); 
                text2 = v.findViewById(android.R.id.text2); 
            }
        }
    }
}
