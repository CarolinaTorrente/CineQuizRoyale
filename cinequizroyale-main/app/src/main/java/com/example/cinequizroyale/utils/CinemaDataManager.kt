package com.example.cinequizroyale.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.Collections

data class Cinema(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val phone: String = "",
    val website: String = "",
    val openingHours: String = "10:00 - 23:00",
    val ticketPrices: String = "From 8€",
    val facilities: String = "Digital Projection, Dolby Sound",
    val currentMovies: List<String> = listOf()
)

class CinemaDataManager(private val context: Context) {
    private val tag = "CinemaDataManager"
    private val bucketName = "cinequizroyale-bucket1" // Match the bucket name from QuestionUploader
    private val cinemasFilePath = "cinemas/locations.json"

    init {
        Log.d(tag, "Initializing CinemaDataManager with bucket: $bucketName and path: $cinemasFilePath")
    }

    // Get hardcoded cinemas as fallback with more detailed information
    fun getHardcodedCinemas(): List<Cinema> {
        Log.d(tag, "Getting hardcoded cinemas as fallback")
        return listOf(
            Cinema(
                name = "Cines Callao",
                latitude = 40.4183,
                longitude = -3.7070,
                address = "Pza. Callao 3, 28013, Madrid",
                phone = "+34 915 22 58 01",
                website = "cinescallao.es",
                openingHours = "Mon-Sun: 16:00-23:30",
                ticketPrices = "General: 9.50€, Reduced: 7.90€",
                facilities = "Digital Projection, 2 Screens, Concessions",
                currentMovies = listOf("Dune: Part Two", "The Batman", "Oppenheimer")
            ),
            Cinema(
                name = "Yelmo Cines Ideal",
                latitude = 40.4233,
                longitude = -3.7044,
                address = "Doctor Cortezo 6, 28012, Madrid",
                phone = "+34 902 22 09 22",
                website = "yelmocines.es",
                openingHours = "Mon-Sun: 15:30-00:00",
                ticketPrices = "General: 8.90€, Student: 7.50€",
                facilities = "8 Screens, IMAX, Dolby Atmos, Snack Bar",
                currentMovies = listOf("Gladiator II", "Furiosa", "Alien: Romulus")
            ),
            Cinema(
                name = "Cinesa Manoteras",
                latitude = 40.4381,
                longitude = -3.6919,
                address = "Avenida de Manoteras 40, 28050, Madrid",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/manoteras",
                openingHours = "Mon-Thu: 16:00-23:00, Fri-Sun: 15:00-01:00",
                ticketPrices = "General: 9.20€, Special Days: 6.90€",
                facilities = "20 Screens, iSense, 4DX, Luxury Seats",
                currentMovies = listOf("Inside Out 2", "Transformers One", "Kingdom of the Planet of the Apes")
            ),
            Cinema(
                name = "Cines Renoir Plaza de España",
                latitude = 40.3906,
                longitude = -3.6996,
                address = "Martín de los Heros 12, 28008, Madrid",
                phone = "+34 915 41 41 00",
                website = "cinesrenoir.com",
                openingHours = "Mon-Sun: 16:15-22:30",
                ticketPrices = "General: 9.00€, Students/Senior: 7.50€",
                facilities = "5 Screens, Art House Films, Original Version",
                currentMovies = listOf("Poor Things", "The Zone of Interest", "Fallen Leaves")
            ),
            Cinema(
                name = "Cinesa Heron City",
                latitude = 40.4425,
                longitude = -3.6775,
                address = "CC Heron City Las Rozas, avda. Juan Ramón Jiménez 3, 28230, Las Rozas",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Las-Rozas",
                openingHours = "Mon-Sun: 16:00-00:00",
                ticketPrices = "General: 9.00€, Family Day: 6.90€",
                facilities = "17 Screens, IMAX, Luxury Recliners, VIP Zone",
                currentMovies = listOf("Deadpool & Wolverine", "Mission: Impossible – The Final Reckoning", "Venom 3")
            ),
            Cinema(
                name = "Cines Renoir Plaza de España",
                latitude = 40.3906,
                longitude = -3.6996,
                address = "Martín de los Heros 12, 28008, Madrid",
                phone = "+34 915 41 41 00",
                website = "cinesrenoir.com",
                openingHours = "Mon-Sun: 16:15-22:30",
                ticketPrices = "General: 9.00€, Students/Senior: 7.50€",
                facilities = "5 Screens, Art House Films, Original Version",
                currentMovies = listOf("Poor Things", "The Zone of Interest", "Fallen Leaves")
            ),
            Cinema(
                name = "Cinesa Heron City",
                latitude = 40.4425,
                longitude = -3.6775,
                address = "CC Heron City Las Rozas, avda. Juan Ramón Jiménez 3, 28230, Las Rozas",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Las-Rozas",
                openingHours = "Mon-Sun: 16:00-00:00",
                ticketPrices = "General: 9.00€, Family Day: 6.90€",
                facilities = "17 Screens, IMAX, Luxury Recliners, VIP Zone",
                currentMovies = listOf("Deadpool & Wolverine", "Mission: Impossible – The Final Reckoning", "Venom 3")
            ),
            Cinema(
                name = "Ocine Quadernillos",
                latitude = 40.4812,
                longitude = -3.3672,
                address = "Parque Cuadernillos, Autovía A-2 (salidas 34 y 35), 28805, Alcalá de Henares",
                phone = "+34 918 77 73 54",
                website = "ocine.es/cine/quadernillos",
                openingHours = "Mon-Sun: 16:30-23:00",
                ticketPrices = "General: 8.50€, Reduced: 7.00€, Wednesday: 5.90€",
                facilities = "8 Screens, Digital Sound, 3D Capability, Snack Bar",
                currentMovies = listOf("Dune: Part Two", "Inside Out 2", "Alien: Romulus")
            ),
            Cinema(
                name = "Kinépolis Diversia Alcobendas",
                latitude = 40.5367,
                longitude = -3.6415,
                address = "Parque de Ocio Diversia, Avda. Bruselas 21, 28108, Alcobendas",
                phone = "+34 902 54 86 86",
                website = "kinepolis.es/cines/kinepolis-madrid-diversia",
                openingHours = "Mon-Sun: 15:30-23:30",
                ticketPrices = "General: 9.80€, Students: 8.20€, Special Day: 6.50€",
                facilities = "12 Screens, Ultra HD, Premium Sound, Disabled Access",
                currentMovies = listOf("Joker: Folie à Deux", "Gladiator II", "Wicked")
            ),
            Cinema(
                name = "Yelmo Cines Tres Aguas 3D",
                latitude = 40.3387,
                longitude = -3.8358,
                address = "CC Tres Aguas Avda. de América 7-9, 28922, Alcorcón",
                phone = "+34 902 22 09 22",
                website = "yelmocines.es/cines-tres-aguas",
                openingHours = "Mon-Sun: 16:00-00:30",
                ticketPrices = "General: 8.90€, Students: 7.50€, Premium Seats: +2.00€",
                facilities = "15 Screens, 3D, Dolby Atmos, Premium Lounges",
                currentMovies = listOf("Furiosa", "The Fall Guy", "Twisters")
            ),
            Cinema(
                name = "Cinesa Intu Xanadú 3D",
                latitude = 40.2978,
                longitude = -3.9123,
                address = "Ctra. N-V Km. 23,500, 28939, Arroyomolinos",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Intu-Xanadu",
                openingHours = "Mon-Sun: 15:45-01:00",
                ticketPrices = "General: 9.20€, VIP Experience: 13.50€, Monday Discount: 6.90€",
                facilities = "24 Screens, IMAX, Luxury VIP Area, 4DX Motion Seats",
                currentMovies = listOf("Star Wars: New Jedi Order", "The Batman: Part II", "Jurassic World: Rebirth")
            ),
            Cinema(
                name = "Yelmo Cines Planetocio 3D",
                latitude = 40.6378,
                longitude = -4.0142,
                address = "Avda. Juan Carlos I 46, 28400, Collado Villalba",
                phone = "+34 902 22 09 22",
                website = "yelmocines.es/cines-planetocio-villalba",
                openingHours = "Mon-Sun: 16:30-23:45",
                ticketPrices = "General: 8.70€, Discount Tuesday: 5.90€",
                facilities = "10 Screens, 3D Technology, Stadium Seating, Accessible Facilities",
                currentMovies = listOf("Avatar 3", "Fast & Furious 11", "Oppenheimer 2")
            ),
            Cinema(
                name = "Cines La Rambla 3D",
                latitude = 40.4266,
                longitude = -3.5352,
                address = "CC La Rambla Honduras s/n, 28820, Coslada",
                phone = "+34 916 74 65 60",
                website = "cineslarambla.com",
                openingHours = "Mon-Fri: 17:00-22:30, Sat-Sun: 16:00-00:00",
                ticketPrices = "General: 8.00€, Children/Seniors: 6.50€",
                facilities = "7 Screens, Digital Projection, Family Area, Snack Service",
                currentMovies = listOf("Wonka 2", "Spider-Man 4", "The Little Mermaid: Return to the Sea")
            ),
            Cinema(
                name = "Cinesa Loranca 3D",
                latitude = 40.2852,
                longitude = -3.7891,
                address = "CC Loranca, Avda. Pablo Iglesias 17, 28942, Fuenlabrada",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Loranca",
                openingHours = "Mon-Thu: 16:30-22:00, Fri-Sun: 16:00-00:30",
                ticketPrices = "General: 8.50€, Senior: 7.20€, Student: 7.50€",
                facilities = "9 Screens, RealD 3D, Dolby Digital Sound, Wheelchair Access",
                currentMovies = listOf("Black Panther 3", "John Wick 5", "The Penguin")
            ),
            Cinema(
                name = "Cinesa Parquesur 3D",
                latitude = 40.3258,
                longitude = -3.7536,
                address = "Pl. de las Barcas 11, 28916, Leganés",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Parquesur",
                openingHours = "Mon-Sun: 16:15-01:15",
                ticketPrices = "General: 9.20€, Family Package: 26.00€ (4 tickets)",
                facilities = "14 Screens, ISENSE, Premium Services, Food Court Access",
                currentMovies = listOf("The Marvels 2", "Mission: Impossible 8", "The Conjuring 4")
            ),
            Cinema(
                name = "Cines Princesa",
                latitude = 40.4283,
                longitude = -3.7172,
                address = "Princesa 3, 28008, Madrid",
                phone = "+34 915 47 25 11",
                website = "cinesprincesa.com",
                openingHours = "Mon-Sun: 16:00-22:45",
                ticketPrices = "General: 9.50€, Cultural Thursday: 7.50€",
                facilities = "7 Screens, Original Version Films, Independent Cinema, Café",
                currentMovies = listOf("The Substance", "Priscilla", "All of Us Strangers")
            ),
            Cinema(
                name = "Renoir Retiro",
                latitude = 40.4195,
                longitude = -3.6741,
                address = "Narváez 42, 28009, Madrid",
                phone = "+34 915 74 76 22",
                website = "cinesrenoir.com/cines/renoir-retiro",
                openingHours = "Mon-Sun: 16:30-22:15",
                ticketPrices = "General: 9.00€, Students: 7.50€, Monday Promotion: 6.50€",
                facilities = "4 Screens, European Cinema, Original Language, Cozy Setting",
                currentMovies = listOf("Perfect Days", "The Boy and the Heron", "Anatomy of a Fall")
            ),
            Cinema(
                name = "Cines Verdi Madrid",
                latitude = 40.4318,
                longitude = -3.7046,
                address = "Bravo Murillo 28, 28015, Madrid",
                phone = "+34 914 47 66 81",
                website = "cinesverdi.com/madrid",
                openingHours = "Mon-Sun: 16:00-23:00",
                ticketPrices = "General: 9.90€, Verdi Club: 7.90€, Sunday Morning: 7.50€",
                facilities = "5 Screens, Art House Films, Film Festival Venue, Bookshop",
                currentMovies = listOf("Another Round 2", "The Favourite 2", "Parasite: Director's Cut")
            ),
            Cinema(
                name = "Cine Capitol",
                latitude = 40.4202,
                longitude = -3.7059,
                address = "Gran Vía, 41, 28013, Madrid",
                phone = "+34 915 22 22 29",
                website = "cinecapitol.es",
                openingHours = "Mon-Sun: 16:30-23:00",
                ticketPrices = "General: 10.00€, Historic Tour + Movie: 15.00€",
                facilities = "Single Screen, Historic Building, Premium Sound, Art Deco Interior",
                currentMovies = listOf("La La Land 2", "Indiana Jones and the Lost City", "Top Gun 3")
            ),
            Cinema(
                name = "Cinesa Las Rosas 3D",
                latitude = 40.4283,
                longitude = -3.6113,
                address = "Avda. Guadalajara 2, 28032, Madrid",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Las-Rosas",
                openingHours = "Mon-Fri: 17:00-23:30, Sat-Sun: 16:00-01:00",
                ticketPrices = "General: 8.90€, Children: 6.90€, Premium Experience: 12.50€",
                facilities = "12 Screens, 3D Technology, Luxury Seating, Free Parking",
                currentMovies = listOf("Batman vs Superman 2", "Avatar 3", "The Greatest Showman 2")
            ),
            Cinema(
                name = "Cinesa Proyecciones 3D",
                latitude = 40.4296,
                longitude = -3.7023,
                address = "Fuencarral 136, 28010, Madrid",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Proyecciones",
                openingHours = "Mon-Sun: 16:15-00:00",
                ticketPrices = "General: 9.40€, Wednesday Special: 7.50€",
                facilities = "8 Screens, Premium Club, Urban Location, VIP Experience",
                currentMovies = listOf("The Matrix Resurrections 2", "No Time To Die 2", "Morbius 2")
            ),
            Cinema(
                name = "Cinesa Príncipe Pío 3D",
                latitude = 40.4211,
                longitude = -3.7197,
                address = "Paseo de la Florida s/n, 28005, Madrid",
                phone = "+34 902 33 32 31",
                website = "cinesa.es/Cines/Principe-Pio",
                openingHours = "Mon-Fri: 16:15-23:30, Sat-Sun: 15:45-01:00",
                ticketPrices = "General: 9.00€, Weekend Premium: 10.50€, Special Monday: 6.90€",
                facilities = "9 Screens, Shopping Center Location, Digital Sound, Family Friendly",
                currentMovies = listOf("Godzilla x Kong 2", "Knives Out 3", "Ant-Man 4")
            ),
            Cinema(
                name = "Conde Duque Verdi Alberto Aguilera",
                latitude = 40.4300,
                longitude = -3.7104,
                address = "Alberto Aguilera 4, 28015, Madrid",
                phone = "+34 915 47 85 00",
                website = "condeduque.es/cines",
                openingHours = "Mon-Thu: 16:30-22:00, Fri-Sun: 16:00-23:30",
                ticketPrices = "General: 9.50€, Students: 7.90€, Senior Discount: 7.50€",
                facilities = "6 Screens, European Films, Original Version, Boutique Experience",
                currentMovies = listOf("Triangle of Sadness 2", "Close", "Decision to Leave")
            ),
            Cinema(
                name = "Conde Duque Goya",
                latitude = 40.4252,
                longitude = -3.6769,
                address = "Goya 67, 28001, Madrid",
                phone = "+34 915 77 60 09",
                website = "condeduque.es/cines/goya",
                openingHours = "Mon-Sun: 16:00-22:30",
                ticketPrices = "General: 9.50€, Early Sessions: 7.50€",
                facilities = "3 Screens, Subtitled Films, Premium Audio, Café-Bar",
                currentMovies = listOf("Aftersun", "Past Lives", "The Worst Person in the World")
            ),
            Cinema(
                name = "MK2 Palacio de Hielo",
                latitude = 40.4599,
                longitude = -3.6590,
                address = "CC Dreams Palacio de Hielo, Silvano 77, 28043, Madrid",
                phone = "+34 902 18 01 93",
                website = "mk2palaciodehielo.es",
                openingHours = "Mon-Thu: 16:45-22:45, Fri-Sun: 16:00-01:15",
                ticketPrices = "General: 9.00€, Premium Seats: 12.00€, Family Package: 28.00€",
                facilities = "15 Screens, Ice Rink View, Ultra HD Projection, Gourmet Concessions",
                currentMovies = listOf("Frozen 3", "Jurassic World 4", "Wonder Woman 3")
            )
        )
    }

