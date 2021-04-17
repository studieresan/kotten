package com.studieresan.studs.net

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import com.studieresan.studs.data.StudsService
import com.studieresan.studs.data.models.Events
import com.studieresan.studs.data.models.ForgotPasswordRequest
import com.studieresan.studs.data.models.LoginResponse
import com.studieresan.studs.data.models.LoginUserRequest
import com.studieresan.studs.data.models.User
import javax.inject.Inject

class StudsRepository @Inject constructor(private val studsService: StudsService) {

    fun login(email: String, password: String): Observable<LoginResponse> =
            studsService
                    .login(LoginUserRequest(email, password))
                    .compose(applySchedulers())

    fun getUser(): Observable<User> =
            studsService
                    .getUser()
                    .map { it.data.user }
                    .compose(applySchedulers())

    fun forgotPassword(email: String): Observable<ResponseBody> =
            studsService
                    .forgotPassword(ForgotPasswordRequest(email))
                    .compose(applySchedulers())

    fun getEvents(): Observable<Events> =
            studsService
                    .getEvents()
                    .compose(applySchedulers())

    fun getHappenings(): Observable<Events> =
            studsService
                    .getHappenings()
                    .compose(applySchedulers())

    private fun <T> applySchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
