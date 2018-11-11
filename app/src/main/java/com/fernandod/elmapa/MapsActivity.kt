package com.fernandod.elmapa

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.koushikdutta.ion.Ion
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import java.math.BigInteger


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    //quests
    private lateinit var missionList: ArrayList<Mission>

    //raids
    private lateinit var raidList: ArrayList<Raid>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun fetchMapData() {
        missionList = ArrayList<Mission>()

        raidList = ArrayList<Raid>()

        fetchQuests()
        fetchRaids()

    }

    private fun fetchQuests() {
        try {
            Ion.with(applicationContext).load("https://elmapa.com.ar/questnomap.php").asJsonArray().setCallback { _, jsonArray ->
                if (jsonArray != null) {
                    missionList.forEach{
                        it.marker?.remove()
                    }
                    missionList.clear()
                    for (i in 0..(jsonArray.size() - 1)) {
                        val item = jsonArray.get(i)
                        val location = LatLng(item.asJsonObject.get("x").asDouble, item.asJsonObject.get("y").asDouble)
                        missionList.add(Mission(
                                item.asJsonObject.get("i").asInt,
                                item.asJsonObject.get("organizador").asString,
                                location,
                                "https://www.elmapa.com.ar/assets/pokes/${item.asJsonObject.get("i").asInt}_shuffle.png",
                                item.asJsonObject.get("picture").asString
                        ))
                    }

                    refreshQuestsMap()
                }
            }
        } catch (e: Throwable) {

        }
    }

    private fun fetchRaids() {
        //fetch raids
        try {
            Ion.with(applicationContext).load("https://elmapa.com.ar/raidnomap.php").asJsonArray() .setCallback { _, jsonArray ->
                if (jsonArray != null) {
                    raidList.forEach{
                        it.marker?.remove()
                    }
                    raidList.clear()
                    for (i in 0..(jsonArray.size() - 1)) {
                        val item = jsonArray.get(i)
                        val location = LatLng(item.asJsonObject.get("x").asDouble, item.asJsonObject.get("y").asDouble)
                        val bossId = item.asJsonObject.get("i").asInt
                        var iconId = if (bossId < 900) bossId else bossId % 900
                        val currentTime = (System.currentTimeMillis()).toBigInteger()
                        val startTime = item.asJsonObject.get("start").asBigInteger
                        val isEgg = bossId > 900 ||  currentTime < startTime
                        if (isEgg && bossId < 900) {
                            iconId = Raids.tierNumberForPokemonId(bossId)
                        }
                        raidList.add(Raid(
                                item.asJsonObject.get("i").asInt,
                                item.asJsonObject.get("organizador").asString,
                                location,
                                "https://www.elmapa.com.ar/assets/raids/${if (isEgg) "e" else ""}$iconId.png",
                                item.asJsonObject.get("picture").asString,
                                startTime,
                                item.asJsonObject.get("final").asDouble
                        ))
                    }
                    refreshRaidsMap()
                }
            }
        } catch (e: Throwable) {
            Log.d("Mapa", "No hay raids")
        }
    }

    private fun refreshQuestsMap() {
        missionList.forEach { mission ->
            try {
                Ion.with(applicationContext)
                        .load(mission.iconUrlString).asBitmap().setCallback { e, result ->
                           if (result != null) {
                               var markerOption = MarkerOptions()
                               markerOption.position(mission.location)
                               markerOption.title(mission.reporterName)
                               val height = 100
                               val width = 100

                               val smallMarker = Bitmap.createScaledBitmap(result, width, height, false)
                               markerOption.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                               markerOption.anchor(0.5f, 0.5f)
                               mission.marker = mMap.addMarker(markerOption)
                           }
                        }

            } catch (e: Throwable) {

            }

        }

    }

    private fun refreshRaidsMap() {
        raidList.forEach { raid ->
            try {
                Ion.with(applicationContext)
                        .load(raid.iconUrlString).asBitmap().setCallback { e, result ->
                            if (e != null) {
                                throw(e)
                            }
                            var markerOption = MarkerOptions()
                            markerOption.position(raid.location)
                            markerOption.title(raid.reporterName)

                            val height = 150
                            val width = 150

                            val smallMarker = Bitmap.createScaledBitmap(result, width, height, false)
                            markerOption.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            markerOption.anchor(0.5f, 0.5f)
                            raid.marker = mMap.addMarker(markerOption)
                        }


            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val center = LatLng(-34.5882, -58.4274)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15.0f))
        fetchMapData()
    }
}
