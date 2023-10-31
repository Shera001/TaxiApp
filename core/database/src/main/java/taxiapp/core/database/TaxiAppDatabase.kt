package taxiapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import taxiapp.core.database.dao.LocationDao
import taxiapp.core.database.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaxiAppDatabase : RoomDatabase(){

    abstract fun locationDao(): LocationDao
}