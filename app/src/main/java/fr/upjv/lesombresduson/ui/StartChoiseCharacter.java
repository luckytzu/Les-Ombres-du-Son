package fr.upjv.lesombresduson.ui;

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

import java.util.Map;

import fr.upjv.lesombresduson.R;
import fr.upjv.lesombresduson.data.remote.FirebaseHelper;
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaGameActivity;
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaGameActivityAfterIntro;
import fr.upjv.lesombresduson.ui.game.cecilia.CeciliaLevel2Activity;
import fr.upjv.lesombresduson.ui.game.lum.LumGameActivity;

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
        btnContinue.setEnabled(false); // Désactivé par défaut

        // On utilise la nouvelle méthode pour lire toutes les données
        FirebaseHelper.getInstance().getGameData(currentUserId, characterName, new FirebaseHelper.GameDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> gameData) {
                if (gameData != null && "started".equals(gameData.get("state"))) {
                    // Partie EXISTANTE et en cours ("started")
                    btnContinue.setText("Continuer");
                    btnContinue.setEnabled(true);
                    btnContinue.setVisibility(View.VISIBLE);
                } else {
                    // Partie NON-EXISTANTE ou marquée "finished"
                    btnContinue.setEnabled(false);
                    btnContinue.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StartChoiseCharacter.this, "Erreur de connexion Firebase", Toast.LENGTH_SHORT).show();
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
     * Lance l'activité de jeu selon le personnage choisi, en tenant compte de la progression.
     *
     * @param character Le personnage sélectionné
     */
    private void launchGameActivity(Character character) {
        if (character == null || currentUserId == null) return;

        // 1. Lire la progression actuelle de l'utilisateur pour ce personnage
        FirebaseHelper.getInstance().getGameData(currentUserId, character.name, new FirebaseHelper.GameDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> gameData) {
                // Valeurs par défaut
                boolean introFinished = false;
                int currentLevel = 1;

                if (gameData != null) {
                    // Récupération sécurisée de introFinished
                    Object introStatus = gameData.get("introFinished");
                    if (introStatus instanceof Boolean) {
                        introFinished = (Boolean) introStatus;
                    }

                    // Récupération sécurisée du currentLevel (Firestore stocke souvent en Long)
                    Object levelStatus = gameData.get("currentLevel");
                    if (levelStatus instanceof Number) {
                        currentLevel = ((Number) levelStatus).intValue();
                    }
                }

                Intent intent = null;

                // LOGIQUE DE REDIRECTION POUR CECILIA
                if (character.id == CECILIA.id) {
                    if (currentLevel >= 2) {
                        // CAS 1 : Le joueur est au niveau 2 (ou plus)
                        Toast.makeText(StartChoiseCharacter.this, "Chargement du Niveau 2...", Toast.LENGTH_SHORT).show();
                        intent = new Intent(StartChoiseCharacter.this, CeciliaLevel2Activity.class);
                    }
                    else if (introFinished) {
                        // CAS 2 : Niveau 1, mais l'intro est déjà faite (Phase tactile/micro)
                        Toast.makeText(StartChoiseCharacter.this, "Reprise du Niveau 1...", Toast.LENGTH_SHORT).show();
                        intent = new Intent(StartChoiseCharacter.this, CeciliaGameActivityAfterIntro.class);
                    }
                    else {
                        // CAS 3 : Début absolu (Intro avec accéléromètre)
                        Toast.makeText(StartChoiseCharacter.this, "Nouvelle partie : Introduction...", Toast.LENGTH_SHORT).show();
                        intent = new Intent(StartChoiseCharacter.this, CeciliaGameActivity.class);
                    }
                }
                // LOGIQUE POUR LUM (À adapter plus tard)
                else {
                    intent = new Intent(StartChoiseCharacter.this, LumGameActivity.class);
                }

                // Lancement de l'activité
                if (intent != null) {
                    intent.putExtra("CHARACTER_NAME", character.name);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StartChoiseCharacter.this, "Erreur de chargement de la progression.", Toast.LENGTH_LONG).show();
            }
        });
    }
}