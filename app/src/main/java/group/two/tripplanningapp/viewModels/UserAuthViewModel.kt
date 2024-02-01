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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _userLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()

    private val _isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    private var _localeConstants = MutableStateFlow<List<LocaleConstant>>(emptyList())
    val localeConstants: StateFlow<List<LocaleConstant>> get() = _localeConstants
    private var currentUserLocaleConstant: LocaleConstant? = null

    init {
        _userLiveData.value = auth.currentUser
        _isUserLoggedIn.value = auth.currentUser != null
        loadLocaleData()
        if (auth.currentUser != null) {
            loadCurrentUserLocaleConstantCode()
        }
    }

    fun loadLocaleData() {
        val localeConstants = mutableListOf<LocaleConstant>()
        db.collection("localeConstants").get().addOnSuccessListener { result ->
            for (document in result) {
                val localeConstant = document.toObject(LocaleConstant::class.java)
                localeConstants.add(localeConstant)
            }
            _localeConstants.value = localeConstants
        }.addOnFailureListener { exception ->
            Log.w("LocaleViewModel", "Error getting documents.", exception)
        }
    }

    fun loadCurrentUserLocaleConstantCode() {
        val currentUserId = auth.currentUser?.uid ?: ""
        db.collection("userProfiles").document(currentUserId).get().addOnSuccessListener { documentSnapshot ->
            val currentUserLocaleConstantCode = documentSnapshot.getString("localeConstantCode") ?: "en-US"
            currentUserLocaleConstant = _localeConstants.value.find { it.code == currentUserLocaleConstantCode }
            Log.d("LocaleViewModel", "Current user locale constant code: $currentUserLocaleConstantCode")
            Log.d("LocaleViewModel", "Current user locale constant: $currentUserLocaleConstant")
        }
    }

    fun formatCurrency(priceInUSDCents: Int): String {
        val locale = Locale.forLanguageTag(currentUserLocaleConstant?.code ?: "en-US")
        val priceInUSD = priceInUSDCents / 100.0
        val usdConversionRate = currentUserLocaleConstant?.usdConversionRate ?: 1.0
        val priceInLocalCurrency = priceInUSD * usdConversionRate
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        return currencyFormat.format(priceInLocalCurrency)
    }

    fun formatTimestamp(datetime: Long): String {
        val locale = Locale.forLanguageTag(currentUserLocaleConstant?.code ?: "en-US")
        val date = Date(datetime)
        val dateFormatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)
        val timeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale)
        return "${dateFormatter.format(date)} ${timeFormatter.format(date)}"
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
