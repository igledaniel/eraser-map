package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.pelias.BoundingBox
import com.mapzen.tangram.MapController
import org.mockito.Mockito

public class TestMapzenLocation : MapzenLocation {
    public var connected = false

    override var mapController: MapController? = null

    override fun startLocationUpdates() {
        connected = true
    }

    override fun stopLocationUpdates() {
        connected = false
    }

    override fun getLastLocation(): Location? {
        return Mockito.mock(Location::class.java)
    }

    override fun getLon(): Double {
        return 0.0
    }

    override fun getLat(): Double {
        return 0.0
    }

    override fun getBoundingBox(): BoundingBox {
        return BoundingBox(0.0, 0.0, 0.0, 0.0)
    }
}
