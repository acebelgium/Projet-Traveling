package com.example.traveling.share;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.traveling.databinding.ItemGroupBinding;
import java.util.List;
import java.util.Map;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Map<String, Object>> groups;
    private final FirestoreRepository repository = new FirestoreRepository();

    public GroupAdapter(List<Map<String, Object>> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> group = groups.get(position);
        String name = (String) group.get("name");
        String desc = (String) group.get("description");
        String id = (String) group.get("id");

        holder.binding.tvGroupName.setText(name);
        holder.binding.tvGroupDesc.setText(desc);

        // Vérifier si membre pour changer le bouton
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        boolean isAnonymous = currentUser == null || currentUser.isAnonymous();

        if (isAnonymous) {
            holder.binding.btnJoin.setVisibility(android.view.View.GONE);
            holder.itemView.setOnClickListener(v -> {
                android.widget.Toast.makeText(v.getContext(), "Connectez-vous pour rejoindre ce groupe", android.widget.Toast.LENGTH_SHORT).show();
            });
            return;
        }

        holder.binding.btnJoin.setVisibility(android.view.View.VISIBLE);
        repository.isMember(id, isMember -> {
            updateButtonUI(holder, isMember);
            
            holder.binding.btnJoin.setOnClickListener(v -> {
                if (isMember) {
                    repository.leaveGroup(id, aVoid -> notifyItemChanged(position));
                } else {
                    repository.joinGroup(id, aVoid -> notifyItemChanged(position));
                }
            });

            // Si membre, on peut cliquer sur le groupe pour ouvrir le tchat
            holder.itemView.setOnClickListener(v -> {
                if (isMember) {
                    Intent intent = new Intent(v.getContext(), GroupChatActivity.class);
                    intent.putExtra("groupId", id);
                    intent.putExtra("groupName", name);
                    v.getContext().startActivity(intent);
                } else {
                    android.widget.Toast.makeText(v.getContext(), "Rejoignez le groupe pour voir le tchat", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateButtonUI(ViewHolder holder, boolean isMember) {
        if (isMember) {
            holder.binding.btnJoin.setText("QUITTER");
            holder.binding.btnJoin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.LTGRAY));
        } else {
            holder.binding.btnJoin.setText("REJOINDRE");
            holder.binding.btnJoin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(com.example.traveling.R.color.primary)));
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemGroupBinding binding;
        public ViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
