package cz.smycka.example.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    val image: String
)
