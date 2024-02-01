package group.two.tripplanningapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val successMessage = MutableLiveData<String?>()
    val errorMessage = MutableLiveData<String?>()

    fun submitFeedback(content: String, rating: Int) {
        val feedback = hashMapOf(
            "content" to content,
            "rating" to rating,
            "timestamp" to Calendar.getInstance().time
        )

        db.collection("feedbacks")
            .add(feedback)
            .addOnSuccessListener { documentReference ->
                successMessage.postValue("Feedback submitted successfully")
            }
            .addOnFailureListener { e ->
                errorMessage.postValue("Error submitting feedback: ${e.message}")
            }
    }
}