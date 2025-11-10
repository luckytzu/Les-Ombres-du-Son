package fr.upjv.lesombresduson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.button.MaterialButton;

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

    // Définition des personnages
    private final Character CECILIA = new Character(1, "Cécilia (cécité totale)", R.drawable.cecilia);
    private final Character LUM = new Character(2, "Lum (cécité partielle)", R.drawable.lum);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_character);

        // 1. Initialisation des vues principales
        selectionGroup = findViewById(R.id.character_selection_group);
        confirmedGroup = findViewById(R.id.character_confirmed_group);
        btnBack = findViewById(R.id.button_back);

        // 2. Initialisation des conteneurs de sélection
        LinearLayout layoutCecilia = findViewById(R.id.layout_cecilia);
        LinearLayout layoutLum = findViewById(R.id.layout_lum);

        // 3. Initialisation des boutons de l'écran confirmé
        MaterialButton btnContinue = findViewById(R.id.button_continue);
        MaterialButton btnNewGame = findViewById(R.id.button_new_game);

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
                Toast.makeText(StartChoiseCharacter.this, "Lancement du jeu. (Continuer)", Toast.LENGTH_SHORT).show();
            }
        });

        // Logique pour commencer une nouvelle partie
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StartChoiseCharacter.this, "Lancement d'une nouvelle partie.", Toast.LENGTH_SHORT).show();
            }
        });
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

        // 3. Mettre à jour le bouton Retour (pour revenir en arrière depuis la confirmation)
        Toast.makeText(this, "Personnage sélectionné : " + selectedCharacter.name, Toast.LENGTH_SHORT).show();
    }
}