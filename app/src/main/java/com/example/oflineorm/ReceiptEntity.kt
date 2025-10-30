// In MainActivity.kt, replace the old data class with this:

// ----------------------------------------------------------------------
// MARK: - Room Data Model (Receipt Entity)
// ----------------------------------------------------------------------
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sourceAppId: String,
    val transactionAmount: Double, // Amount is now non-nullable as extraction is reliable
    val transactionTime: String, // Time is now non-nullable
    val rawText: String,
    val dateAdded: Long = System.currentTimeMillis()
)
// ----------------------------------------------------------------------