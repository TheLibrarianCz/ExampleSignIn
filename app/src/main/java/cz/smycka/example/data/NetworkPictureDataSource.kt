package cz.smycka.example.data

import cz.smycka.example.network.ApiResult
import cz.smycka.example.network.ExampleApi
import javax.inject.Inject

class NetworkPictureDataSource @Inject constructor(
    private val exampleApi: ExampleApi
) : PictureDataSource {

    override suspend fun loadImage(userName: String, password: String): LoadResult<String> {
        return exampleApi.downloadImage(userName, password).let {
            when (it) {
                is ApiResult.Error -> mapErrorStatusCodes(it.statusCode)
                is ApiResult.Success -> LoadResult.Success(it.data)
            }
        }
    }

    private fun <T> mapErrorStatusCodes(statusCode: Int): LoadResult<T> =
        if (statusCode == 401) {
            LoadResult.Unauthorized
        } else {
            LoadResult.Failed
        }
}
