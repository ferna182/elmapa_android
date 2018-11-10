package com.fernandod.elmapa

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.fernandod.elmapa.R.id.async

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.longToast
import android.os.AsyncTask
import com.google.android.gms.maps.model.Marker
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.drawable.BitmapDrawable





class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var missionList: ArrayList<Mission>
    private lateinit var missionMarkers: ArrayList<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchMapData()

    }

    private fun fetchMapData() {
        missionList = ArrayList<Mission>()
        missionMarkers = ArrayList<Marker>()
        AsyncTaskHandleJson().execute("https://elmapa.com.ar/questnomap.php")
    }

    inner class AsyncTaskHandleJson : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {
            var text: String
            var connection = URL(url[0]).openConnection() as HttpURLConnection
            try {
                connection.connect()
                text = connection.inputStream.use { it.reader().use{reader -> reader.readText()} }
            } finally {
                connection.disconnect()
            }

            return text
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            handleJson(result)
        }
    }

    private fun handleJson(jsonString: String?) {
        val jsonArray = JSONArray(jsonString)

        missionList.clear()
        for (i in 0..(jsonArray.length() - 1)) {
            val item = jsonArray.getJSONObject(i)
            val location = LatLng(item.getDouble("x"), item.getDouble("y"))
            missionList.add(Mission(
                    item.getInt("i"),
                    item.getString("organizador"),
                    location,
                    "https://www.elmapa.com.ar/assets/pokes/${item.getInt("i")}_shuffle.png",
                    item.getString("picture")
            ))
        }

        refreshMap()
    }

    private fun refreshMap() {
        missionMarkers.forEach {marker ->
            marker.remove()
        }
        missionMarkers.clear()

        missionList.forEach {mission ->
            var markerOption = MarkerOptions()
            markerOption.position(mission.location)
            markerOption.title(mission.reporterName)

            try {
                val bmImg = Ion.with(applicationContext)
                        .load(mission.iconUrlString).asBitmap().get()
                val height = 100
                val width = 100

                val smallMarker = Bitmap.createScaledBitmap(bmImg, width, height, false)

                markerOption.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                missionMarkers.add(mMap.addMarker(markerOption))

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

    }
}
