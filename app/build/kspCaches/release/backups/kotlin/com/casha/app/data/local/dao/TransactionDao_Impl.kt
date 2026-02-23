package com.casha.app.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.casha.app.`data`.local.database.Converters
import com.casha.app.`data`.local.entity.TransactionEntity
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
public class TransactionDao_Impl(
  __db: RoomDatabase,
) : TransactionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTransactionEntity: EntityInsertAdapter<TransactionEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfTransactionEntity: EntityDeleteOrUpdateAdapter<TransactionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTransactionEntity = object : EntityInsertAdapter<TransactionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `transactions` (`id`,`name`,`category`,`amount`,`datetime`,`note`,`isSynced`,`remoteId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.category)
        statement.bindDouble(4, entity.amount)
        val _tmp: Long? = __converters.dateToTimestamp(entity.datetime)
        if (_tmp == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmp)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpNote)
        }
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpRemoteId: String? = entity.remoteId
        if (_tmpRemoteId == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpRemoteId)
        }
        val _tmp_2: Long? = __converters.dateToTimestamp(entity.createdAt)
        if (_tmp_2 == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmp_2)
        }
        val _tmp_3: Long? = __converters.dateToTimestamp(entity.updatedAt)
        if (_tmp_3 == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmp_3)
        }
      }
    }
    this.__deleteAdapterOfTransactionEntity = object :
        EntityDeleteOrUpdateAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `transactions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindText(1, entity.id)
      }
    }
  }

  public override suspend fun insertTransaction(transaction: TransactionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTransactionEntity.insert(_connection, transaction)
  }

  public override suspend fun insertTransactions(transactions: List<TransactionEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTransactionEntity.insert(_connection, transactions)
  }

  public override suspend fun deleteTransaction(transaction: TransactionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTransactionEntity.handle(_connection, transaction)
  }

  public override fun getAllTransactions(): Flow<List<TransactionEntity>> {
    val _sql: String = "SELECT * FROM transactions ORDER BY datetime DESC"
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _item =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedTransactions(): List<TransactionEntity> {
    val _sql: String = "SELECT * FROM transactions WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _item =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTransactionById(id: String): TransactionEntity? {
    val _sql: String = "SELECT * FROM transactions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: TransactionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _result =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTransactionsSince(startDate: Long): List<TransactionEntity> {
    val _sql: String = "SELECT * FROM transactions WHERE datetime >= ? ORDER BY datetime ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startDate)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _item =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTransactionsBetween(startDate: Long, endDate: Long):
      List<TransactionEntity> {
    val _sql: String =
        "SELECT * FROM transactions WHERE datetime >= ? AND datetime <= ? ORDER BY datetime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endDate)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _item =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTransactionsByCategoryBetween(
    category: String,
    startDate: Long,
    endDate: Long,
  ): List<TransactionEntity> {
    val _sql: String =
        "SELECT * FROM transactions WHERE category = ? AND datetime >= ? AND datetime <= ? ORDER BY datetime DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        _argIndex = 2
        _stmt.bindLong(_argIndex, startDate)
        _argIndex = 3
        _stmt.bindLong(_argIndex, endDate)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfDatetime: Int = getColumnIndexOrThrow(_stmt, "datetime")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          val _tmpRemoteId: String?
          if (_stmt.isNull(_columnIndexOfRemoteId)) {
            _tmpRemoteId = null
          } else {
            _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          }
          val _tmpCreatedAt: Date
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_4: Date? = __converters.fromTimestamp(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_4
          }
          val _tmpUpdatedAt: Date
          val _tmp_5: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_5 = null
          } else {
            _tmp_5 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_6: Date? = __converters.fromTimestamp(_tmp_5)
          if (_tmp_6 == null) {
            error("Expected NON-NULL 'java.util.Date', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_6
          }
          _item =
              TransactionEntity(_tmpId,_tmpName,_tmpCategory,_tmpAmount,_tmpDatetime,_tmpNote,_tmpIsSynced,_tmpRemoteId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalSpendingSince(startDate: Long): Double {
    val _sql: String = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE datetime >= ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startDate)
        val _result: Double
        if (_stmt.step()) {
          val _tmp: Double
          _tmp = _stmt.getDouble(0)
          _result = _tmp
        } else {
          _result = 0.0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCategorySpendingBetween(startDate: Long, endDate: Long):
      List<CategoryTotal> {
    val _sql: String =
        "SELECT category, SUM(amount) as total FROM transactions WHERE datetime >= ? AND datetime <= ? GROUP BY category ORDER BY total DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endDate)
        val _columnIndexOfCategory: Int = 0
        val _columnIndexOfTotal: Int = 1
        val _result: MutableList<CategoryTotal> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryTotal
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpTotal: Double
          _tmpTotal = _stmt.getDouble(_columnIndexOfTotal)
          _item = CategoryTotal(_tmpCategory,_tmpTotal)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM transactions"
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
