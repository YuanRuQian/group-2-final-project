package group.two.tripplanningapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import group.two.tripplanningapp.data.LocaleConstant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocaleViewModel: ViewModel() {

    private val _auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _db = Firebase.firestore

    private var _localeConstants = MutableStateFlow<List<LocaleConstant>>(emptyList())
    val localeConstants: StateFlow<List<LocaleConstant>> get() = _localeConstants

    private var currentUserLocaleConstant: LocaleConstant? = null

    fun loadLocaleData() {
        val localeConstants = mutableListOf<LocaleConstant>()
        _db.collection("localeConstants").get().addOnSuccessListener { result ->
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
        val currentUserId = _auth.currentUser?.uid ?: ""
        _db.collection("userProfiles").document(currentUserId).get().addOnSuccessListener { documentSnapshot ->
            val currentUserLocaleConstantCode = documentSnapshot.getString("localeConstantCode") ?: "en-US"
            currentUserLocaleConstant = _localeConstants.value.find { it.code == currentUserLocaleConstantCode }
            Log.d("LocaleViewModel", "Current user locale constant code: $currentUserLocaleConstantCode")
            Log.d("LocaleViewModel", "Current user locale constant: $currentUserLocaleConstant")
        }
    }

    fun formatCurrency(priceInUSDCents: Int): String {
        val locale = Locale.forLanguageTag(currentUserLocaleConstant?.code ?: "en-US")

        // Convert USD cents to dollars
        val priceInUSD = priceInUSDCents / 100.0

        val usdConversionRate = currentUserLocaleConstant?.usdConversionRate ?: 1.0

        val priceInLocalCurrency = priceInUSD * usdConversionRate

        // Create a NumberFormat instance for the specified locale
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        // Format the price in the local currency
        return currencyFormat.format(priceInLocalCurrency)
    }

    fun formatTimestamp(datetime: Long): String {
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
        return "$formattedDate $formattedTime"
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LocaleViewModel()
            }
        }
    }
}