package com.expenseit.feature.splitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.expenseit.core.model.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Insert
    suspend fun insert(friend: FriendEntity)

    @Update
    suspend fun update(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM friends ORDER BY name ASC")
    fun getAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends ORDER BY name ASC")
    suspend fun getAllOnce(): List<FriendEntity>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getById(id: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<FriendEntity>

    @Query("SELECT COUNT(*) FROM friends")
    suspend fun getCount(): Int
}
