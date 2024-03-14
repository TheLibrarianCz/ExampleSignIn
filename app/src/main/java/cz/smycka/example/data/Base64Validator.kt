package cz.smycka.example.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Base64Validator @Inject constructor() {

    private val regex by lazy { BASE_64_REGEX_STRING.toRegex() }

    fun validate(base64: String): Boolean = base64.isNotEmpty() && regex.containsMatchIn(base64)

    companion object {
        private const val BASE_64_REGEX_STRING = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?\$"
    }
}
