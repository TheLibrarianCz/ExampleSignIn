package cz.smycka.example.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun providePasswordEncoder(shA1Encoder: SHA1Encoder): Encoder = shA1Encoder
}
