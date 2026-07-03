package com.expenseit.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expenseit.core.model.ExpenseSplitEntity
import com.expenseit.core.model.FriendEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupExpenseEntity
import com.expenseit.core.model.GroupMemberEntity
import com.expenseit.core.model.SettlementEntity
import com.expenseit.core.model.TransactionEntity
import com.expenseit.feature.splitter.data.FriendDao
import com.expenseit.feature.splitter.data.GroupDao
import com.expenseit.feature.tracker.data.TransactionDao

@Database(
    entities = [
        TransactionEntity::class,
        FriendEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        GroupExpenseEntity::class,
        ExpenseSplitEntity::class,
        SettlementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParooDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: ParooDatabase? = null

        fun getDatabase(context: Context): ParooDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParooDatabase::class.java,
                    "paroo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
