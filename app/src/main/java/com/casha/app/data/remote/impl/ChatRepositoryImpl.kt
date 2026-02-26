package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.IncomeDao
import com.casha.app.data.local.dao.TransactionDao
import com.casha.app.data.local.entity.IncomeEntity
import com.casha.app.data.local.entity.TransactionEntity
import com.casha.app.data.remote.api.ChatApiService
import com.casha.app.data.remote.dto.ChatIncomeDto
import com.casha.app.data.remote.dto.ChatRequestDto
import com.casha.app.data.remote.dto.ChatTransactionDto
import com.casha.app.domain.model.ChatParseIntent
import com.casha.app.domain.model.ChatParseResult
import com.casha.app.domain.repository.ChatRepository
import com.casha.app.core.network.safeApiCall
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
    private val transactionDao: TransactionDao,
    private val incomeDao: IncomeDao,
    private val json: Json
) : ChatRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun parseChat(input: String): ChatParseResult {
        val result = safeApiCall { apiService.parseChat(ChatRequestDto(input = input)) }
        return result.fold(
            onSuccess = { response -> processChatResponse(response) },
            onFailure = { throw it }
        )
    }

    override suspend fun parseImage(file: File): ChatParseResult {
        // Many backends (like multer) reject wildcard mime types. Use explicit image/jpeg.
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val inputBody = okhttp3.RequestBody.Companion.create("text/plain".toMediaTypeOrNull(), "")
        
        val result = safeApiCall { apiService.parseImage(body, inputBody) }
        return result.fold(
            onSuccess = { response -> processChatResponse(response) },
            onFailure = { throw it }
        )
    }

    private suspend fun processChatResponse(response: com.casha.app.data.remote.dto.ChatResponseDto): ChatParseResult {
        val parseData = response.data ?: throw Exception("No data returned from API: ${response.message}")
        
        val intentString = parseData.intent
        val message = response.message
        
        return when (intentString) {
            "EXPENSE", "PAYMENT" -> {
                val transactionDto = json.decodeFromJsonElement<ChatTransactionDto>(parseData.data)
                
                val entity = TransactionEntity(
                    id = transactionDto.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                    name = transactionDto.name,
                    category = transactionDto.category.ifEmpty { "Other" },
                    amount = transactionDto.amount,
                    datetime = try { dateFormat.parse(transactionDto.datetime) ?: Date() } catch (e: Exception) { Date() },
                    note = transactionDto.note,
                    isSynced = true,
                    remoteId = transactionDto.id,
                    createdAt = try { transactionDto.createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                    updatedAt = try { transactionDto.updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
                )
                transactionDao.insertTransaction(entity)
                
                ChatParseResult(
                    intent = if (intentString == "EXPENSE") ChatParseIntent.EXPENSE else ChatParseIntent.PAYMENT,
                    message = message
                )
            }
            "INCOME" -> {
                val incomeDto = json.decodeFromJsonElement<ChatIncomeDto>(parseData.data)
                
                val typeResolved = try {
                    com.casha.app.domain.model.IncomeType.valueOf(incomeDto.type ?: "OTHER")
                } catch(e: Exception) {
                    com.casha.app.domain.model.IncomeType.OTHER
                }
                
                val frequencyResolved = try {
                    incomeDto.frequency?.let { com.casha.app.domain.model.IncomeFrequency.valueOf(it) }
                } catch(e: Exception) {
                    null
                }

                val entity = IncomeEntity(
                    id = incomeDto.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                    name = incomeDto.name,
                    amount = incomeDto.amount,
                    datetime = try { dateFormat.parse(incomeDto.datetime) ?: Date() } catch (e: Exception) { Date() },
                    type = typeResolved,
                    source = incomeDto.source,
                    assetId = incomeDto.assetId,
                    isRecurring = incomeDto.isRecurring,
                    frequency = frequencyResolved,
                    note = incomeDto.note,
                    createdAt = try { incomeDto.createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                    updatedAt = try { incomeDto.updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
                )
                incomeDao.insertIncome(entity)
                
                ChatParseResult(
                    intent = ChatParseIntent.INCOME,
                    message = message
                )
            }
            else -> {
                throw Exception("Unknown intent: $intentString")
            }
        }
    }
}
