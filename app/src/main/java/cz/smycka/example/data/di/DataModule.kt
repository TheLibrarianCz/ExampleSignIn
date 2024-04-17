package cz.smycka.example.data.di

import cz.smycka.example.data.FakePictureDataSource
import cz.smycka.example.data.NetworkPictureDataSource
import cz.smycka.example.data.PictureDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @Named("network")
    fun provideDataSource(source: NetworkPictureDataSource): PictureDataSource = source

    @Provides
    @Singleton
    @Named("fake")
    fun provideFakeDataSource(source: FakePictureDataSource): PictureDataSource = source
}
