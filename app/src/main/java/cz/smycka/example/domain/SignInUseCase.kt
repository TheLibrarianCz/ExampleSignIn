package cz.smycka.example.domain

import cz.smycka.example.data.LoadResult
import cz.smycka.example.data.PictureRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val pictureRepository: PictureRepository
) {

    suspend operator fun invoke(userName: String, password: String): SignInResult {
        return when (pictureRepository.fetchImage(userName, password)) {
            LoadResult.Failed -> SignInResult.Error
            is LoadResult.Success -> SignInResult.Success
            LoadResult.Unauthorized -> SignInResult.Unauthorized
        }
    }
}

sealed interface SignInResult {
    data object Success : SignInResult
    data object Unauthorized : SignInResult
    data object Error : SignInResult
}
