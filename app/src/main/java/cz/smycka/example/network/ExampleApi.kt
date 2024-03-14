package cz.smycka.example.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import javax.inject.Inject

private const val EXAMPLE_BASE_URL = "https://image.example.com"

class ExampleApi @Inject constructor(
    private val encoder: Encoder
) {

    private val api: ExampleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EXAMPLE_BASE_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ExampleApiService::class.java)
    }

    /**
     * Downloads the image for the given [userName], authenticated by [password].
     */
    suspend fun downloadImage(userName: String, password: String): ApiResult<String> =
        withContext(Dispatchers.IO) {
            val apiCall = try {
                api.getImage(userName, encoder.encode(password))
            } catch (e: SocketTimeoutException) {
                return@withContext ApiResult.Error(408)
            }
            mapResult(apiCall) { imageResponse ->
                imageResponse.image
            }
        }

    private fun <T, V> mapResult(response: Response<T>, map: (T) -> V): ApiResult<V> {
        return if (response.isSuccessful) {
            mapSuccessfulResult(response.body(), response.code(), map)
        } else {
            ApiResult.Error(response.code(), response.errorBody()?.toString())
        }
    }

    private fun <T, V> mapSuccessfulResult(body: T?, code: Int, map: (T) -> V): ApiResult<V> {
        return if (body != null) {
            ApiResult.Success(map(body))
        } else {
            ApiResult.Error(code)
        }
    }
}

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val statusCode: Int, val message: String? = null) : ApiResult<Nothing>
}
