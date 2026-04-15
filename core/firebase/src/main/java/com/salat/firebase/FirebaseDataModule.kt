package com.salat.firebase

import android.content.Context
import com.salat.firebase.data.repository.FirebaseRepositoryImpl
import com.salat.firebase.domain.repository.FirebaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseDataModule {

    @Provides
    @Singleton
    fun provideFirebaseRepository(@ApplicationContext context: Context): FirebaseRepository =
        FirebaseRepositoryImpl(context)
}