    // Initialize and upload cinema data
    suspend fun initializeCinemaData(account: GoogleSignInAccount? = null): Boolean {
        val userAccount = account ?: GoogleSignIn.getLastSignedInAccount(context)
        if (userAccount == null) {
            Log.e(tag, "No signed-in account found")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Setting up credentials for Cloud Storage")
                Log.d(tag, "User account: ${userAccount.email}, ID: ${userAccount.id}")

                // Log the Scope being used
                val scope = Collections.singleton(StorageScopes.DEVSTORAGE_READ_WRITE)
                Log.d(tag, "Using scope: $scope")

                // Set up credentials for Cloud Storage, using the approach from QuestionUploader
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    scope
                )
                credential.selectedAccount = userAccount.account
                Log.d(tag, "Credential created with account: ${userAccount.account?.name}")

                // Set up Cloud Storage client
                Log.d(tag, "Creating Storage client")
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()

                val storage = Storage.Builder(
                    transport,
                    jsonFactory,
                    credential
                )
                    .setApplicationName("CineQuizRoyale") // Match application name with QuestionUploader
                    .build()

                Log.d(tag, "Storage client created")

                // Get cinema data
                val cinemas = getHardcodedCinemas()
                Log.d(tag, "Using ${cinemas.size} hardcoded cinemas for initialization")

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

                // Convert to JSON string
                val cinemasData = cinemasArray.toString(2) // Pretty print with 2-space indentation

                Log.d(tag, "Cinema data JSON created: ${cinemasData.length} chars")
                // Log a snippet
                val snippet = cinemasData.take(200)
                Log.d(tag, "JSON snippet: $snippet...")

                // Create input stream from JSON string
                val inputStream = ByteArrayInputStream(cinemasData.toByteArray())

                Log.d(tag, "Attempting to upload cinema data to $bucketName/$cinemasFilePath")

                try {
                    // First try to check if bucket exists
                    val bucket = storage.buckets().get(bucketName).execute()
                    Log.d(tag, "Successfully connected to bucket: ${bucket.name}, created at: ${bucket.timeCreated}")
                } catch (e: Exception) {
                    Log.e(tag, "Error checking bucket: ${e.message}", e)
                    Log.w(tag, "Will attempt to proceed with upload anyway")
                }

                // Create StorageObject metadata
                val storageObject = StorageObject()
                    .setName(cinemasFilePath)
                    .setContentType("application/json")

                Log.d(tag, "StorageObject created with name: ${storageObject.name}")

                // Upload to Cloud Storage
                try {
                    Log.d(tag, "Starting upload...")
                    val result = storage.objects().insert(
                        bucketName,
                        storageObject,
                        InputStreamContent("application/json", inputStream)
                    ).execute()

                    val success = result != null && result.name == cinemasFilePath

                    if (success) {
                        Log.d(tag, "Successfully uploaded cinema data to Cloud Storage")
                        Log.d(tag, "Result object: name=${result.name}, size=${result.size}, mediaLink=${result.mediaLink}")
                    } else {
                        Log.e(tag, "Failed to upload cinema data: result didn't match expected path")
                        if (result != null) {
                            Log.e(tag, "Upload result: name=${result.name}, expected: $cinemasFilePath")
                        } else {
                            Log.e(tag, "Upload result is null")
                        }
                    }

                    return@withContext success
                } catch (e: Exception) {
                    Log.e(tag, "Error during upload: ${e.message}", e)

                    // Check for Google Cloud Storage specific errors
                    if (e.message?.contains("403") == true) {
                        Log.e(tag, "Possible permissions issue (403 Forbidden)")
                    } else if (e.message?.contains("404") == true) {
                        Log.e(tag, "Possible bucket not found issue (404 Not Found)")
                    } else if (e.message?.contains("401") == true) {
                        Log.e(tag, "Possible authentication issue (401 Unauthorized)")
                    }

                    return@withContext false
                }

            } catch (e: Exception) {
                Log.e(tag, "Error uploading cinema data: ${e.message}", e)
                e.printStackTrace() // Print full stack trace
                return@withContext false
            }
        }
    }

    // Get cinemas from Cloud Storage
    suspend fun getCinemas(account: GoogleSignInAccount? = null): List<Cinema> {
        val userAccount = account ?: GoogleSignIn.getLastSignedInAccount(context)
        if (userAccount == null) {
            Log.e(tag, "No signed-in account found for getCinemas")
            return getHardcodedCinemas()
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Setting up credentials for fetching cinema data")
                Log.d(tag, "User account: ${userAccount.email}, ID: ${userAccount.id}")

                // Set up credentials for Cloud Storage
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(StorageScopes.DEVSTORAGE_READ_WRITE)
                )
                credential.selectedAccount = userAccount.account
                Log.d(tag, "Credential created with account: ${userAccount.account?.name}")

                // Set up Cloud Storage client
                val storage = Storage.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName("CineQuizRoyale")
                    .build()

                Log.d(tag, "Storage client created")

                try {
                    Log.d(tag, "Attempting to fetch cinemas from $bucketName/$cinemasFilePath")

                    // Check if the file exists first
                    try {
                        val obj = storage.objects().get(bucketName, cinemasFilePath).execute()
                        Log.d(tag, "File exists: ${obj.name}, size: ${obj.size}, updated: ${obj.updated}")
                    } catch (e: Exception) {
                        Log.e(tag, "File does not exist or cannot be accessed: ${e.message}", e)
                        Log.d(tag, "Trying to initialize cinema data first")

                        val initialized = initializeCinemaData(userAccount)
                        if (!initialized) {
                            Log.e(tag, "Failed to initialize cinema data, falling back to hardcoded")
                            return@withContext getHardcodedCinemas()
                        }
                    }

                    // Get the cinema data file
                    try {
                        val cinemasStream = storage.objects().get(bucketName, cinemasFilePath)
                            .executeMedia()
                            .content

                        val cinemasData = cinemasStream.bufferedReader().use { it.readText() }
                        Log.d(tag, "Successfully retrieved cinema data: ${cinemasData.length} bytes")

                        // Log a snippet of the content
                        val snippet = cinemasData.take(200)
                        Log.d(tag, "Content snippet: $snippet...")

                        // Parse JSON and return cinemas list
                        val cinemas = parseCinemaData(cinemasData)

                        if (cinemas.isEmpty()) {
                            Log.w(tag, "Parsed cinema list is empty, using hardcoded data")
                            return@withContext getHardcodedCinemas()
                        }

                        Log.d(tag, "Successfully returned ${cinemas.size} cinemas from Cloud Storage")
                        return@withContext cinemas
                    } catch (e: Exception) {
                        Log.e(tag, "Error getting cinema data from storage: ${e.message}", e)
                        return@withContext getHardcodedCinemas()
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error fetching cinema data: ${e.message}", e)
                    Log.d(tag, "Trying to initialize cinema data first, then fetch again")

                    // Try to initialize the data
                    val initialized = initializeCinemaData(userAccount)

                    if (initialized) {
                        try {
                            // Try again after initialization
                            val cinemasStream = storage.objects().get(bucketName, cinemasFilePath)
                                .executeMedia()
                                .content

                            val cinemasData = cinemasStream.bufferedReader().use { it.readText() }
                            val cinemas = parseCinemaData(cinemasData)

                            if (cinemas.isNotEmpty()) {
                                Log.d(tag, "Successfully fetched cinemas after initialization")
                                return@withContext cinemas
                            }
                        } catch (e2: Exception) {
                            Log.e(tag, "Error fetching cinema data after initialization: ${e2.message}", e2)
                        }
                    }

                    // Return hardcoded values as fallback
                    Log.d(tag, "Using hardcoded cinema data as fallback")
                    return@withContext getHardcodedCinemas()
                }
            } catch (e: Exception) {
                Log.e(tag, "General error in getCinemas: ${e.message}", e)
                return@withContext getHardcodedCinemas()
            }
        }
    }

    private fun parseCinemaData(data: String): List<Cinema> {
        val cinemasList = mutableListOf<Cinema>()
        try {
            val jsonArray = JSONArray(data)
            Log.d(tag, "Parsing JSON array with ${jsonArray.length()} items")

            for (i in 0 until jsonArray.length()) {
                val cinema = jsonArray.getJSONObject(i)

                // Parse the movies array
                val moviesArray = cinema.optJSONArray("currentMovies") ?: JSONArray()
                val moviesList = mutableListOf<String>()
                for (j in 0 until moviesArray.length()) {
                    moviesList.add(moviesArray.getString(j))
                }

                val cinemaObj = Cinema(
                    name = cinema.getString("name"),
                    latitude = cinema.getDouble("latitude"),
                    longitude = cinema.getDouble("longitude"),
                    address = cinema.optString("address", ""),
                    phone = cinema.optString("phone", ""),
                    website = cinema.optString("website", ""),
                    openingHours = cinema.optString("openingHours", "10:00 - 23:00"),
                    ticketPrices = cinema.optString("ticketPrices", "From 8€"),
                    facilities = cinema.optString("facilities", "Digital Projection"),
                    currentMovies = moviesList
                )

                cinemasList.add(cinemaObj)

                // Log first 3 cinemas for debugging
                if (i < 3) {
                    Log.d(tag, "Parsed Cinema $i: ${cinemaObj.name}, lat=${cinemaObj.latitude}, lng=${cinemaObj.longitude}")
                }
            }
            Log.d(tag, "Successfully parsed ${cinemasList.size} cinemas")
        } catch (e: Exception) {
            Log.e(tag, "Error parsing cinema data: ${e.message}", e)
            e.printStackTrace()
        }
        return cinemasList
    }}