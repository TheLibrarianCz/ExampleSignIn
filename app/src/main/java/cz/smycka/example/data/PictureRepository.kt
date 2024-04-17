package cz.smycka.example.data

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PictureRepository @Inject constructor(
    private val base64Validator: Base64Validator,
    @Named("fake") private val pictureDataSource: PictureDataSource
) {
    private val pictureMap: MutableMap<String, String> = mutableMapOf()

    /**
     * Fetches and stores the image.
     */
    suspend fun fetchImage(userName: String, password: String): LoadResult<Unit> {
        return pictureDataSource.loadImage(userName, password).let {
            when (it) {
                LoadResult.Failed -> LoadResult.Failed
                is LoadResult.Success -> {
                    pictureMap[userName] = it.data
                    LoadResult.Success(Unit)
                }

                LoadResult.Unauthorized -> LoadResult.Unauthorized
            }
        }
    }

    /**
     * Fetches image for the [userName].
     *
     * @return image, null if missing or invalid format.
     */
    fun getImage(userName: String): String? = pictureMap[userName]?.let { picture ->
        if (base64Validator.validate(picture)) {
            picture
        } else {
            null
        }
    }
}
