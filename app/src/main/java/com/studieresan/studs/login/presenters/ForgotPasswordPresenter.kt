package com.studieresan.studs.login.presenters

import android.util.Patterns
import com.studieresan.studs.login.contracts.ForgotPasswordContract
import com.studieresan.studs.net.StudsRepository
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ForgotPasswordPresenter(
        private val view: ForgotPasswordContract.View,
        private val studsRepository: StudsRepository
) : ForgotPasswordContract.Presenter {
    private var disposable: Disposable? = null

    override fun forgotEmail(email: String) {
        when {
            email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() -> resetPassword(email)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> view.showInvalidEmailError()
            else -> view.showInvalidEmailError()
        }
    }

    private fun resetPassword(email: String) {
        disposable?.dispose()
        disposable = studsRepository.forgotPassword(email)
                .subscribeOn(Schedulers.io())
                .observeOn(view.mainScheduler)
                .subscribe({
                    view.showEmailSentVerification()
                }, { view.showGenericErrorMessage() })
    }

    override fun onCleanup() {
        disposable?.dispose()
        disposable = null
    }
}
