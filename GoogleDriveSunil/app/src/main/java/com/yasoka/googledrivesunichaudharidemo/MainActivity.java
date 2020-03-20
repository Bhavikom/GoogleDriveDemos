package com.yasoka.googledrivesunichaudharidemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.gson.Gson;

import java.io.File;

import static com.yasoka.googledrivesunichaudharidemo.Driver_utils.mfile;
import static com.yasoka.googledrivesunichaudharidemo.Driver_utils.preferences_driverId;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient google_api_client;
    private static final int DIALOG_ERROR_CODE = 102;
    Button btnBackup,btnRestore;
    private static final int REQ_CODE_OPEN = 101;
    GoogleSignInClient googleSignInClient;
    public static final int REQUEST_CODE_SIGN_IN2 = 5;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("Backup_pref", MODE_PRIVATE);
        editor = preferences.edit();

        google_api_client = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        btnBackup = findViewById(R.id.btn_backup);
        btnRestore = findViewById(R.id.btn_restore);

        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File directorys = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Databackup");
                if (directorys.exists()) {
                    String json = preferences_driverId.getString("drive_id", "");
                    DriveId driveId = new Gson().fromJson(json, DriveId.class);
                    //Update file already stored in Drive
                    Driver_utils.trash(driveId, google_api_client);
                    // Create the Drive API instance
                    Driver_utils.creatBackupDrive(MainActivity.this, google_api_client);
                    //dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "backup done", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), " create directory first ", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRestore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
// Launch user interface and allow user to select file
                IntentSender intentSender = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setMimeType(new String[]{"application/zip"})
                        .build(google_api_client);
                try {

                    startIntentSenderForResult(

                            intentSender, REQ_CODE_OPEN, null, 0, 0, 0);

                } catch (IntentSender.SendIntentException e) {

                    Log.w("TAG", e.getMessage());
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIALOG_ERROR_CODE) {
            boolean mResolvingError = false;
            if (resultCode == RESULT_OK) { // Error was resolved, now connect to the client if not done so.
                if (!google_api_client.isConnecting() && !google_api_client.isConnected()) {
                    google_api_client.connect();
                }
            }

        }
        if (requestCode == REQ_CODE_OPEN && resultCode == RESULT_OK) {
            DriveId mSelectedFileDriveId = data.getParcelableExtra(
                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
            Log.e("DriveID ---", mSelectedFileDriveId + "");
            Gson gson = new Gson();
            String json = gson.toJson(mSelectedFileDriveId); // myObject - instance of MyObject
            //editor = preferences_driverId.edit();
            editor.putString("drive_id", json).commit();
            Log.e("TAG", "driveId this 1-- " + mSelectedFileDriveId);
            //if (Utils.isInternetWorking()) {
                //restore Drive file to SDCArd
                Driver_utils.restoreDriveBackup(MainActivity.this, google_api_client, "GOOGLE_DRIVE_FILE_NAME", preferences_driverId, mfile);
                Driver_utils.restore(MainActivity.this);

            /*} else {
                Toast.makeText(getApplicationContext(), R.string.nointernets, Toast.LENGTH_LONG).show();
            }*/
        }
    }
    /*public void singIntoGoogle(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (account == null) {
            signIn2();
        }else {
            Toast.makeText(MainActivity.this, "You have already singed in another account", Toast.LENGTH_SHORT).show();
        }
    }
    private void signIn2() {
        Log.i("TAG", "Start sign in");
        googleSignInClient = buildGoogleSignInClient();
        startActivityForResult(googleSignInClient.getSignInIntent(), MainActivity.REQUEST_CODE_SIGN_IN2);
    }
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(MainActivity.this, signInOptions);
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
