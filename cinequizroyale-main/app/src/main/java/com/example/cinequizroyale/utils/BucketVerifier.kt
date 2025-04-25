package com.example.cinequizroyale.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.Bucket
import java.util.Collections

/**
 * Utility class to verify and create a Google Cloud Storage bucket.
 * This helps diagnose and fix issues with bucket access.
 */
object BucketVerifier {
    private const val TAG = "BucketVerifier"

    /**
     * Verifies if the bucket exists and is accessible.
     * This is useful for debugging cloud storage issues.
     *
     * @param context The application context
     * @param account The signed-in Google account
     * @param bucketName The bucket name to verify
     * @return A diagnostic message about the bucket status
     */
    fun verifyBucket(context: Context, account: GoogleSignInAccount, bucketName: String): String {
        try {
            Log.d(TAG, "Verifying bucket: $bucketName")

            // Set up credentials for Cloud Storage
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(StorageScopes.DEVSTORAGE_READ_WRITE)
            )
            credential.selectedAccount = account.account

            // Set up Cloud Storage client
            val storage = Storage.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("CineQuizRoyale")
                .build()

            // Check if bucket exists
            try {
                val bucket = storage.buckets().get(bucketName).execute()
                Log.d(TAG, "Bucket found: ${bucket.name}, created at: ${bucket.timeCreated}")
                return "✅ Bucket found: ${bucket.name}\nCreated: ${bucket.timeCreated}"
            } catch (e: Exception) {
                Log.e(TAG, "Bucket not found or access denied: ${e.message}", e)

                // Check if we can list buckets
                try {
                    val buckets = storage.buckets().list(account.id).execute()
                    val foundBuckets = buckets.items ?: emptyList()

                    if (foundBuckets.isNotEmpty()) {
                        val bucketNames = foundBuckets.joinToString(", ") { it.name }
                        Log.d(TAG, "Available buckets: $bucketNames")
                        return "❌ Bucket '$bucketName' not found\nAvailable buckets: $bucketNames"
                    } else {
                        Log.d(TAG, "No buckets found for this account")
                        return "❌ No buckets found for this account"
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Cannot list buckets: ${e2.message}", e2)

                    if (e.message?.contains("403") == true) {
                        return "❌ Permission denied (403)\nYou don't have access to this bucket"
                    } else if (e.message?.contains("404") == true) {
                        return "❌ Bucket not found (404)\nThe bucket '$bucketName' doesn't exist"
                    }

                    return "❌ Error: ${e.message}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying bucket: ${e.message}", e)
            return "❌ Error: ${e.message}"
        }
    }

    /**
     * Attempts to create the bucket if it doesn't exist.
     *
     * @param context The application context
     * @param account The signed-in Google account
     * @param bucketName The bucket name to create
     * @return true if bucket was created or already exists, false otherwise
     */
    fun createBucketIfNeeded(context: Context, account: GoogleSignInAccount, bucketName: String): Boolean {
        try {
            Log.d(TAG, "Checking/creating bucket: $bucketName")

            // Set up credentials for Cloud Storage
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL)
            )
            credential.selectedAccount = account.account

            // Set up Cloud Storage client
            val storage = Storage.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("CineQuizRoyale")
                .build()

            // Check if bucket exists
            try {
                val bucket = storage.buckets().get(bucketName).execute()
                Log.d(TAG, "Bucket already exists: ${bucket.name}")
                return true
            } catch (e: Exception) {
                // Bucket doesn't exist or access denied, try to create it
                if (e.message?.contains("404") == true) {
                    try {
                        Log.d(TAG, "Bucket not found, attempting to create")
                        val newBucket = Bucket().setName(bucketName)
                        val createdBucket = storage.buckets().insert(account.id, newBucket).execute()
                        Log.d(TAG, "Bucket created: ${createdBucket.name}")
                        return true
                    } catch (e2: Exception) {
                        Log.e(TAG, "Failed to create bucket: ${e2.message}", e2)
                        return false
                    }
                } else {
                    Log.e(TAG, "Error checking bucket: ${e.message}", e)
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "General error in createBucketIfNeeded: ${e.message}", e)
            return false
        }
    }
}