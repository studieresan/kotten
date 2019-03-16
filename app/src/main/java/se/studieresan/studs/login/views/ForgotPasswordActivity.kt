package se.studieresan.studs.login.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_forgot_password.*
import se.studieresan.studs.R
import se.studieresan.studs.StudsActivity
import se.studieresan.studs.StudsApplication
import se.studieresan.studs.data.IntentExtra
import se.studieresan.studs.login.contracts.ForgotPasswordContract
import se.studieresan.studs.login.presenters.ForgotPasswordPresenter
import se.studieresan.studs.net.StudsRepository
import javax.inject.Inject


private val MESSAGES = listOf(
        "You should keep better track of your passwords",
        "Don't wear out the forgot password button",
        "Didn't your parents teach you it's rude to harass buttons?"
)

class ForgotPasswordActivity : StudsActivity(), ForgotPasswordContract.View {

    private lateinit var presenter: ForgotPasswordContract.Presenter
    private var toast: Toast? = null

    @Inject
    lateinit var studsRepository: StudsRepository

    companion object {
        fun makeIntent(context: Context, email: String) = Intent(context, ForgotPasswordActivity::class.java).apply {
            putExtra(IntentExtra.EMAIL, email)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StudsApplication.applicationComponent.inject(this)
        setContentView(R.layout.activity_forgot_password)
        addToolbar()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setupClickListeners()

        val email = intent.getStringExtra(IntentExtra.EMAIL)
        et_email.setText(email, TextView.BufferType.EDITABLE)
        presenter = ForgotPasswordPresenter(this, studsRepository)
    }

    private fun setupClickListeners() {
        tv_forgot_password_title.setOnClickListener { showRandomDegradingMessage() }
        btn_reset_password.setOnClickListener { presenter.forgotEmail(et_email.text.toString()) }
    }

    private fun showRandomDegradingMessage() {
        toast?.cancel()
        toast = Toast.makeText(this, MESSAGES.shuffled()[0], Toast.LENGTH_SHORT)
        toast?.show()
    }

    override fun showEmailSentVerification() =
            Snackbar.make(view, getString(R.string.reset_password_message), Snackbar.LENGTH_SHORT).show()

    override fun showGenericErrorMessage() =
            Snackbar.make(view, getString(R.string.generic_error_message), Snackbar.LENGTH_LONG).show()

    override fun showInvalidEmailError() =
            Snackbar.make(view, getString(R.string.enter_valid_email), Snackbar.LENGTH_LONG).show()
}
