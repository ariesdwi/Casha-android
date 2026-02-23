package com.casha.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.casha.app.`data`.local.database.Converters
import com.casha.app.`data`.local.entity.IncomeEntity
import com.casha.app.domain.model.IncomeFrequency
import com.casha.app.domain.model.IncomeType
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
public class IncomeDao_Impl(
  __db: RoomDatabase,
) : IncomeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfIncomeEntity: EntityInsertAdapter<IncomeEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfIncomeEntity = object : EntityInsertAdapter<IncomeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `incomes` (`id`,`name`,`amount`,`datetime`,`type`,`source`,`assetId`,`isRecurring`,`frequency`,`note`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: IncomeEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.amount)
        val _tmp: Long? = __converters.dateToTimestamp(entity.datetime)
        if (_tmp == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmp)
        }
        val _tmp_1: String? = __converters.incomeTypeToString(entity.type)
        if (_tmp_1 == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmp_1)
        }
        val _tmpSource: String? = entity.source
        if (_tmpSource == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpSource)
        }
        val _tmpAssetId: String? = entity.assetId
        if (_tmpAssetId == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpAssetId)
        }
        val _tmp_2: Int = if (entity.isRecurring) 1 else 0
        statement.bindLong(8, _tmp_2.toLong())
        val _tmpFrequency: IncomeFrequency? = entity.frequency
        val _tmp_3: String? = __converters.incomeFrequencyToString(_tmpFrequency)
        if (_tmp_3 == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmp_3)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpNote)
        }
        val _tmp_4: Long? = __converters.dateToTimestamp(entity.createdAt)
        if (_tmp_4 == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmp_4)
        }
        val _tmp_5: Long? = __converters.dateToTimestamp(entity.updatedAt)
        if (_tmp_5 == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmp_5)
        }
      }
    }
  }

  public override suspend fun insertIncome(income: IncomeEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfIncomeEntity.insert(_connection, income)
  }

  public override suspend fun insertIncomes(incomes: List<IncomeEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfIncomeEntity.insert(_connection, incomes)
  }

  public override fun getAllIncomes(): Flow<List<IncomeEntity>> {
    val _sql: String = "SELECT * FROM incomes ORDER BY datetime DESC"
    return createFlow(__db, false, arrayOf("incomes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfAssetId: Int = getColumnIndexOrThrow(_stmt, "assetId")
        val _columnIndexOfIsRecurring: Int = getColumnIndexOrThrow(_stmt, "isRecurring")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<IncomeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: IncomeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpDatetime: Date
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDatetime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDatetime)
          }
          val _tmp_1: Date? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpDatetime = _tmp_1
          }
          val _tmpType: IncomeType
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfType)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfType)
          }
          val _tmp_3: IncomeType? = __converters.fromIncomeType(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'com.casha.app.domain.model.IncomeType', but it was NULL.")
          } else {
            _tmpType = _tmp_3
          }
          val _tmpSource: String?
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource)
          }
          val _tmpAssetId: String?
          if (_stmt.isNull(_columnIndexOfAssetId)) {
            _tmpAssetId = null
          } else {
            _tmpAssetId = _stmt.getText(_columnIndexOfAssetId)
          }
          val _tmpIsRecurring: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsRecurring).toInt()
          _tmpIsRecurring = _tmp_4 != 0
          val _tmpFrequency: IncomeFrequency?
          val _tmp_5: String?
          if (_stmt.isNull(_columnIndexOfFrequency)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getText(_columnIndexOfFrequency)
          }
          _tmpFrequency = __converters.fromIncomeFrequency(_tmp_5)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpCreatedAt: Date
          val _tmp_6: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_7: Date? = __converters.fromTimestamp(_tmp_6)
          if (_tmp_7 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_7
          }
          val _tmpUpdatedAt: Date
          val _tmp_8: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_8 = null
          } else {
            _tmp_8 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_9: Date? = __converters.fromTimestamp(_tmp_8)
          if (_tmp_9 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_9
          }
          _item =
              IncomeEntity(_tmpId,_tmpName,_tmpAmount,_tmpDatetime,_tmpType,_tmpSource,_tmpAssetId,_tmpIsRecurring,_tmpFrequency,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM incomes WHERE id = ?"
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

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM incomes"
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
