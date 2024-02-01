package group.two.tripplanningapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class SettingsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val successMessage = MutableLiveData<String?>()
    val errorMessage = MutableLiveData<String?>()
    private val _currentUserLocaleConstantCode = MutableLiveData<String>("en-US")
    val currentUserLocaleConstantCode: LiveData<String> = _currentUserLocaleConstantCode

    init {
        loadUserLocaleConstantCode()
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun loadUserLocaleConstantCode() {
        db.collection("userProfiles")
            .document(getUserId())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    document.getString("localeConstantCode")?.let {
                        _currentUserLocaleConstantCode.postValue(it)
                    }
                } else {
                    errorMessage.postValue("No such document")
                }
            }
            .addOnFailureListener { exception ->
                errorMessage.postValue("Error getting document: ${exception.message}")
            }
    }

    fun updateLocaleConstantCode(newLocaleConstantCode: String) {
        val userId = getUserId()
        db.collection("userProfiles")
            .document(userId)
            .update("localeConstantCode", newLocaleConstantCode)
            .addOnSuccessListener {
                successMessage.postValue("Currency updated successfully")
            }
            .addOnFailureListener { e ->
                errorMessage.postValue("Error updating currency: ${e.message}")
            }
    }

    fun submitFeedback(content: String, rating: Int) {
        val feedback = hashMapOf(
            "content" to content,
            "rating" to rating,
            "timestamp" to Calendar.getInstance().time
        )

        db.collection("feedbacks")
            .add(feedback)
            .addOnSuccessListener {
                successMessage.postValue("Feedback submitted successfully")
            }
            .addOnFailureListener { e ->
                errorMessage.postValue("Error submitting feedback: ${e.message}")
            }
    }
}