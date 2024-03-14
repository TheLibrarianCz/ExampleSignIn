package cz.smycka.example.data

interface PictureDataSource {

    /**
     * Loads the image for the given [userName], authenticated by [password].
     */
    suspend fun loadImage(userName: String, password: String): LoadResult<String>
}

sealed interface LoadResult<out T> {
    data class Success<T>(val data: T) : LoadResult<T>
    data object Unauthorized : LoadResult<Nothing>
    data object Failed : LoadResult<Nothing>
}
