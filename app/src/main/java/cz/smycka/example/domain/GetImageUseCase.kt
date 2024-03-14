package cz.smycka.example.domain

import cz.smycka.example.data.PictureRepository
import javax.inject.Inject

@JvmInline
value class Base64Picture(val data: String)

class GetImageUseCase @Inject constructor(
    private val pictureRepository: PictureRepository
) {

    operator fun invoke(userName: String): Base64Picture? {
        return pictureRepository.getImage(userName)?.let {
            Base64Picture(data = it)
        }
    }
}
