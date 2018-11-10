package com.fernandod.elmapa

import com.google.android.gms.maps.model.LatLng

data class Mission(var encounterDexId: Int, val reporterName: String, val location: LatLng, val iconUrlString: String)