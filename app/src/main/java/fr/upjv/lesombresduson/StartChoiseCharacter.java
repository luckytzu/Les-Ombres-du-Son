package fr.upjv.lesombresduson;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartChoiseCharacter extends AppCompatActivity {

    // Data class pour stocker les informations du personnage.
    private static class Character {
        final int id;
        final String name;
        final int imageResId;

        Character(int id, String name, int imageResId) {
            this.id = id;
            this.name = name;
            this.imageResId = imageResId;
        }
    }

    // Déclaration des vues pour l'accès
    private ConstraintLayout selectionGroup;
    private ConstraintLayout confirmedGroup;
    private MaterialButton btnBack;
    private MaterialButton btnContinue;
    private String currentUserId;

    // Définition des personnages
    private final Character CECILIA = new Character(1, "Cécilia (cécité totale)", R.drawable.cecilia);
    private final Character LUM = new Character(2, "Lum (cécité partielle)", R.drawable.lum);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_character);

        // Récupération de l'utilisateur Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Gérer le cas où l'utilisateur n'est pas connecté (redirection vers l'écran de connexion)
            Toast.makeText(this, "Erreur: Utilisateur non connecté.", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. Initialisation des vues principales
        selectionGroup = findViewById(R.id.character_selection_group);
        confirmedGroup = findViewById(R.id.character_confirmed_group);
        btnBack = findViewById(R.id.button_back);

        // 2. Initialisation des conteneurs de sélection
        LinearLayout layoutCecilia = findViewById(R.id.layout_cecilia);
        LinearLayout layoutLum = findViewById(R.id.layout_lum);

        // 3. Initialisation des boutons de l'écran confirmé
        btnContinue = findViewById(R.id.button_continue);
        MaterialButton btnNewGame = findViewById(R.id.button_new_game);

        // Bouton caché et désactivé par défaut
        btnContinue.setEnabled(false);
        btnContinue.setVisibility(View.GONE);

        // ===================================
        // Logique de sélection de personnage
        // ===================================

        layoutCecilia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCharacterSelection(CECILIA);
            }
        });

        layoutLum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCharacterSelection(LUM);
            }
        });

        // ===================================
        // Logique des boutons de navigation
        // ===================================

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Revenir à l'état de sélection si nous sommes sur l'écran confirmé
                if (confirmedGroup.getVisibility() == View.VISIBLE) {
                    confirmedGroup.setVisibility(View.GONE);
                    selectionGroup.setVisibility(View.VISIBLE);
                    Toast.makeText(StartChoiseCharacter.this, "Annulation de la sélection.", Toast.LENGTH_SHORT).show();
                } else {
                    // Redirection vers la page de home
                    Intent intent = new Intent(StartChoiseCharacter.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Logique pour continuer la partie
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Character selectedCharacter = getLastSelectedCharacter();
                launchGameActivity(selectedCharacter);
            }
        });

        // Logique pour commencer une nouvelle partie
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Character selectedCharacter = getLastSelectedCharacter();
                if (selectedCharacter == null) return;

                // Vérifie d'abord si une partie existe déjà
                FirebaseHelper.getInstance().checkGameExists(currentUserId, selectedCharacter.name, new FirebaseHelper.GameCheckCallback() {
                    @Override
                    public void onResult(boolean gameExists) {
                        if (gameExists) {
                            // Si une partie existe déjà, on demande confirmation à l’utilisateur
                            showConfirmNewGameDialog(selectedCharacter);
                        } else {
                            // Sinon, on crée directement une nouvelle partie
                            startNewGame(selectedCharacter);
                        }
                    }
                });
            }
        });
    }

    // Méthode pour obtenir le personnage actuellement affiché/sélectionné
    private Character getLastSelectedCharacter() {
        TextView centerName = findViewById(R.id.text_center_name);
        String name = centerName.getText().toString();

        if (name.equals(CECILIA.name)) {
            return CECILIA;
        } else if (name.equals(LUM.name)) {
            return LUM;
        }
        return null;
    }


    /**
     * Gère la mise à jour du layout après la sélection d'un personnage.
     * @param selectedCharacter Le personnage sélectionné (Cécilia ou Lum).
     */
    private void handleCharacterSelection(Character selectedCharacter) {
        // 1. Mettre à jour les éléments du groupe confirmé
        ImageView centerImage = findViewById(R.id.image_center_character);
        TextView centerName = findViewById(R.id.text_center_name);

        centerImage.setImageResource(selectedCharacter.imageResId);
        centerName.setText(selectedCharacter.name);

        // 2. Basculer la visibilité des groupes
        selectionGroup.setVisibility(View.GONE);
        confirmedGroup.setVisibility(View.VISIBLE);

        // 3. VÉRIFIER L'ÉTAT DE LA PARTIE pour ce personnage et mettre à jour le bouton "Continuer"
        checkIfGameExists(selectedCharacter.name);
    }

    /**
     * Vérifie auprès de Firebase si l'utilisateur a déjà une partie en cours pour ce personnage.
     * Met à jour l'état du bouton "Continuer".
     */
    private void checkIfGameExists(String characterName) {
        if (currentUserId == null) return;

        btnContinue.setVisibility(View.GONE);

        FirebaseHelper.getInstance().checkGameExists(currentUserId, characterName, new FirebaseHelper.GameCheckCallback() {
            @Override
            public void onResult(boolean gameExists) {
                if (gameExists) {
                    // Partie EXISTANTE : Activer le bouton Continuer
                    btnContinue.setText("Continuer");
                    btnContinue.setEnabled(true);
                    btnContinue.setVisibility(View.VISIBLE);
                } else {
                    // Partie NON-EXISTANTE : Désactiver le bouton Continuer
                    btnContinue.setEnabled(false);
                    btnContinue.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Affiche une boîte de dialogue pour confirmer l'écrasement de la partie existante.
     */
    private void showConfirmNewGameDialog(Character character) {
        new AlertDialog.Builder(this)
                .setTitle("Partie Existante")
                .setMessage("Vous avez déjà une partie en cours avec " + character.name + ". Voulez-vous la recommencer et perdre la progression non sauvegardée ?")
                .setPositiveButton("Nouvelle Partie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // L'utilisateur confirme: écraser l'ancienne (currentGameId) et démarrer la nouvelle.
                        startNewGame(character);
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // L'utilisateur annule: ne rien faire.
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Enregistre une nouvelle partie dans Firestore et lance l'activité de jeu.
     *
     * @param character Le personnage avec lequel commencer la partie.
     */
    private void startNewGame(Character character) {
        if (currentUserId == null || character == null) return;

        // Si une ancienne partie existe, on la marque comme "finished"
        FirebaseHelper.getInstance().finishGame(currentUserId, character.name);

        // Réinitialiser l'état local après l'archivage
        btnContinue.setVisibility(View.GONE);
        btnContinue.setEnabled(false);

        // Enregistre une nouvelle entrée de partie dans Firestore
        FirebaseHelper.getInstance().saveNewGame(currentUserId, character.name);

        // Lancer l'activité de jeu
        launchGameActivity(character);
    }

    /**
     * Lance l'activité de jeu selon le personnage choisi.
     *
     * @param character Le personnage sélectionné
     */
    private void launchGameActivity(Character character) {
        if (character == null) return;

        Intent intent;
        switch (character.id) {
            case 1: // Cécilia
                intent = new Intent(this, CeciliaGameActivity.class);
                break;
            case 2: // Lum
                intent = new Intent(this, LumGameActivity.class);
                break;
            default:
                Toast.makeText(this, "Personnage inconnu", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra("CHARACTER_NAME", character.name);
        startActivity(intent);
        finish();
    }
}