package com.fernandod.elmapa

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
data class Raid(val raidBossId: Int, val reporterName: String, val location: LatLng, val iconUrlString: String, val userAvatar: String, val raidStartTime: Number, val raidEndTime: Number, var marker: Marker? = null)

class Raids {
    companion object {
        val tier5: IntArray = intArrayOf(386, 487)
        val tier4: IntArray = intArrayOf(176, 229, 248, 359, 105)
        val tier3: IntArray = intArrayOf(68, 94, 210, 319)
        val tier2: IntArray = intArrayOf(200, 215, 281, 302, 303)
        val tier1: IntArray = intArrayOf(353, 355, 361, 403, 418)

        fun tierNumberForPokemonId( id: Int) : Int {
            if (tier5.contains(id)) return 5
            if (tier4.contains(id)) return 4
            if (tier3.contains(id)) return 3
            if (tier2.contains(id)) return 2
            if (tier1.contains(id)) return 1

            return -1
        }
    }
}