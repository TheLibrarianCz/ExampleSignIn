package cz.smycka.example.network

import cz.smycka.example.network.model.ImageResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface ExampleApiService {

    @FormUrlEncoded
    @POST("/download/bootcamp/image.php")
    suspend fun getImage(
        @Field("username") userName: String,
        @Header("authorization") authorization: String
    ): Response<ImageResponse>
}
