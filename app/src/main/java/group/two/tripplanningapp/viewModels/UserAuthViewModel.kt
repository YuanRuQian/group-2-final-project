package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import group.two.tripplanningapp.data.LocaleConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _userLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()
    private val _isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    private var _localeConstants = MutableStateFlow<List<LocaleConstant>>(emptyList())

    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn
    val localeConstants: StateFlow<List<LocaleConstant>> get() = _localeConstants

    private var currentUserLocaleConstant: LocaleConstant? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        Log.d("UserAuthViewModel", "Auth state changed: user=${user?.uid}")
        _userLiveData.value = user
        _isUserLoggedIn.value = user != null
        if (user != null) {
            loadCurrentUserLocaleConstantCode(user.uid)
        }
    }


    init {
        auth.addAuthStateListener(authStateListener)
        loadLocaleConstants()
        if (auth.currentUser != null) {
            loadCurrentUserLocaleConstantCode(auth.currentUser!!.uid)
        }
    }

    private fun loadLocaleConstants() {
        CoroutineScope(Dispatchers.IO).launch {
            val localeConstants = mutableListOf<LocaleConstant>()
            try {
                val result = db.collection("localeConstants").get().await()
                for (document in result) {
                    val localeConstant = document.toObject(LocaleConstant::class.java)
                    localeConstants.add(localeConstant)
                }
                _localeConstants.value = localeConstants

                auth.currentUser?.uid?.let { loadCurrentUserLocaleConstantCode(it) }
            } catch (exception: Exception) {
                Log.e("UserAuthViewModel", "Error loading locale constants", exception)
            }
        }
    }


    private fun loadCurrentUserLocaleConstantCode(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 从 Firestore 获取用户的 locale constant code
                val documentSnapshot = db.collection("userProfiles").document(userId).get().await()
                val currentUserLocaleConstantCode = documentSnapshot.getString("localeConstantCode") ?: "en-US"
                Log.d("UserAuthViewModel", "Retrieved locale constant code for current user: $currentUserLocaleConstantCode")

                // 查找与 currentUserLocaleConstantCode 相匹配的 LocaleConstant
                currentUserLocaleConstant = _localeConstants.value.find { it.code == currentUserLocaleConstantCode }
                if (currentUserLocaleConstant != null) {
                    Log.d("UserAuthViewModel", "Found matching LocaleConstant: ${currentUserLocaleConstant?.code}")
                } else {
                    Log.d("UserAuthViewModel", "No matching LocaleConstant found for code: $currentUserLocaleConstantCode")
                }
            } catch (exception: Exception) {
                Log.e("UserAuthViewModel", "Error loading user profile for locale constant code", exception)
            }
        }
    }

    fun formatCurrency(priceInUSDCents: Int): String {
        Log.d("UserAuthViewModel", "Formatting currency: $priceInUSDCents")

        val locale = Locale.forLanguageTag(currentUserLocaleConstant?.code ?: "en-US")

        // Convert USD cents to dollars
        val priceInUSD = priceInUSDCents / 100.0

        val usdConversionRate = currentUserLocaleConstant?.usdConversionRate ?: 1.0

        val priceInLocalCurrency = priceInUSD * usdConversionRate

        // Create a NumberFormat instance for the specified locale
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        // Format the price in the local currency
        val result = currencyFormat.format(priceInLocalCurrency)
        Log.d("UserAuthViewModel", "Formatted currency: $result")
        return result
    }

    fun formatTimestamp(datetime: Long): String {
        Log.d("UserAuthViewModel", "Formatting timestamp: $datetime")
        // Get the user's locale
        val locale = Locale.forLanguageTag(currentUserLocaleConstant?.code ?: "en-US")

        // Convert timestamp to Date object
        val date = Date(datetime)

        // Create a DateFormatter instance for the specified locale and time zone
        val dateFormatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)

        // Create a TimeFormatter instance for the specified locale and time zone
        val timeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale)

        // Format the date and time in the specified time zone
        val formattedDate = dateFormatter.format(date)
        val formattedTime = timeFormatter.format(date)

        // Combine formatted date and time
        val result = "$formattedDate $formattedTime"
        Log.d("UserAuthViewModel", "Formatted timestamp: $result")
        return result
    }

    fun signUp(email: String, password: String, username: String, localeConstant: LocaleConstant, navigateToHomeScreen: () -> Unit, showSnackbarMessage: (String) -> Unit, showDialog: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update user profile in Firebase Auth
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                    )
                        ?.addOnCompleteListener { profileUpdateTask ->
                            if (profileUpdateTask.isSuccessful) {
                                // Add user details to Firestore
                                val user = hashMapOf(
                                    "localeConstantCode" to localeConstant.code
                                    // TODO: Add more user details as needed
                                )

                                db.collection("userProfiles")
                                    .document(auth.currentUser?.uid ?: "")
                                    .set(user)
                                    .addOnSuccessListener {
                                        _userLiveData.value = auth.currentUser
                                        _isUserLoggedIn.value = true
                                        navigateToHomeScreen()
                                        showSnackbarMessage("Welcome, $username!")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.d("UserAuthViewModel", "Error writing document, message: ${e.message}")
                                        showDialog(e.message ?: "Sign up failed")
                                    }
                            } else {
                                showDialog(profileUpdateTask.exception?.message ?: "Sign up failed")
                            }
                        }
                } else {
                    showDialog(task.exception?.message ?: "Sign up failed")
                }
            }
    }

    fun signIn(email: String, password: String, navigateToHomeScreen: () -> Unit, showSnackbarMessage: (String) -> Unit, showDialog: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userLiveData.value = auth.currentUser
                    _isUserLoggedIn.value = true
                    navigateToHomeScreen()
                    showSnackbarMessage("Welcome, ${auth.currentUser?.displayName}!")
                } else {
                    showDialog(task.exception?.message ?: "Sign in failed")
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun signOut(navigateToLoginScreen: () -> Unit) {
        auth.signOut()
        _userLiveData.value = null
        _isUserLoggedIn.value = false
        navigateToLoginScreen()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserAuthViewModel()
            }
        }
    }
}
