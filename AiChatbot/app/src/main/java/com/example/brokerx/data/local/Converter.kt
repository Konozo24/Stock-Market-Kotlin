package com.example.brokerx.data.local

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromDoubleList(value: List<Double>): String = Gson().toJson(value)

    @TypeConverter
    fun toDoubleList(value: String): List<Double> =
        Gson().fromJson(value, object : TypeToken<List<Double>>() {}.type)

    @TypeConverter
    fun fromLongList(value: List<Long>): String = Gson().toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        Gson().fromJson(value, object : TypeToken<List<Long>>() {}.type)
}