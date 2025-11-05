package fr.upjv.lesombresduson;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
}
