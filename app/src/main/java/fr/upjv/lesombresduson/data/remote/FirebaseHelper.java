package fr.upjv.lesombresduson.data.remote;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
        private static final String TAG = "FirebaseHelper";
        private static FirebaseHelper instance;
        private final FirebaseFirestore db;
        private final CollectionReference usersRef;

        private FirebaseHelper() {
            db = FirebaseFirestore.getInstance();
            usersRef = db.collection("Users");
        }

        /**
         * Obtient l'instance singleton de FirebaseHelper.
         *
         * @return instance unique de FirebaseHelper
         */
        public static synchronized FirebaseHelper getInstance() {
            if (instance == null) {
                instance = new FirebaseHelper();
            }
            return instance;
        }

        /**
         * Crée ou met à jour un document utilisateur dans la collection "Users" de Firestore.
         *
         * ➡ Fonctionnement :
         * - Si c'est la première connexion de l'utilisateur, un nouveau document sera créé avec son UID comme identifiant.
         * - Si l'utilisateur existe déjà, ses informations sont mises à jour.
         *
         * Champs enregistrés dans Firestore :
         *  - email          → Adresse email de l'utilisateur (FirebaseUser)
         *  - displayName    → Nom complet tel que fourni par FirebaseUser
         *  - creationDate   → Timestamp de création du compte Firebase
         *  - lastLogin      → Timestamp côté serveur (FieldValue.serverTimestamp), mis à jour à chaque appel
         *
         * @param firebaseUser  Utilisateur connecté via Firebase Authentication
         * @param account       Compte Google lié (permet de récupérer prénom et nom de famille)
         */
        public void creerOuMettreAJourUtilisateur(FirebaseUser firebaseUser, GoogleSignInAccount account) {
            if (firebaseUser == null) return;

            DocumentReference userDoc = usersRef.document(firebaseUser.getUid());

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", firebaseUser.getEmail());
            userData.put("displayName", firebaseUser.getDisplayName());

            // Toujours mettre à jour la dernière connexion serveur
            userData.put("lastLogin", com.google.firebase.firestore.FieldValue.serverTimestamp());

            userDoc.set(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Utilisateur créé/mis à jour : " + firebaseUser.getEmail()))
                    .addOnFailureListener(e -> Log.e(TAG, "Erreur Firestore", e));
        }


    /**
     * Enregistre une nouvelle partie pour l'utilisateur et le personnage donné.
     * Cette méthode démarre une nouvelle "session" de jeu.
     *
     * @param userId L'UID de l'utilisateur connecté.
     * @param characterName Le nom du personnage choisi (Cécilia ou Lum).
     */
    public void saveNewGame(String userId, String characterName) {
        if (userId == null || characterName == null) return;

        DocumentReference newGameDoc = usersRef
                .document(userId)
                .collection("Games")
                .document(characterName);

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("character", characterName);
        gameData.put("state", "started"); // État initial de la partie
        gameData.put("currentLevel", 1); // Niveau de départ
        gameData.put("startDate", FieldValue.serverTimestamp());

        newGameDoc.set(gameData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nouvelle partie enregistrée pour : " + characterName))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la sauvegarde de la partie", e));
    }

    /**
     * Marque une partie existante comme "terminée" avant d'en créer une nouvelle.
     *
     * @param userId L'UID de l'utilisateur.
     * @param characterName Nom du personnage dont la partie doit être terminée.
     */
    public void finishGame(String userId, String characterName) {
        if (userId == null || characterName == null) return;

        CollectionReference gamesRef = usersRef
                .document(userId)
                .collection("Games");

        // Cherche la partie existante correspondant à ce personnage
        gamesRef.whereEqualTo("characterName", characterName)
                .whereEqualTo("state", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            DocumentReference gameDoc = doc.getReference();

                            Map<String, Object> update = new HashMap<>();
                            update.put("state", "finished");
                            update.put("endDate", FieldValue.serverTimestamp());

                            gameDoc.update(update)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Partie marquée comme terminée : " + doc.getId()))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Erreur lors de la mise à jour de la partie", e));
                        }
                    } else {
                        Log.d(TAG, "Aucune partie active trouvée pour " + characterName);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur lors de la recherche de la partie", e));
    }

    /**
     * Vérifie s'il existe une partie en cours pour un personnage spécifique.
     *
     * @param userId L'UID de l'utilisateur connecté.
     * @param characterName Le nom du personnage à vérifier.
     * @param callback L'interface pour renvoyer le résultat (true si une partie existe).
     */
    public void checkGameExists(String userId, String characterName, GameCheckCallback callback) {
        CollectionReference gamesRef = usersRef.document(userId).collection("Games");

        // Recherche une partie pour ce personnage dont l'état n'est pas "finished" (terminé)
        gamesRef.whereEqualTo("character", characterName)
                .whereEqualTo("state", "started") // On suppose qu'une partie "started" est en cours
                .limit(1) // On n'a besoin que d'un résultat
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        // Si le nombre de documents trouvés est supérieur à zéro, la partie existe.
                        boolean exists = result != null && !result.isEmpty();
                        callback.onResult(exists);
                    } else {
                        Log.e(TAG, "Erreur lors de la vérification de l'existence de la partie", task.getException());
                        callback.onResult(false); // Par sécurité, on renvoie false en cas d'erreur
                    }
                });
    }

    /**
     * Met à jour le statut du jeu en cours pour l'utilisateur et le personnage donné.
     *
     * @param userId L'UID de l'utilisateur.
     * @param characterName Le nom du personnage (Cécilia ou Lum).
     * @param fieldName Le nom du champ à mettre à jour (ex: "introFinished").
     * @param value La nouvelle valeur du champ (ex: true).
     */
    public void updateGameProgress(String userId, String characterName, String fieldName, Object value) {
        if (userId == null || characterName == null) return;

        DocumentReference gameDoc = usersRef
                .document(userId)
                .collection("Games")
                .document(characterName);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(fieldName, value);
        updateData.put("lastUpdate", FieldValue.serverTimestamp());

        gameDoc.update(updateData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progression de partie mise à jour : " + fieldName + "=" + value))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la mise à jour de la progression", e));
    }

    /**
     * Interface de rappel (Callback) pour la vérification asynchrone de l'existence d'une partie.
     */
    public interface GameCheckCallback {
        void onResult(boolean gameExists);
    }

    // Ajouter cette nouvelle interface pour le callback de lecture de données
    public interface GameDataCallback {
        void onDataLoaded(Map<String, Object> gameData);
        void onFailure(Exception e);
    }

    /**
     * Lit les données de progression d'une partie existante.
     *
     * @param userId L'UID de l'utilisateur.
     * @param characterName Le nom du personnage.
     * @param callback L'interface pour renvoyer les données (Map) ou l'échec.
     */
    public void getGameData(String userId, String characterName, GameDataCallback callback) {
        if (userId == null || characterName == null) {
            callback.onFailure(new Exception("UserID or CharacterName is null"));
            return;
        }

        DocumentReference gameDoc = usersRef
                .document(userId)
                .collection("Games")
                .document(characterName);

        gameDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onDataLoaded(documentSnapshot.getData());
                    } else {
                        // Le document de partie n'existe pas
                        callback.onDataLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la lecture des données de partie", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Met à jour le niveau actuel du joueur pour un personnage donné.
     *
     * @param userId L'UID de l'utilisateur.
     * @param characterName Le nom du personnage.
     * @param newLevel Le nouveau numéro de niveau.
     */
    public void saveLevelProgression(String userId, String characterName, int newLevel) {
        if (userId == null || characterName == null) return;

        DocumentReference gameDoc = usersRef
                .document(userId)
                .collection("Games")
                .document(characterName);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("currentLevel", newLevel);
        updateData.put("lastUpdate", FieldValue.serverTimestamp());

        gameDoc.update(updateData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Niveau mis à jour : " + newLevel + " pour " + characterName))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la mise à jour du niveau", e));
    }
}
