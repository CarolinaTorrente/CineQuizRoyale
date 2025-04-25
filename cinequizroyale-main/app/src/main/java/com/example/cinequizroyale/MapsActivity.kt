package com.example.cinequizroyale

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinequizroyale.utils.Cinema
import com.example.cinequizroyale.utils.CinemaDataManager
import com.example.cinequizroyale.utils.CinemaDataParser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.io.BufferedReader
import java.io.InputStreamReader

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private val tag = "MapsActivity"
    private lateinit var cinemaDataManager: CinemaDataManager
    private val cinemasList = mutableListOf<Cinema>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>
    private lateinit var cinemaInfoCard: MaterialCardView
    private lateinit var cinemaNameText: TextView
    private lateinit var cinemaAddressText: TextView
    private lateinit var cinemaPhoneText: TextView
    private lateinit var cinemaHoursText: TextView
    private lateinit var cinemaPricesText: TextView
    private lateinit var cinemaFacilitiesText: TextView
    private lateinit var moviesRecyclerView: RecyclerView
    private lateinit var cinemaWebsiteText: TextView
    private lateinit var backButton: ImageButton

    private val markerCinemaMap = mutableMapOf<Marker, Cinema>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "MapsActivity onCreate started - Debug Version")

        try {
            setContentView(R.layout.activity_maps)
            Log.d(tag, "setContentView completed")

            // back button
            setupBackButton()

        } catch (e: Exception) {
            Log.e(tag, "Error setting content view: ${e.message}", e)
            Toast.makeText(this, "Error loading map layout: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Check if user is signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            Log.e(tag, "No signed-in account found")
            Toast.makeText(this, "Not signed in. Please sign in first.", Toast.LENGTH_LONG).show()
        } else {
            Log.d(tag, "User is signed in: ${account.email}")
        }

        // Initialize CinemaDataManager
        try {
            cinemaDataManager = CinemaDataManager(this)
            Log.d(tag, "CinemaDataManager initialized")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing CinemaDataManager: ${e.message}", e)
            Toast.makeText(this, "Error initializing cinema data: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Check if required views exist in layout
        try {
            // Initialize Cinema Info Card and its components
            setupCinemaInfoCard()
            Log.d(tag, "Cinema info card setup completed")
        } catch (e: Exception) {
            Log.e(tag, "Error setting up cinema info card: ${e.message}", e)
            Toast.makeText(this, "Error setting up UI components: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Get the SupportMapFragment
        try {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            Log.d(tag, "Map fragment obtained")

            // Fetch cinema data and then initialize the map
            fetchCinemaData {
                Log.d(tag, "Calling getMapAsync")
                mapFragment.getMapAsync(this)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting map fragment: ${e.message}", e)
            Toast.makeText(this, "Error loading Google Maps: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCinemaInfoCard() {
        Log.d(tag, "Setting up cinema info card")

        try {
            // Find views
            cinemaInfoCard = findViewById(R.id.cinema_info_card)
            cinemaNameText = findViewById(R.id.cinema_name)
            cinemaAddressText = findViewById(R.id.cinema_address)
            cinemaPhoneText = findViewById(R.id.cinema_phone)
            cinemaHoursText = findViewById(R.id.cinema_hours)
            cinemaPricesText = findViewById(R.id.cinema_prices)
            cinemaFacilitiesText = findViewById(R.id.cinema_facilities)
            moviesRecyclerView = findViewById(R.id.movies_recycler_view)
            cinemaWebsiteText = findViewById(R.id.cinema_website)

            Log.d(tag, "All views found")

            // Set up RecyclerView
            moviesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            // Initialize bottom sheet behavior
            bottomSheetBehavior = BottomSheetBehavior.from(cinemaInfoCard)
            Log.d(tag, "Bottom sheet behavior initialized")

            // Initially hide the bottom sheet
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            // Add callback for bottom sheet state changes
            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Log.d(tag, "Bottom sheet state changed to: $newState")
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Optional: Do something during sliding
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Error in setupCinemaInfoCard: ${e.message}", e)
            throw e  // Rethrow to handle in calling method
        }
    }

    private fun fetchCinemaData(callback: () -> Unit) {
        Log.d(tag, "Fetching cinema data")
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account == null) {
            Log.e(tag, "No signed-in account found, will use hardcoded data")
            cinemasList.clear()

            try {
                val hardcodedCinemas = cinemaDataManager.getHardcodedCinemas()
                Log.d(tag, "Got ${hardcodedCinemas.size} hardcoded cinemas")
                cinemasList.addAll(hardcodedCinemas)
            } catch (e: Exception) {
                Log.e(tag, "Error getting hardcoded cinemas: ${e.message}", e)
            }

            Toast.makeText(this, "Using local cinema data (not signed in)", Toast.LENGTH_SHORT).show()
            callback()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            // Show loading message
            Toast.makeText(this@MapsActivity, "Loading cinema locations...", Toast.LENGTH_SHORT).show()

            try {
                // First try to read the cinema data from raw resources
                Log.d(tag, "Attempting to read cinemas from raw resources")
                val cinemasText = readCinemasFromRaw()

                if (cinemasText.isNotEmpty()) {
                    Log.d(tag, "Successfully read cinema text from raw: ${cinemasText.length} chars")

                    // Parse the cinema data
                    withContext(Dispatchers.IO) {
                        try {
                            Log.d(tag, "Parsing cinema data")
                            val parsedCinemas = CinemaDataParser.parseCinemaData(cinemasText)

                            if (parsedCinemas.isNotEmpty()) {
                                Log.d(tag, "Successfully parsed ${parsedCinemas.size} cinemas from raw resource")

                                // Print first few cinemas for debugging
                                parsedCinemas.take(3).forEachIndexed { index, cinema ->
                                    Log.d(tag, "Cinema $index: ${cinema.name}, Lat: ${cinema.latitude}, Lng: ${cinema.longitude}")
                                }

                                // Upload cinemas to Cloud Storage
                                Log.d(tag, "Attempting to upload cinemas to Cloud Storage")
                                val uploadSuccess = CinemaDataParser.uploadCinemaData(
                                    this@MapsActivity,
                                    account,
                                    parsedCinemas
                                )

                                if (uploadSuccess) {
                                    Log.d(tag, "Successfully uploaded cinemas to Cloud Storage")

                                    withContext(Dispatchers.Main) {
                                        cinemasList.clear()
                                        cinemasList.addAll(parsedCinemas)
                                        Toast.makeText(
                                            this@MapsActivity,
                                            "Loaded ${parsedCinemas.size} cinemas from list",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        callback()
                                    }
                                } else {
                                    // Try to get cinemas from Cloud Storage
                                    Log.d(tag, "Failed to upload cinemas, trying to fetch from Cloud Storage")
                                    fetchCinemasFromCloud(account, callback)
                                }
                            } else {
                                Log.w(tag, "No cinemas parsed from raw text data")
                                fetchCinemasFromCloud(account, callback)
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing cinema data: ${e.message}", e)
                            fetchCinemasFromCloud(account, callback)
                        }
                    }
                } else {
                    Log.w(tag, "No cinema text found in raw resources")
                    fetchCinemasFromCloud(account, callback)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error in fetchCinemaData: ${e.message}", e)
                fetchCinemasFromCloud(account, callback)
            }
        }
    }

    private suspend fun fetchCinemasFromCloud(account: GoogleSignInAccount, callback: () -> Unit) {
        Log.d(tag, "Fetching cinemas from cloud storage")

        withContext(Dispatchers.IO) {
            try {
                // Explicitly initialize cinema data first
                Log.d(tag, "Initializing cinema data in cloud")
                val initialized = cinemaDataManager.initializeCinemaData(account)
                Log.d(tag, "Cinema data initialization result: $initialized")

                // Then get the cinema list
                Log.d(tag, "Getting cinemas from cloud")
                val cinemas = cinemaDataManager.getCinemas(account)
                Log.d(tag, "Retrieved ${cinemas.size} cinemas from Cloud Storage")

                withContext(Dispatchers.Main) {
                    cinemasList.clear()
                    cinemasList.addAll(cinemas)

                    Toast.makeText(
                        this@MapsActivity,
                        "Loaded ${cinemas.size} cinemas from cloud",
                        Toast.LENGTH_SHORT
                    ).show()

                    callback()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching from cloud: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    // Fallback to hardcoded cinemas
                    cinemasList.clear()
                    try {
                        val hardcodedCinemas = cinemaDataManager.getHardcodedCinemas()
                        Log.d(tag, "Fallback to ${hardcodedCinemas.size} hardcoded cinemas")
                        cinemasList.addAll(hardcodedCinemas)
                    } catch (e2: Exception) {
                        Log.e(tag, "Error getting hardcoded cinemas: ${e2.message}", e2)
                    }

                    Toast.makeText(
                        this@MapsActivity,
                        "Error loading data: Using local cinema data",
                        Toast.LENGTH_SHORT
                    ).show()

                    callback()
                }
            }
        }
    }

    private fun readCinemasFromRaw(): String {
        return try {
            Log.d(tag, "Opening raw resource: R.raw.cinemas")
            // Read the cinema list from the raw resource
            val resourceId = resources.getIdentifier("cinemas.txt", "raw", packageName)

            if (resourceId == 0) {
                Log.e(tag, "Resource 'cinemas.txt' not found in raw folder")
                return ""
            }

            Log.d(tag, "Resource found with ID: $resourceId")

            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val text = reader.use { it.readText() }
            Log.d(tag, "Successfully read cinemas from raw resource: ${text.length} bytes")

            // Log a snippet of the content
            val snippet = text.take(100)
            Log.d(tag, "Content snippet: $snippet...")

            text
        } catch (e: Exception) {
            Log.e(tag, "Error reading cinemas from raw: ${e.message}", e)
            ""
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d(tag, "Map is ready")

        // Set marker click listener
        mMap.setOnMarkerClickListener(this)

        // If no cinemas were loaded, use hardcoded fallback again as safety
        if (cinemasList.isEmpty()) {
            Log.d(tag, "Cinema list is empty, using hardcoded data")
            try {
                val hardcodedCinemas = cinemaDataManager.getHardcodedCinemas()
                Log.d(tag, "Got ${hardcodedCinemas.size} hardcoded cinemas")
                cinemasList.addAll(hardcodedCinemas)
            } catch (e: Exception) {
                Log.e(tag, "Error getting hardcoded cinemas: ${e.message}", e)
            }
        }

        // Add markers for each cinema
        Log.d(tag, "Adding ${cinemasList.size} cinema markers to map")
        for (cinema in cinemasList) {
            val location = LatLng(cinema.latitude, cinema.longitude)
            Log.d(tag, "Adding marker for ${cinema.name} at (${cinema.latitude}, ${cinema.longitude})")
            try {
                val marker = mMap.addMarker(MarkerOptions().position(location).title(cinema.name))
                if (marker != null) {
                    markerCinemaMap[marker] = cinema
                } else {
                    Log.e(tag, "Failed to create marker for ${cinema.name}")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error adding marker for ${cinema.name}: ${e.message}", e)
            }
        }

        // Move camera to Madrid center
        try {
            val madrid = LatLng(40.4168, -3.7038)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 12f))
            Log.d(tag, "Camera moved to Madrid with zoom 12f")
        } catch (e: Exception) {
            Log.e(tag, "Error moving camera: ${e.message}", e)
        }

        // Set an empty click listener to hide the cinema info when clicking elsewhere on the map
        mMap.setOnMapClickListener {
            Log.d(tag, "Map clicked, hiding bottom sheet")
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(tag, "Marker clicked: ${marker.title}")

        // Find the cinema associated with this marker
        val cinema = markerCinemaMap[marker]

        if (cinema != null) {
            Log.d(tag, "Found cinema: ${cinema.name}")
            try {
                // Populate the cinema info card
                displayCinemaInfo(cinema)

                // Show the bottom sheet
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                Log.d(tag, "Bottom sheet displayed")

                // Center camera on the selected cinema
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))

                // Show info window
                marker.showInfoWindow()

                return true // We handled the event
            } catch (e: Exception) {
                Log.e(tag, "Error displaying cinema info: ${e.message}", e)
            }
        } else {
            Log.e(tag, "Cinema not found for marker: ${marker.title}")
        }

        return false // Let the default behavior occur
    }

    private fun displayCinemaInfo(cinema: Cinema) {
        try {
            // Set the cinema details in the card
            cinemaNameText.text = cinema.name
            cinemaAddressText.text = cinema.address
            cinemaPhoneText.text = cinema.phone
            cinemaHoursText.text = cinema.openingHours
            cinemaPricesText.text = cinema.ticketPrices
            cinemaFacilitiesText.text = cinema.facilities
            cinemaWebsiteText.text = cinema.website

            Log.d(tag, "Cinema details set to UI")

            // Set up the movies recycler view
            try {
                moviesRecyclerView.adapter = MoviesAdapter(cinema.currentMovies)
                Log.d(tag, "Movies adapter set with ${cinema.currentMovies.size} movies")
            } catch (e: Exception) {
                Log.e(tag, "Error setting movies adapter: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in displayCinemaInfo: ${e.message}", e)
        }
    }

    // back button
    private fun setupBackButton() {
        try {
            backButton = findViewById(R.id.back_button)
            backButton.setOnClickListener {
                // Simply finish the activity to go back
                finish()
            }
            Log.d(tag, "Back button set up successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error setting up back button: ${e.message}", e)
        }
    }

    // Inside your MapsActivity.kt file

    // Add this mapping between movie titles and image resources
    private val movieImageMap = mapOf(
        "Dune: Part Two" to R.drawable.dune_part_two,
        "Oppenheimer" to R.drawable.oppenheimer,
        "Deadpool & Wolverine" to R.drawable.deadpool_wolverine,
        "Tardes de Soledad" to R.drawable.tardes_de_soledad,
        "Gladiator II" to R.drawable.gladiator_2,
        "Alien: Romulus" to R.drawable.alien_romulus,
        "Mission: Impossible – The Final Reckoning" to R.drawable.mission_impossible,
        "Transformers: One" to R.drawable.transformers_one,
        "The Zone of Interest" to R.drawable.zone_of_interest,
        "Kingdom of the Planet of the Apes" to R.drawable.planet_of_apes,
        "Furiosa" to R.drawable.furiosa,
        "Blade Runner 2049" to R.drawable.blade_runner
        // Add more movies as needed
    )

    // MoviesAdapter class
    private inner class MoviesAdapter(private val movies: List<String>) :
        RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

        inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val movieTitle: TextView = view.findViewById(R.id.movie_title)
            val moviePoster: ImageView = view.findViewById(R.id.movie_poster)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
            try {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie, parent, false)
                return MovieViewHolder(view)
            } catch (e: Exception) {
                Log.e(tag, "Error creating movie view holder: ${e.message}", e)
                // Create a fallback view
                val textView = TextView(parent.context)
                return MovieViewHolder(textView)
            }
        }

        override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
            try {
                val movieTitle = movies[position]
                holder.movieTitle.text = movieTitle

                // Set the appropriate image based on the movie title
                val imageResource = movieImageMap[movieTitle] ?: R.drawable.pelicula // Fallback to default image
                holder.moviePoster.setImageResource(imageResource)
            } catch (e: Exception) {
                Log.e(tag, "Error binding movie view holder: ${e.message}", e)
            }
        }

        override fun getItemCount() = movies.size

    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "MapsActivity onResume")
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "MapsActivity onStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "MapsActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "MapsActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "MapsActivity onDestroy")
    }
}