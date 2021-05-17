package com.studieresan.studs.data

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import com.studieresan.studs.data.models.ForgotPasswordRequest
import com.studieresan.studs.data.models.LoginResponse
import com.studieresan.studs.data.models.LoginUserRequest

interface StudsService {

    @POST("login")
    fun login(@Body loginUserRequest: LoginUserRequest): Observable<LoginResponse>

    @POST("forgot")
    fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Observable<ResponseBody>

}
