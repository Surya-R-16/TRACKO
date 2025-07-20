package com.expensetracker.domain.usecase.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for use cases that execute business logic
 */
abstract class UseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Executes the use case asynchronously and returns the result
     */
    suspend operator fun invoke(parameters: P): R {
        return withContext(coroutineDispatcher) {
            execute(parameters)
        }
    }
    
    /**
     * Override this to set the code to be executed
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * Base class for use cases that don't require parameters
 */
abstract class NoParameterUseCase<R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Executes the use case asynchronously and returns the result
     */
    suspend operator fun invoke(): R {
        return withContext(coroutineDispatcher) {
            execute()
        }
    }
    
    /**
     * Override this to set the code to be executed
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): R
}

/**
 * Base class for use cases that return Flow
 */
abstract class FlowUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Executes the use case and returns a Flow
     */
    operator fun invoke(parameters: P): kotlinx.coroutines.flow.Flow<R> {
        return execute(parameters)
    }
    
    /**
     * Override this to set the code to be executed
     */
    protected abstract fun execute(parameters: P): kotlinx.coroutines.flow.Flow<R>
}