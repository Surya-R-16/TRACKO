package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.usecase.base.UseCase
import com.expensetracker.domain.util.TransactionValidator
import javax.inject.Inject

/**
 * Use case for managing transactions (add, update, delete)
 */
class ManageTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) : UseCase<ManageTransactionUseCase.Params, ManageTransactionResult>() {
    
    override suspend fun execute(parameters: Params): ManageTransactionResult {
        return try {
            when (parameters.operation) {
                TransactionOperation.ADD -> {
                    val transaction = parameters.transaction!!
                    val validationResult = TransactionValidator.validateTransaction(transaction)
                    
                    if (!validationResult.isValid) {
                        return ManageTransactionResult.ValidationError(validationResult.errors)
                    }
                    
                    val id = transactionRepository.insertTransaction(transaction)
                    ManageTransactionResult.Success(
                        operation = parameters.operation,
                        transactionId = id,
                        message = "Transaction added successfully"
                    )
                }
                
                TransactionOperation.ADD_MULTIPLE -> {
                    val transactions = parameters.transactions!!
                    val validTransactions = transactions.filter { transaction ->
                        TransactionValidator.validateTransaction(transaction).isValid
                    }
                    
                    if (validTransactions.isEmpty()) {
                        return ManageTransactionResult.ValidationError(listOf("No valid transactions to add"))
                    }
                    
                    val ids = transactionRepository.insertTransactions(validTransactions)
                    ManageTransactionResult.Success(
                        operation = parameters.operation,
                        transactionIds = ids,
                        message = "${ids.size} transactions added successfully"
                    )
                }
                
                TransactionOperation.UPDATE -> {
                    val transaction = parameters.transaction!!
                    val validationResult = TransactionValidator.validateTransaction(transaction)
                    
                    if (!validationResult.isValid) {
                        return ManageTransactionResult.ValidationError(validationResult.errors)
                    }
                    
                    transactionRepository.updateTransaction(transaction)
                    ManageTransactionResult.Success(
                        operation = parameters.operation,
                        transactionId = transaction.id,
                        message = "Transaction updated successfully"
                    )
                }
                
                TransactionOperation.DELETE -> {
                    val transaction = parameters.transaction
                    val transactionId = parameters.transactionId
                    
                    when {
                        transaction != null -> {
                            transactionRepository.deleteTransaction(transaction)
                            ManageTransactionResult.Success(
                                operation = parameters.operation,
                                transactionId = transaction.id,
                                message = "Transaction deleted successfully"
                            )
                        }
                        transactionId != null -> {
                            transactionRepository.deleteTransactionById(transactionId)
                            ManageTransactionResult.Success(
                                operation = parameters.operation,
                                transactionId = transactionId,
                                message = "Transaction deleted successfully"
                            )
                        }
                        else -> {
                            ManageTransactionResult.Error("Transaction or transaction ID is required for deletion")
                        }
                    }
                }
                
                TransactionOperation.DELETE_ALL -> {
                    transactionRepository.deleteAllTransactions()
                    ManageTransactionResult.Success(
                        operation = parameters.operation,
                        message = "All transactions deleted successfully"
                    )
                }
                
                TransactionOperation.GET_BY_ID -> {
                    val transactionId = parameters.transactionId!!
                    val transaction = transactionRepository.getTransactionById(transactionId)
                    
                    if (transaction != null) {
                        ManageTransactionResult.TransactionRetrieved(transaction)
                    } else {
                        ManageTransactionResult.Error("Transaction not found")
                    }
                }
                
                TransactionOperation.CHECK_DUPLICATE -> {
                    val transaction = parameters.transaction!!
                    val isDuplicate = transactionRepository.isDuplicateTransaction(transaction)
                    ManageTransactionResult.DuplicateCheckResult(isDuplicate)
                }
            }
        } catch (e: Exception) {
            ManageTransactionResult.Error(e.message ?: "Failed to manage transaction")
        }
    }
    
    data class Params(
        val operation: TransactionOperation,
        val transaction: Transaction? = null,
        val transactions: List<Transaction>? = null,
        val transactionId: Long? = null
    ) {
        companion object {
            fun add(transaction: Transaction) = Params(
                operation = TransactionOperation.ADD,
                transaction = transaction
            )
            
            fun addMultiple(transactions: List<Transaction>) = Params(
                operation = TransactionOperation.ADD_MULTIPLE,
                transactions = transactions
            )
            
            fun update(transaction: Transaction) = Params(
                operation = TransactionOperation.UPDATE,
                transaction = transaction
            )
            
            fun delete(transaction: Transaction) = Params(
                operation = TransactionOperation.DELETE,
                transaction = transaction
            )
            
            fun deleteById(transactionId: Long) = Params(
                operation = TransactionOperation.DELETE,
                transactionId = transactionId
            )
            
            fun deleteAll() = Params(operation = TransactionOperation.DELETE_ALL)
            
            fun getById(transactionId: Long) = Params(
                operation = TransactionOperation.GET_BY_ID,
                transactionId = transactionId
            )
            
            fun checkDuplicate(transaction: Transaction) = Params(
                operation = TransactionOperation.CHECK_DUPLICATE,
                transaction = transaction
            )
        }
    }
    
    enum class TransactionOperation {
        ADD,
        ADD_MULTIPLE,
        UPDATE,
        DELETE,
        DELETE_ALL,
        GET_BY_ID,
        CHECK_DUPLICATE
    }
}

/**
 * Result of transaction management operation
 */
sealed class ManageTransactionResult {
    data class Success(
        val operation: ManageTransactionUseCase.TransactionOperation,
        val transactionId: Long? = null,
        val transactionIds: List<Long>? = null,
        val message: String
    ) : ManageTransactionResult()
    
    data class TransactionRetrieved(val transaction: Transaction) : ManageTransactionResult()
    data class DuplicateCheckResult(val isDuplicate: Boolean) : ManageTransactionResult()
    data class ValidationError(val errors: List<String>) : ManageTransactionResult()
    data class Error(val message: String) : ManageTransactionResult()
    
    val isSuccess: Boolean get() = this is Success || this is TransactionRetrieved || this is DuplicateCheckResult
    val isError: Boolean get() = this is Error || this is ValidationError
}