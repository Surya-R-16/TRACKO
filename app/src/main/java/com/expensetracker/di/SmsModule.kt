package com.expensetracker.di

import com.expensetracker.data.sms.SmsContentProvider
import com.expensetracker.data.sms.SmsContentProviderImpl
import com.expensetracker.data.sms.SmsParserService
import com.expensetracker.data.sms.SmsParserServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsModule {
    
    @Binds
    @Singleton
    abstract fun bindSmsContentProvider(
        smsContentProviderImpl: SmsContentProviderImpl
    ): SmsContentProvider
    
    @Binds
    @Singleton
    abstract fun bindSmsParserService(
        smsParserServiceImpl: SmsParserServiceImpl
    ): SmsParserService
}