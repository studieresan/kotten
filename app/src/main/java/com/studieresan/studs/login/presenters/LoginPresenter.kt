package com.studieresan.studs.login.presenters

import com.studieresan.studs.login.contracts.LoginContract
import com.studieresan.studs.net.StudsRepository
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class LoginPresenter(
        private val view: LoginContract.View,
        private val studsRepository: StudsRepository
) : LoginContract.Presenter {

    private var loginDisposable: Disposable? = null

    override fun onLoginClicked(email: String, password: String) {
        val validCredentialFormat = validEmail(email) && validPassword(password)
        view.showEmailErrorMessage(!validEmail(email))
        view.showPasswordErrorMessage(!validPassword(password))

        if (validCredentialFormat) {
            loginDisposable?.dispose()
            loginDisposable = studsRepository
                    .login(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(view.mainScheduler)
                    .subscribe({
                        view.loginSuccessful(it)
                    }, {
                        view.showLoginFailedMessage()
                    })
        }
    }

    // Shouldn't technically use Android here in the presenter, but whatever
    private fun validEmail(email: String): Boolean = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun validPassword(password: String) = password.isNotEmpty()

    override fun onCleanup() {
        loginDisposable?.dispose()
        loginDisposable = null
    }
}
