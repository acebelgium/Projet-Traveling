package com.example.traveling.share;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference postsRef = db.collection("posts");

    /**
     * Récupère les posts publics triés par date
     */
    public Query getPostsQuery() {
        return postsRef.whereEqualTo("visibility", "public")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Récupère les posts d'un groupe spécifique
     */
    public Query getGroupPostsQuery(String groupId) {
        return postsRef.whereEqualTo("visibility", groupId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Récupère les posts dont le lieu ou le titre commence par le terme recherché
     */
    public Query getSearchQuery(String searchTerm) {
        return postsRef.whereGreaterThanOrEqualTo("location", searchTerm)
                .whereLessThanOrEqualTo("location", searchTerm + "\uf8ff");
    }

    /**
     * Récupère les posts d'une catégorie spécifique (uniquement publics)
     */
    public Query getCategoryQuery(String category) {
        return postsRef.whereEqualTo("category", category)
                .whereEqualTo("visibility", "public");
    }

    /**
     * Récupère tous les points d'intérêt disponibles
     */
    public Query getLocationsQuery() {
        return db.collection("locations").orderBy("nom", Query.Direction.ASCENDING);
    }

    /**
     * S'abonner à un utilisateur
     */
    public void followUser(String targetUserId, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null || targetUserId == null) return;

        Map<String, Object> followData = new HashMap<>();
        followData.put("followerId", currentUserId);
        followData.put("followedId", targetUserId);
        followData.put("timestamp", System.currentTimeMillis());

        db.collection("follows")
                .document(currentUserId + "_" + targetUserId)
                .set(followData)
                .addOnSuccessListener(listener);
    }

    /**
     * Se désabonner
     */
    public void unfollowUser(String targetUserId, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) return;

        db.collection("follows")
                .document(currentUserId + "_" + targetUserId)
                .delete()
                .addOnSuccessListener(listener);
    }

    /**
     * Vérifier si on suit déjà cet utilisateur
     */
    public void isFollowing(String targetUserId, OnSuccessListener<Boolean> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) {
            listener.onSuccess(false);
            return;
        }

        db.collection("follows")
                .document(currentUserId + "_" + targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> listener.onSuccess(documentSnapshot.exists()));
    }

    /**
     * Récupère tous les utilisateurs (sauf soi-même)
     */
    public Query getAllUsersQuery() {
        return db.collection("users").orderBy("name").limit(20);
    }

    /**
     * Récupère la liste des groupes
     */
    public Query getGroupsQuery() {
        return db.collection("groups").orderBy("name");
    }

    /**
     * Créer un nouveau groupe
     */
    public void createGroup(String name, String description, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("name");
            
            Map<String, Object> group = new HashMap<>();
            group.put("name", name);
            group.put("description", description);
            group.put("createdBy", currentUserId);
            group.put("timestamp", System.currentTimeMillis());

            db.collection("groups").add(group).addOnSuccessListener(docRef -> {
                Map<String, Object> member = new HashMap<>();
                member.put("userId", currentUserId);
                member.put("userName", userName != null ? userName : "Anonyme");
                docRef.collection("members").document(currentUserId).set(member)
                        .addOnSuccessListener(aVoid -> successListener.onSuccess(null));
            }).addOnFailureListener(failureListener);
        });
    }

    /**
     * Rejoindre un groupe
     */
    public void joinGroup(String groupId, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("name");
            
            Map<String, Object> member = new HashMap<>();
            member.put("userId", currentUserId);
            member.put("userName", userName != null ? userName : "Anonyme");

            db.collection("groups").document(groupId).collection("members").document(currentUserId)
                    .set(member)
                    .addOnSuccessListener(listener);
        });
    }

    /**
     * Quitter un groupe
     */
    public void leaveGroup(String groupId, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) return;

        db.collection("groups").document(groupId).collection("members").document(currentUserId)
                .delete()
                .addOnSuccessListener(listener);
    }

    /**
     * Vérifier si on est membre d'un groupe
     */
    public void isMember(String groupId, OnSuccessListener<Boolean> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) {
            listener.onSuccess(false);
            return;
        }

        db.collection("groups").document(groupId).collection("members").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> listener.onSuccess(documentSnapshot.exists()));
    }

    /**
     * Envoyer un message dans le tchat du groupe
     */
    public void sendGroupMessage(String groupId, String text) {
        String currentUserId = auth.getUid();
        if (currentUserId == null || text.isEmpty()) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("name");
            
            Map<String, Object> message = new HashMap<>();
            message.put("senderId", currentUserId);
            message.put("senderName", userName != null ? userName : "Anonyme");
            message.put("text", text);
            message.put("timestamp", System.currentTimeMillis());

            db.collection("groups").document(groupId).collection("messages").add(message);
        });
    }

    /**
     * Récupère les membres d'un groupe
     */
    public Query getGroupMembersQuery(String groupId) {
        return db.collection("groups").document(groupId).collection("members").orderBy("userName");
    }

    /**
     * Récupère les groupes dont l'utilisateur est membre
     */
    public void getMyGroups(OnSuccessListener<List<Map<String, String>>> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        // On cherche dans la collection "groups" ceux qui contiennent l'utilisateur dans leurs membres
        // Pour simplifier le prototype, on va lister tous les groupes et filtrer (ou utiliser une collection dédiée)
        db.collection("groups").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, String>> myGroups = new ArrayList<>();
            for (com.google.firebase.firestore.QueryDocumentSnapshot groupDoc : queryDocumentSnapshots) {
                // On vérifie manuellement l'appartenance pour éviter les index complexes de collectionGroup
                groupDoc.getReference().collection("members").document(currentUserId).get().addOnSuccessListener(memberDoc -> {
                    if (memberDoc.exists()) {
                        Map<String, String> groupInfo = new HashMap<>();
                        groupInfo.put("id", groupDoc.getId());
                        groupInfo.put("name", groupDoc.getString("name"));
                        myGroups.add(groupInfo);
                    }
                    // Si on a fini de parcourir tous les groupes
                    if (queryDocumentSnapshots.getDocuments().indexOf(groupDoc) == queryDocumentSnapshots.size() - 1) {
                        listener.onSuccess(myGroups);
                    }
                });
            }
            if (queryDocumentSnapshots.isEmpty()) listener.onSuccess(myGroups);
        });
    }

    /**
     * Ajouter un commentaire à un post
     */
    public void addComment(String postId, String text, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null || postId == null || text.isEmpty()) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
            String userName = userDoc.getString("name");
            
            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", currentUserId);
            comment.put("userName", userName != null ? userName : "Anonyme");
            comment.put("text", text);
            comment.put("timestamp", System.currentTimeMillis());

            db.collection("posts").document(postId).collection("comments").add(comment)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(null));
        });
    }

    /**
     * Récupérer les commentaires d'un post triés par date
     */
    public Query getCommentsQuery(String postId) {
        return db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    /**
     * Incrémente ou décrémente les likes d'un post de manière atomique
     */
    public void toggleLike(String postId, boolean isAdding, OnSuccessListener<Void> listener) {
        String currentUserId = auth.getUid();
        if (currentUserId == null || postId == null) return;

        final com.google.firebase.firestore.DocumentReference postRef = postsRef.document(postId);
        final com.google.firebase.firestore.DocumentReference likeRef = db.collection("likes").document(currentUserId + "_" + postId);

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot postSnap = transaction.get(postRef);
            long currentLikes = 0;
            if (postSnap.exists()) {
                String likesStr = postSnap.getString("likes");
                try {
                    currentLikes = Long.parseLong(likesStr != null ? likesStr : "0");
                } catch (NumberFormatException e) {
                    currentLikes = 0;
                }
            }

            if (isAdding) {
                transaction.set(likeRef, new HashMap<String, Object>() {{
                    put("userId", currentUserId);
                    put("postId", postId);
                    put("timestamp", System.currentTimeMillis());
                }});
                transaction.update(postRef, "likes", String.valueOf(currentLikes + 1));
            } else {
                transaction.delete(likeRef);
                transaction.update(postRef, "likes", String.valueOf(Math.max(0, currentLikes - 1)));
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            if (listener != null) listener.onSuccess(null);
        });
    }
}
