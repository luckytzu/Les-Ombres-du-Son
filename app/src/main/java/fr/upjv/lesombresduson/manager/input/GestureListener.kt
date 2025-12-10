package fr.upjv.lesombresduson.manager.input

/**
 * Interface de communication entre la logique du jeu (SensorGameManager)
 */
interface GestureListener {

    /**
     * Appelé lorsque l'introduction est terminée et que le joueur doit commencer le premier geste.
     */
    fun onInstructionReady(instruction: String)

    /**
     * Appelé lorsqu'un geste a été maintenu 3 secondes et est validé.
     * @param isGameComplete Vrai si tous les 4 gestes sont terminés.
     * @param nextInstruction L'instruction pour le geste suivant, ou une chaîne vide si le jeu est terminé.
     */
    fun onGestureValidated(isGameComplete: Boolean, nextInstruction: String) // Rétabli les deux paramètres

    /**
     * Appelée lorsqu'une action est lancée (début du maintien, annulation du maintien).
     * @param message Le message Toast à afficher.
     */
    fun onFeedbackNeeded(message: String)

    /**
     * Appelé lorsque le joueur trouve la cible dans la phase de navigation tactile.
     */
    fun onTargetFound()

    /**
     * Appelé lorsque le joueur trouve la cible dans la phase microphone.
     */
    fun onDogFound()
}