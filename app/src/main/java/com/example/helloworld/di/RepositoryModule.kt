package com.example.helloworld.di

import com.example.helloworld.data.repository.AuthRepositoryImpl
import com.example.helloworld.data.repository.GalleryRepositoryImpl
import com.example.helloworld.data.repository.MusicRepositoryImpl
import com.example.helloworld.data.repository.NotesRepositoryImpl
import com.example.helloworld.domain.repository.AuthRepository
import com.example.helloworld.domain.repository.GalleryRepository
import com.example.helloworld.domain.repository.MusicRepository
import com.example.helloworld.domain.repository.NotesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindGalleryRepository(
        galleryRepositoryImpl: GalleryRepositoryImpl
    ): GalleryRepository

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository
}
