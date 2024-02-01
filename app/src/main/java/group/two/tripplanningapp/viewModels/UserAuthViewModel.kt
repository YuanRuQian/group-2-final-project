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

class UserAuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _userLiveData: MutableLiveData<FirebaseUser?> = MutableLiveData()

    private val _isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    init {
        _userLiveData.value = auth.currentUser
        _isUserLoggedIn.value = auth.currentUser != null
    }

    fun signUp(email: String, password: String, username: String, localeAreaCode: String, navigateToHomeScreen: () -> Unit, showSnackbarMessage: (String) -> Unit, showDialog: (String) -> Unit) {
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
                                    "localeAreaCode" to localeAreaCode,
                                    "userName" to username
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
