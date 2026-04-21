package com.nandish.productivity

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

object GoogleDriveSync {

    private const val STATE_FILE = "prodash_cloud_state.json"

    fun googleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context.applicationContext, gso)
    }

    fun lastSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context.applicationContext)

    private fun driveForAccount(context: Context, account: GoogleSignInAccount): Drive? {
        val androidAccount = account.account ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context.applicationContext,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).setSelectedAccount(androidAccount)
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    private fun findStateFileId(drive: Drive): String? {
        val list = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$STATE_FILE' and trashed = false")
            .setFields("files(id, name)")
            .execute()
        return list.files?.firstOrNull()?.id
    }

    /** Uploads or replaces JSON in the Drive app data folder. */
    fun uploadStateJson(context: Context, account: GoogleSignInAccount, json: String) {
        val drive = driveForAccount(context, account) ?: return
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        val media = ByteArrayContent("application/json", bytes)
        val existingId = findStateFileId(drive)
        if (existingId != null) {
            drive.files().update(existingId, DriveFile(), media).execute()
        } else {
            val meta = DriveFile().apply {
                name = STATE_FILE
                parents = listOf("appDataFolder")
            }
            drive.files().create(meta, media).setFields("id").execute()
        }
    }

    /** Returns JSON from Drive, or null if no file yet. */
    fun downloadStateJson(context: Context, account: GoogleSignInAccount): String? {
        val drive = driveForAccount(context, account) ?: return null
        val id = findStateFileId(drive) ?: return null
        val out = ByteArrayOutputStream()
        drive.files().get(id).executeMediaAndDownloadTo(out)
        return out.toString(StandardCharsets.UTF_8.name())
    }
}
