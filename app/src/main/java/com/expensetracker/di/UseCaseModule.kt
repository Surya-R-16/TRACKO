package com.expensetracker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing use cases
 * 
 * Note: Use cases are provided automatically by Hilt since they have @Inject constructors
 * This module exists for future manual bindings if needed
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Use cases are automatically provided by Hilt through @Inject constructors
    // Add manual bindings here if needed in the future
}