package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.IncomeDao
import com.casha.app.data.local.dao.TransactionDao
import com.casha.app.data.local.entity.IncomeEntity
import com.casha.app.data.local.entity.TransactionEntity
import com.casha.app.data.remote.api.ChatApiService
import com.casha.app.data.remote.dto.ChatIncomeDto
import com.casha.app.data.remote.dto.ChatRequestDto
import com.casha.app.data.remote.dto.ChatTransactionDto
import com.casha.app.data.remote.dto.MultiExpenseSummaryDto
import com.casha.app.data.remote.dto.WhatIfDataDto
import com.casha.app.data.remote.dto.BudgetRecommendationDataDto
import com.casha.app.data.remote.dto.FinancialSummaryDataDto
import com.casha.app.data.remote.dto.toDomain
import com.casha.app.domain.model.ChatParseIntent
import com.casha.app.domain.model.ChatParseResult
import com.casha.app.domain.model.MultiExpenseSummary
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.repository.ChatRepository
import com.casha.app.core.network.safeApiCall
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
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
        val message = parseData.message ?: response.message
        
        return when (intentString) {
            "EXPENSE", "PAYMENT" -> {
                val transactionDto = json.decodeFromJsonElement<ChatTransactionDto>(parseData.data)
                
                val entity = TransactionEntity(
                    id = transactionDto.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                    name = transactionDto.name,
                    category = transactionDto.categoryName.ifEmpty { "Other" },
                    amount = transactionDto.amount,
                    datetime = try { dateFormat.parse(transactionDto.datetime) ?: Date() } catch (e: Exception) { Date() },
                    note = transactionDto.note,
                    isSynced = true,
                    remoteId = transactionDto.id,
                    createdAt = try { transactionDto.createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                    updatedAt = try { transactionDto.updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                    groupId = transactionDto.groupId,
                    groupName = transactionDto.groupName
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
                    isSynced = true,
                    remoteId = incomeDto.id,
                    createdAt = try { incomeDto.createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                    updatedAt = try { incomeDto.updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
                )
                incomeDao.insertIncome(entity)
                
                ChatParseResult(
                    intent = ChatParseIntent.INCOME,
                    message = message
                )
            }
            "WHAT_IF" -> {
                // Simulation only — do NOT save to DB, do NOT emit sync event
                val whatIfDto = json.decodeFromJsonElement<WhatIfDataDto>(parseData.data)
                ChatParseResult(
                    intent = ChatParseIntent.WHAT_IF,
                    message = message,
                    whatIfSimulation = whatIfDto.toDomain()
                )
            }
            "FINANCIAL_SUMMARY" -> {
                // Display-only — show AI message + structured card, no DB save
                val summaryDto = try {
                    json.decodeFromJsonElement<FinancialSummaryDataDto>(parseData.data)
                } catch (_: Exception) { null }
                ChatParseResult(
                    intent = ChatParseIntent.FINANCIAL_SUMMARY,
                    message = message,
                    financialSummary = summaryDto?.toDomain()
                )
            }
            "BUDGET_RECOMMENDATION" -> {
                val recDto = json.decodeFromJsonElement<BudgetRecommendationDataDto>(parseData.data)
                ChatParseResult(
                    intent = ChatParseIntent.BUDGET_RECOMMENDATION,
                    message = message,
                    budgetRecommendation = recDto.toDomain()
                )
            }
            "UNKNOWN" -> {
                // For unknown intents, we just return the AI's message
                // so the UI can display it without saving any transaction.
                ChatParseResult(
                    intent = ChatParseIntent.UNKNOWN,
                    message = message
                )
            }
            "MULTI_EXPENSE" -> {
                // data is an array of ChatTransactionDto
                val transactionDtos: List<ChatTransactionDto> = if (parseData.data is JsonArray) {
                    json.decodeFromJsonElement<List<ChatTransactionDto>>(parseData.data)
                } else {
                    // Fallback: single item wrapped
                    listOf(json.decodeFromJsonElement<ChatTransactionDto>(parseData.data))
                }

                val domainExpenses = mutableListOf<TransactionCasha>()

                for (dto in transactionDtos) {
                    val id = dto.id.ifEmpty { java.util.UUID.randomUUID().toString() }
                    val entity = TransactionEntity(
                        id = id,
                        name = dto.name,
                        category = dto.categoryName.ifEmpty { "Other" },
                        amount = dto.amount,
                        datetime = try { dateFormat.parse(dto.datetime) ?: Date() } catch (e: Exception) { Date() },
                        note = dto.note,
                        isSynced = true,
                        remoteId = dto.id,
                        createdAt = try { dto.createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                        updatedAt = try { dto.updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
                        groupId = dto.groupId,
                        groupName = dto.groupName
                    )
                    transactionDao.insertTransaction(entity)

                    domainExpenses.add(
                        TransactionCasha(
                            id = entity.id,
                            name = entity.name,
                            category = entity.category,
                            amount = entity.amount,
                            datetime = entity.datetime,
                            note = entity.note,
                            isSynced = true,
                            remoteId = entity.remoteId,
                            createdAt = entity.createdAt,
                            updatedAt = entity.updatedAt,
                            groupId = entity.groupId,
                            groupName = entity.groupName
                        )
                    )
                }

                val summaryDto = parseData.summary
                val summary = summaryDto?.let {
                    MultiExpenseSummary(
                        groupId = it.groupId,
                        groupName = it.groupName,
                        count = it.count,
                        total = it.total,
                        currency = it.currency
                    )
                }

                ChatParseResult(
                    intent = ChatParseIntent.MULTI_EXPENSE,
                    message = message,
                    expenses = domainExpenses,
                    summary = summary
                )
            }
            else -> {
                throw Exception("Unknown intent: $intentString")
            }
        }
    }
}
