package fr.upjv.lesombresduson;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

/**
 * Gère la logique de la phase de navigation tactile (recherche de la cible).
 * Doit être attaché à une View ou à l'Activity pour intercepter les événements tactiles.
 */
public class TouchNavigationManager implements View.OnTouchListener {

    private final GestureListener listener;
    private final Context context;
    private final Vibrator vibrator;
    private MediaPlayer navigationSoundPlayer;

    // Logique de Jeu
    private final int MAX_DISTANCE = 1000; // Distance maximale de référence pour le volume
    private final int TARGET_RADIUS = 150;  // Rayon en pixels pour valider la sortie
    private int targetX;
    private int targetY;

    /**
     * Constructeur pour initialiser le manager de navigation tactile.
     * Configure le Vibrator, la cible de recherche et démarre le son de guidage.
     *
     * @param context Le contexte de l'application (généralement l'Activity).
     * @param listener L'écouteur pour communiquer les événements de jeu (e.g., cible trouvée).
     * @param screenWidth La largeur de l'écran en pixels pour définir la position X de la cible.
     * @param screenHeight La hauteur de l'écran en pixels pour définir la position Y de la cible.
     */
    public TouchNavigationManager(Context context, GestureListener listener, int screenWidth, int screenHeight) {
        this.context = context;
        this.listener = listener;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        int marginX = (int) (screenWidth * 0.1);
        int marginY = (int) (screenHeight * 0.1);

        // X cible aléatoire entre 10% de la largeur et 90% de la largeur
        this.targetX = marginX + (int) (Math.random() * (screenWidth - 2 * marginX));
        // Y cible aléatoire entre 10% de la hauteur et 90% de la hauteur
        this.targetY = marginY + (int) (Math.random() * (screenHeight - 2 * marginY));

        initializeNavigationSound();
    }

    /**
     * Initialise le MediaPlayer pour le son de navigation (son constant).
     * Définit la boucle (looping) et démarre la lecture avec un volume minimal (0.1f).
     */
    private void initializeNavigationSound() {
        if (navigationSoundPlayer == null) {
            // Un son constant ou pulsé, qui peut être manipulé par le volume
            navigationSoundPlayer = MediaPlayer.create(context, R.raw.ambiance_murmur);
            navigationSoundPlayer.setLooping(true);
            navigationSoundPlayer.setVolume(0.1f, 0.1f); // Démarrer doucement
            navigationSoundPlayer.start();
        }
    }

    /**
     * Nettoie les ressources. À appeler dans onDestroy().
     */
    public void cleanup() {
        if (navigationSoundPlayer != null) {
            navigationSoundPlayer.stop();
            navigationSoundPlayer.release();
            navigationSoundPlayer = null;
        }
    }

    /**
     * Méthode de rappel (callback) appelée lors d'un événement tactile sur la vue attachée.
     * Gère le mouvement du doigt (MOVE) pour ajuster le feedback, et la fin du toucher (UP)
     * pour vérifier si la cible a été trouvée.
     *
     * @param v La vue sur laquelle l'événement s'est produit.
     * @param event Les données de l'événement tactile (position, action, etc.).
     * @return true pour indiquer que l'événement a été consommé.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();

        // Calcul de la distance
        double distance = Math.sqrt(
                Math.pow(currentX - targetX, 2) +
                        Math.pow(currentY - targetY, 2)
        );

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                handleTouchFeedback(distance);
                break;

            case MotionEvent.ACTION_UP:
                // Arrêt du son si le doigt est levé
                stopTouchFeedback();

                // Validation de la cible
                if (distance < TARGET_RADIUS) {
                    listener.onTargetFound();
                } else {
                    listener.onFeedbackNeeded("Vous vous éloignez, réessayez.");
                }
                break;
        }
        return true;
    }

    /**
     * Ajuste le volume du son de navigation et active une vibration en fonction de la distance.
     * Le volume augmente avec la proximité (gain), et la vibration devient plus longue/fréquente.
     *
     * @param distance La distance calculée en pixels entre le doigt et la cible.
     */
    private void handleTouchFeedback(double distance) {
        if (navigationSoundPlayer == null) return;

        // 1. Calcul du Gain
        float normalizedDistance = (float) Math.min(distance, MAX_DISTANCE) / MAX_DISTANCE;
        float gain = 1.0f - normalizedDistance;

        float minGain = 0.1f; // Volume minimum pour ne pas perdre complètement le son
        float finalVolume = minGain + (1.0f - minGain) * gain;

        // 2. Appliquer le volume
        navigationSoundPlayer.setVolume(finalVolume, finalVolume);

        // 3. Feedback Haptique (Vibrer plus fort/longtemps quand on est proche)
        if (distance < 200) { // Zone de vibration plus large pour commencer à sentir
            long delay = (long) (50 + (distance / 200.0) * 150);

            if (distance < TARGET_RADIUS * 2) {
                vibrator.vibrate(100);
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    /**
     * Réduit le volume du son de navigation au niveau minimum (0.1f) lorsque le doigt est levé (ACTION_UP).
     * Le son ne s'arrête pas complètement, permettant au joueur de reprendre la recherche rapidement.
     */
    private void stopTouchFeedback() {
        if (navigationSoundPlayer != null) {
            // Le son ne s'arrête pas complètement, mais retourne au volume minimum
            navigationSoundPlayer.setVolume(0.1f, 0.1f);
        }
    }
}