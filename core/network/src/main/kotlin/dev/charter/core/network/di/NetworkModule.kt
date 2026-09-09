package dev.charter.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.charter.core.network.api.BangumiApi
import dev.charter.core.network.api.GitHubApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/** Per-service clients: each API gets its own headers, one shared logging setup. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GitHubClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BangumiClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    @GitHubClient
    fun provideGitHubOkHttp(): OkHttpClient =
        baseClient()
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .build(),
                )
            }.build()

    @Provides
    @Singleton
    @BangumiClient
    fun provideBangumiOkHttp(): OkHttpClient =
        baseClient()
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", BangumiApi.USER_AGENT)
                        .build(),
                )
            }.build()

    @Provides
    @Singleton
    fun provideGitHubApi(
        json: Json,
        @GitHubClient client: OkHttpClient,
    ): GitHubApi = retrofit(json, client, GitHubApi.BASE_URL).create(GitHubApi::class.java)

    @Provides
    @Singleton
    fun provideBangumiApi(
        json: Json,
        @BangumiClient client: OkHttpClient,
    ): BangumiApi = retrofit(json, client, BangumiApi.BASE_URL).create(BangumiApi::class.java)

    private fun baseClient(): OkHttpClient.Builder =
        OkHttpClient
            .Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            )

    private fun retrofit(
        json: Json,
        client: OkHttpClient,
        baseUrl: String,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
