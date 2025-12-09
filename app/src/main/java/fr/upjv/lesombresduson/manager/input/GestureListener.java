package fr.upjv.lesombresduson.manager.input;

/**
 * Interface de communication entre la logique du jeu (SensorGameManager)
 */
public interface GestureListener {
    /**
     * Appelé lorsque l'introduction est terminée et que le joueur doit commencer le premier geste.
     */
    void onInstructionReady(String instruction);

    /**
     * Appelé lorsqu'un geste a été maintenu 3 secondes et est validé.
     * @param isGameComplete Vrai si tous les 4 gestes sont terminés.
     * @param nextInstruction L'instruction pour le geste suivant, ou une chaîne vide si le jeu est terminé.
     */
    void onGestureValidated(boolean isGameComplete, String nextInstruction);

    /**
     * Appelée lorsqu'une action est lancée (début du maintien, annulation du maintien).
     * @param message Le message Toast à afficher.
     */
    void onFeedbackNeeded(String message);

    /**
     * Appelé lorsque le joueur trouve la cible dans la phase de navigation tactile.
     */
    void onTargetFound();

    /**
     * Appelé lorsque le joueur trouve la cible dans la phase microphone.
     */
    void onDogFound();
}