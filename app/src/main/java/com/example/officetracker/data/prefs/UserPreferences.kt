package com.example.officetracker.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class OfficeLocation(
    val lat: Double,
    val lng: Double,
    val radiusMetres: Double = 50.0,
    val isSet: Boolean = false
)

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val OFFICE_LAT = doublePreferencesKey("office_lat")
        val OFFICE_LNG = doublePreferencesKey("office_lng")
        val IS_LOCATION_SET = booleanPreferencesKey("is_location_set")
        val RADIUS_METRES = doublePreferencesKey("radius_metres")
        val DAILY_GOAL_HOURS = intPreferencesKey("daily_goal_hours")
        val MONTHLY_GOAL_HOURS = intPreferencesKey("monthly_goal_hours")
        val USER_NAME = stringPreferencesKey("user_name")
        val HAS_SEEN_TOUR = booleanPreferencesKey("has_seen_tour")
    }

    val officeLocation: Flow<OfficeLocation> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            OfficeLocation(
                lat = preferences[Keys.OFFICE_LAT] ?: 0.0,
                lng = preferences[Keys.OFFICE_LNG] ?: 0.0,
                radiusMetres = preferences[Keys.RADIUS_METRES] ?: 50.0,
                isSet = preferences[Keys.IS_LOCATION_SET] ?: false
            )
        }

    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.USER_NAME] ?: ""
        }

    val hasSeenTour: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.HAS_SEEN_TOUR] ?: false
        }

    data class UserGoals(
        val dailyGoalHours: Int = 6,
        val monthlyGoalHours: Int = 72
    )

    val userGoals: Flow<UserGoals> = context.dataStore.data
        .map { preferences ->
            UserGoals(
                dailyGoalHours = preferences[Keys.DAILY_GOAL_HOURS] ?: 6,
                monthlyGoalHours = preferences[Keys.MONTHLY_GOAL_HOURS] ?: 72
            )
        }

    suspend fun setOfficeLocation(lat: Double, lng: Double, radius: Double = 50.0, isSet: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[Keys.OFFICE_LAT] = lat
            preferences[Keys.OFFICE_LNG] = lng
            preferences[Keys.RADIUS_METRES] = radius
            preferences[Keys.IS_LOCATION_SET] = isSet
        }
    }

    suspend fun setGoals(dailyHours: Int, monthlyHours: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DAILY_GOAL_HOURS] = dailyHours
            preferences[Keys.MONTHLY_GOAL_HOURS] = monthlyHours
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USER_NAME] = name
        }
    }

    suspend fun setHasSeenTour(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAS_SEEN_TOUR] = hasSeen
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
