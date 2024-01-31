package group.two.tripplanningapp.viewModels

import androidx.lifecycle.ViewModel
import group.two.tripplanningapp.data.Trip



class TripsViewModel : ViewModel() {

    companion object {
        var trips = emptyList<Trip>()
    }


}