// File: ReceiptDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Insert
    suspend fun insert(receipt: ReceiptEntity)

    @Query("SELECT * FROM receipts ORDER BY dateAdded DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>
}