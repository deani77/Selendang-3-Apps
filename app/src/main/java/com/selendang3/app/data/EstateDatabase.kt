package com.selendang3.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Empty by default. Operational data must come from a future authenticated import.
@Entity(tableName = "estate_fields")
data class EstateField(
    @PrimaryKey val code: String,
    val alternateCode: String?,
    val hectares: Double,
    val palms: Int,
    val reportingYear: Int,
    val verified: Boolean = false,
)

@Entity(tableName = "sync_state")
data class SyncState(@PrimaryKey val id: Int = 1, val lastSuccessEpochMillis: Long?)

@Dao
interface EstateDao {
    @Query("SELECT * FROM estate_fields ORDER BY code")
    fun observeFields(): Flow<List<EstateField>>

    @Query("SELECT * FROM sync_state WHERE id = 1")
    fun observeSync(): Flow<SyncState?>
}

@Database(entities = [EstateField::class, SyncState::class], version = 1, exportSchema = true)
abstract class EstateDatabase : RoomDatabase() {
    abstract fun estateDao(): EstateDao

    companion object {
        @Volatile private var instance: EstateDatabase? = null
        fun get(context: Context): EstateDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, EstateDatabase::class.java, "estate.db"
            ).build().also { instance = it }
        }
    }
}
