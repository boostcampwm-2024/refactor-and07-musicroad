package com.squirtles.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.squirtles.data.datasource.remote.FirebaseDataSourceImpl
import com.squirtles.data.repository.FirebaseRepositoryImpl
import com.squirtles.domain.datasource.FirebaseRemoteDataSource
import com.squirtles.domain.repository.FirebaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseRepository(firebaseRemoteDataSource: FirebaseRemoteDataSource): FirebaseRepository =
        FirebaseRepositoryImpl(firebaseRemoteDataSource)

    @Provides
    @Singleton
    fun provideFirebaseRemoteDataSource(db: FirebaseFirestore): FirebaseRemoteDataSource =
        FirebaseDataSourceImpl(db)
}
