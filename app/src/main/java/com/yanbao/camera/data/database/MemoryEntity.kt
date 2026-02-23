package com.yanbao.camera.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.yanbao.camera.presentation.camera.Param29D

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val mode: String,
    val paramsJson: String, // Param29D 序列化为 JSON
    val filterId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,
    val weather: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromParams29D(
            photoPath: String,
            mode: String,
            params: Param29D,
            filterId: String? = null,
            lat: Double? = null,
            lng: Double? = null,
            address: String? = null,
            weather: String? = null
        ): MemoryEntity {
            return MemoryEntity(
                photoPath = photoPath,
                mode = mode,
                paramsJson = Gson().toJson(params),
                filterId = filterId,
                lat = lat,
                lng = lng,
                address = address,
                weather = weather
            )
        }
    }
}
