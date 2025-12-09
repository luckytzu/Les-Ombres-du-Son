package fr.upjv.lesombresduson.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import fr.upjv.lesombresduson.data.remote.FirebaseHelper;
import fr.upjv.lesombresduson.R;

public class MainActivity extends AppCompatActivity {

    static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private SignInButton signInButton;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseHelper = FirebaseHelper.getInstance();

        // Si l'utilisateur est déjà connecté, on le redirige directement
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, Home.class));
            finish();
            return;
        }

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.Web_client_id))
            .requestEmail()
            .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        signInButton = findViewById(R.id.btnGoogleSignIn);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasInternetPermission()) {
                    if (isInternetAvailable()) {
                        initiateSignIn();
                    } else {
                        Toast.makeText(MainActivity.this, "Connexion Internet non disponible.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Permission Internet non accordée.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @summary Vérifie si l'application dispose de la permission INTERNET.
     * @return true si la permission est accordée, sinon false.
     */
    boolean hasInternetPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @summary Gère le résultat des demandes de permissions.
     * Redemande les permissions ou redirige vers les paramètres si refus définitif.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions de localisation accordées", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @summary Redirige l'utilisateur vers les paramètres de l'application pour gérer les permissions.
     */
    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    /**
     * @summary Vérifie si une connexion Internet est active.
     * @return true si connecté, sinon false.
     */
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * @summary Lance l'intent pour initier la connexion Google.
     */
    private void initiateSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * @summary Récupère le résultat de l'intent de connexion Google.
     * @param requestCode Code de la requête.
     * @param resultCode Résultat de l'activité.
     * @param data Données retournées.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                authenticateWithFirebase(account);
            } catch (Exception e) {
                Toast.makeText(this, "Échec de la connexion : " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }


    public void setFirebaseHelper(FirebaseHelper helper) {
        this.firebaseHelper = helper;
    }

    /**
     * @summary Authentifie l'utilisateur auprès de Firebase à l'aide du compte Google.
     * Si la connexion est réussie, les infos utilisateur sont sauvegardées et redirection vers Home.
     * @param account Compte Google connecté.
     */
    private void authenticateWithFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {
                            firebaseHelper.creerOuMettreAJourUtilisateur(user, account);

                            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("user_uid", user.getUid());
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Connecté : " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(MainActivity.this, Home.class));
                            finish();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Échec de l'authentification", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}