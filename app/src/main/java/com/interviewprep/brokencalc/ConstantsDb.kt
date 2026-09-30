package com.interviewprep.brokencalc

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// a named number the user can reuse, like tax = 0.16
@Entity(tableName = "constants", indices = [Index(value = ["name"], unique = true)])
data class Constant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val value: Float // numbers here are small, float is plenty
)

@Dao
interface ConstantsDao {

    @Query("SELECT * FROM constants ORDER BY id")
    fun getAll(): Flow<List<Constant>>

    @Insert
    suspend fun insert(constant: Constant)

    // no return value so it doesn't need to be suspend
    @Query("DELETE FROM constants WHERE id = :id")
    fun deleteById(id: Long)
}

@Database(entities = [Constant::class], version = 2)
abstract class ConstantsDatabase : RoomDatabase() {
    abstract fun constantsDao(): ConstantsDao
}

@Module
@InstallIn(SingletonComponent::class)
object ConstantsModule {

    @Provides
    fun provideConstantsDatabase(@ApplicationContext context: Context): ConstantsDatabase {
        Log.d("ConstantsDb", "Opening constants database")
        return Room.databaseBuilder(context, ConstantsDatabase::class.java, "constants.db").build()
    }

    @Provides
    fun provideConstantsDao(db: ConstantsDatabase): ConstantsDao = db.constantsDao()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConstantsEntryPoint {
    fun constantsDao(): ConstantsDao
}

object Constants {

    fun dao(): ConstantsDao {
        return EntryPointAccessors.fromApplication(MainActivity.instance.applicationContext, ConstantsEntryPoint::class.java).constantsDao()
    }

    suspend fun add(name: String, value: Float) {
        dao().insert(Constant(name = name, value = value))
    }

    // editing = remove the old one and put the new one in
    suspend fun update(old: Constant, name: String, value: Float) {
        val dao = dao()
        withContext(Dispatchers.IO) {
            dao.deleteById(old.id)
        }
        dao.insert(Constant(name = name, value = value))
    }

    fun delete(constant: Constant) {
        dao().deleteById(constant.id)
    }
}
