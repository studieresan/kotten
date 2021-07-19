package com.studieresan.studs.login.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import com.studieresan.studs.MainActivity
import com.studieresan.studs.R
import com.studieresan.studs.StudsActivity
import com.studieresan.studs.StudsApplication
import com.studieresan.studs.data.StudsPreferences
import com.studieresan.studs.data.models.LoginResponse
import com.studieresan.studs.login.contracts.LoginContract
import com.studieresan.studs.login.presenters.LoginPresenter
import com.studieresan.studs.net.StudsRepository
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : StudsActivity(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter

    @Inject
    lateinit var studsRepository: StudsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StudsApplication.applicationComponent.inject(this)
        setContentView(R.layout.activity_login)
        setupClickListeners()
        presenter = LoginPresenter(this, studsRepository)

        val currentEmail = StudsPreferences.getEmail(this)
        if (currentEmail.isNotEmpty()) {
            et_email.setText(currentEmail)
        }
    }

    private fun setupClickListeners() {
        btn_login.setOnClickListener { login() }
        btn_forgot_password.setOnClickListener { showForgotPassword() }
    }

    private fun showForgotPassword() = startActivity(ForgotPasswordActivity.makeIntent(this, et_email.text.toString()))

    private fun login() = presenter.onLoginClicked(et_email.text.toString(), et_password.text.toString())

    override fun showEmailErrorMessage(show: Boolean) {
        til_email.error = if (show) getString(R.string.invalid_email) else null
    }

    override fun showPasswordErrorMessage(show: Boolean) {
        til_password.error = if (show) getString(R.string.invalid_password) else null
    }

    override fun showLoginFailedMessage() {
        btn_login.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
        Snackbar.make(view, getString(R.string.invalid_email_or_password), Snackbar.LENGTH_SHORT).show()
    }

    override fun loginSuccessful(loginResponse: LoginResponse) {
        StudsPreferences.run {
            logIn(this@LoginActivity, et_email.text.toString())
            setID(this@LoginActivity, loginResponse.id)
            setName(this@LoginActivity, loginResponse.name)
            setPicture(this@LoginActivity, loginResponse.picture)
            setPosition(this@LoginActivity, loginResponse.role)
            setPermissions(this@LoginActivity, loginResponse.permissions)
        }
        startActivity(MainActivity.makeIntent(this, true))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onCleanup()
    }

    companion object {
        fun makeIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
