package com.fernandod.elmapa

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class Mission(var encounterDexId: Int, val reporterName: String, val location: LatLng, val iconUrlString: String, val userAvatar: String, var marker: Marker? = null)