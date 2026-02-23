package com.casha.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.casha.app.`data`.local.database.Converters
import com.casha.app.`data`.local.entity.BudgetEntity
import java.util.Date
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BudgetDao_Impl(
  __db: RoomDatabase,
) : BudgetDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBudgetEntity: EntityInsertAdapter<BudgetEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfBudgetEntity = object : EntityInsertAdapter<BudgetEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `budgets` (`id`,`amount`,`spent`,`remaining`,`period`,`startDate`,`endDate`,`category`,`currency`,`isSynced`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BudgetEntity) {
        statement.bindText(1, entity.id)
        statement.bindDouble(2, entity.amount)
        statement.bindDouble(3, entity.spent)
        statement.bindDouble(4, entity.remaining)
        statement.bindText(5, entity.period)
        val _tmp: Long? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp)
        }
        val _tmp_1: Long? = __converters.dateToTimestamp(entity.endDate)
        if (_tmp_1 == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp_1)
        }
        statement.bindText(8, entity.category)
        statement.bindText(9, entity.currency)
        val _tmp_2: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(10, _tmp_2.toLong())
        val _tmp_3: Long? = __converters.dateToTimestamp(entity.createdAt)
        if (_tmp_3 == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmp_3)
        }
        val _tmp_4: Long? = __converters.dateToTimestamp(entity.updatedAt)
        if (_tmp_4 == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmp_4)
        }
      }
    }
  }

  public override suspend fun insertBudget(budget: BudgetEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfBudgetEntity.insert(_connection, budget)
  }

  public override suspend fun insertBudgets(budgets: List<BudgetEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBudgetEntity.insert(_connection, budgets)
  }

  public override fun getAllBudgets(): Flow<List<BudgetEntity>> {
    val _sql: String = "SELECT * FROM budgets ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("budgets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfSpent: Int = getColumnIndexOrThrow(_stmt, "spent")
        val _columnIndexOfRemaining: Int = getColumnIndexOrThrow(_stmt, "remaining")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<BudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BudgetEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpSpent: Double
          _tmpSpent = _stmt.getDouble(_columnIndexOfSpent)
          val _tmpRemaining: Double
          _tmpRemaining = _stmt.getDouble(_columnIndexOfRemaining)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpStartDate: Date
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartDate)
          }
          val _tmp_1: Date? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpEndDate: Date
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndDate)
          }
          val _tmp_3: Date? = __converters.fromTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpEndDate = _tmp_3
          }
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          val _tmpCreatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_6
          }
          val _tmpUpdatedAt: Date
          val _tmp_7: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_7 = null
          } else {
            _tmp_7 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_8: Date? = __converters.fromTimestamp(_tmp_7)
          if (_tmp_8 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_8
          }
          _item =
              BudgetEntity(_tmpId,_tmpAmount,_tmpSpent,_tmpRemaining,_tmpPeriod,_tmpStartDate,_tmpEndDate,_tmpCategory,_tmpCurrency,_tmpIsSynced,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBudgetsByMonth(month: String): List<BudgetEntity> {
    val _sql: String = "SELECT * FROM budgets WHERE period = ? ORDER BY category ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, month)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfSpent: Int = getColumnIndexOrThrow(_stmt, "spent")
        val _columnIndexOfRemaining: Int = getColumnIndexOrThrow(_stmt, "remaining")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<BudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BudgetEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpSpent: Double
          _tmpSpent = _stmt.getDouble(_columnIndexOfSpent)
          val _tmpRemaining: Double
          _tmpRemaining = _stmt.getDouble(_columnIndexOfRemaining)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpStartDate: Date
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartDate)
          }
          val _tmp_1: Date? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpEndDate: Date
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndDate)
          }
          val _tmp_3: Date? = __converters.fromTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpEndDate = _tmp_3
          }
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          val _tmpCreatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_6
          }
          val _tmpUpdatedAt: Date
          val _tmp_7: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_7 = null
          } else {
            _tmp_7 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_8: Date? = __converters.fromTimestamp(_tmp_7)
          if (_tmp_8 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_8
          }
          _item =
              BudgetEntity(_tmpId,_tmpAmount,_tmpSpent,_tmpRemaining,_tmpPeriod,_tmpStartDate,_tmpEndDate,_tmpCategory,_tmpCurrency,_tmpIsSynced,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBudgetById(id: String): BudgetEntity? {
    val _sql: String = "SELECT * FROM budgets WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfSpent: Int = getColumnIndexOrThrow(_stmt, "spent")
        val _columnIndexOfRemaining: Int = getColumnIndexOrThrow(_stmt, "remaining")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: BudgetEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpSpent: Double
          _tmpSpent = _stmt.getDouble(_columnIndexOfSpent)
          val _tmpRemaining: Double
          _tmpRemaining = _stmt.getDouble(_columnIndexOfRemaining)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpStartDate: Date
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartDate)
          }
          val _tmp_1: Date? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpEndDate: Date
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndDate)
          }
          val _tmp_3: Date? = __converters.fromTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpEndDate = _tmp_3
          }
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          val _tmpCreatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_6
          }
          val _tmpUpdatedAt: Date
          val _tmp_7: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_7 = null
          } else {
            _tmp_7 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_8: Date? = __converters.fromTimestamp(_tmp_7)
          if (_tmp_8 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_8
          }
          _result =
              BudgetEntity(_tmpId,_tmpAmount,_tmpSpent,_tmpRemaining,_tmpPeriod,_tmpStartDate,_tmpEndDate,_tmpCategory,_tmpCurrency,_tmpIsSynced,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedBudgets(): List<BudgetEntity> {
    val _sql: String = "SELECT * FROM budgets WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfSpent: Int = getColumnIndexOrThrow(_stmt, "spent")
        val _columnIndexOfRemaining: Int = getColumnIndexOrThrow(_stmt, "remaining")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<BudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BudgetEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpSpent: Double
          _tmpSpent = _stmt.getDouble(_columnIndexOfSpent)
          val _tmpRemaining: Double
          _tmpRemaining = _stmt.getDouble(_columnIndexOfRemaining)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpStartDate: Date
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartDate)
          }
          val _tmp_1: Date? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpEndDate: Date
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndDate)
          }
          val _tmp_3: Date? = __converters.fromTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpEndDate = _tmp_3
          }
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          val _tmpCreatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_6
          }
          val _tmpUpdatedAt: Date
          val _tmp_7: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_7 = null
          } else {
            _tmp_7 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_8: Date? = __converters.fromTimestamp(_tmp_7)
          if (_tmp_8 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_8
          }
          _item =
              BudgetEntity(_tmpId,_tmpAmount,_tmpSpent,_tmpRemaining,_tmpPeriod,_tmpStartDate,_tmpEndDate,_tmpCategory,_tmpCurrency,_tmpIsSynced,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM budgets WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSyncStatus(
    oldId: String,
    newId: String,
    synced: Boolean,
  ) {
    val _sql: String = "UPDATE budgets SET isSynced = ?, id = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (synced) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, newId)
        _argIndex = 3
        _stmt.bindText(_argIndex, oldId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM budgets"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
