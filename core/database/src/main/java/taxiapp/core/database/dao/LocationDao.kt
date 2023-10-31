package taxiapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import taxiapp.core.database.entity.LocationEntity

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY id DESC LIMIT 1")
    fun getLocations(): Flow<LocationEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(locationEntity: LocationEntity)
}