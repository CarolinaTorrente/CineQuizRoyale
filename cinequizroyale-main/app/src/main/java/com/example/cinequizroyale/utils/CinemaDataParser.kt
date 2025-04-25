package com.example.cinequizroyale.utils

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.storage.model.StorageObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CinemaDataParser {
    private const val TAG = "CinemaDataParser"
    private const val BUCKET_NAME = "cinequizroyale-bucket1" // Same as QuestionUploader
    private const val CINEMAS_PATH = "cinemas/locations.json"

    // Sample movies that we can assign to cinemas
    private val popularMovies = listOf(
        "Dune: Part Two",
        "Oppenheimer",
        "Deadpool & Wolverine",
        "Inside Out 2",
        "Gladiator II",
        "Alien: Romulus",
        "Poor Things",
        "The Batman",
        "Mission: Impossible – The Final Reckoning",
        "Venom 3",
        "Transformers: One",
        "The Zone of Interest",
        "Kingdom of the Planet of the Apes",
        "Furiosa",
        "Blade Runner 2049",
        "Avatar: The Way of Water"
    )

    // Parse cinema text data and convert to JSON format
    fun parseCinemaData(cinemaText: String): List<Cinema> {
        val cinemaList = mutableListOf<Cinema>()
        val lines = cinemaText.trim().split("\n\n")

        for (cinemaEntry in lines) {
            try {
                val cinemaLines = cinemaEntry.trim().split("\n")
                if (cinemaLines.isEmpty()) continue

                val name = cinemaLines[0].trim()
                var address = ""

                if (cinemaLines.size > 1) {
                    address = cinemaLines[1].trim()
                }

                // Extract location from Madrid address
                // For the sake of this example, we'll generate random locations around Madrid
                // In a real app, you'd use geocoding to get accurate coordinates
                val random = Random()
                val baseLatitude = 40.4168 // Madrid center latitude
                val baseLongitude = -3.7038 // Madrid center longitude

                // Generate random offset (max ~2km in any direction)
                val latOffset = (random.nextDouble() - 0.5) * 0.04
                val lngOffset = (random.nextDouble() - 0.5) * 0.04

                val latitude = baseLatitude + latOffset
                val longitude = baseLongitude + lngOffset

                // Generate random phone number
                val phone = "+34 " + (900 + random.nextInt(100)) + " " +
                        (100 + random.nextInt(900)) + " " +
                        (100 + random.nextInt(900))

                // Generate website based on cinema name
                val simplifiedName = name.replace(Regex("[^a-zA-Z0-9]"), "").toLowerCase(Locale.ROOT)
                val website = "www.${simplifiedName}.es"

                // Random opening hours
                val openingTime = (10 + random.nextInt(3)).toString().padStart(2, '0') + ":00"
                val closingTime = (21 + random.nextInt(4)).toString().padStart(2, '0') + ":00"
                val openingHours = "Mon-Sun: $openingTime - $closingTime"

                // Random ticket prices
                val basePrice = 7 + random.nextInt(5)
                val discountPrice = basePrice - 2
                val ticketPrices = "General: ${basePrice}.90€, Reduced: ${discountPrice}.50€"

                // Random facilities
                val facilities = mutableListOf<String>()
                val screenCount = 2 + random.nextInt(18)
                facilities.add("$screenCount Screens")

                if (random.nextBoolean()) facilities.add("Digital Projection")
                if (random.nextBoolean()) facilities.add("3D")
                if (random.nextDouble() < 0.3) facilities.add("IMAX") // 30% chance
                if (random.nextBoolean()) facilities.add("Dolby Sound")
                if (random.nextDouble() < 0.7) facilities.add("Concessions") // 70% chance
                if (random.nextDouble() < 0.4) facilities.add("VIP Seating") // 40% chance

                // Generate 3-5 random movies for this cinema
                val movieCount = 3 + random.nextInt(3)
                val movieSet = mutableSetOf<String>()
                while (movieSet.size < movieCount) {
                    movieSet.add(popularMovies[random.nextInt(popularMovies.size)])
                }

                cinemaList.add(
                    Cinema(
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        address = address,
                        phone = phone,
                        website = website,
                        openingHours = openingHours,
                        ticketPrices = ticketPrices,
                        facilities = facilities.joinToString(", "),
                        currentMovies = movieSet.toList()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cinema entry: $cinemaEntry", e)
            }
        }

        return cinemaList
    }

    // Upload parsed cinema data to Cloud Storage
    suspend fun uploadCinemaData(context: Context, account: GoogleSignInAccount, cinemas: List<Cinema>): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Starting upload of ${cinemas.size} cinemas")

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

                // Convert cinemas to JSON
                val cinemasArray = JSONArray()

                for (cinema in cinemas) {
                    val cinemaObj = JSONObject().apply {
                        put("name", cinema.name)
                        put("latitude", cinema.latitude)
                        put("longitude", cinema.longitude)
                        put("address", cinema.address)
                        put("phone", cinema.phone)
                        put("website", cinema.website)
                        put("openingHours", cinema.openingHours)
                        put("ticketPrices", cinema.ticketPrices)
                        put("facilities", cinema.facilities)

                        // Add current movies as a JSON array
                        val moviesArray = JSONArray()
                        for (movie in cinema.currentMovies) {
                            moviesArray.put(movie)
                        }
                        put("currentMovies", moviesArray)
                    }
                    cinemasArray.put(cinemaObj)
                }

                // Convert to JSON string with pretty printing
                val cinemasData = cinemasArray.toString(2)

                // Convert to input stream
                val inputStream = ByteArrayInputStream(cinemasData.toByteArray())

                // Create StorageObject metadata
                val storageObject = StorageObject()
                    .setName(CINEMAS_PATH)
                    .setContentType("application/json")

                // Upload to Cloud Storage
                val result = storage.objects().insert(
                    BUCKET_NAME,
                    storageObject,
                    InputStreamContent("application/json", inputStream)
                ).execute()

                val success = result != null
                if (success) {
                    Log.d(TAG, "Cinema data uploaded successfully")
                } else {
                    Log.e(TAG, "Failed to upload cinema data")
                }

                success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading cinema data: ${e.message}", e)
            false
        }
    }
}