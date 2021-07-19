package com.studieresan.studs

import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class StudsActivity : AppCompatActivity(), BaseView {

    protected fun addToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        checkNotNull(toolbar) { "Toolbar must not be null" }

        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            homePressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    protected fun homePressed() {
        onBackPressed()
    }

    fun setToolbarTitle(title: String) {
        val toolbar = supportActionBar
        checkNotNull(toolbar) { "Can't set title on a non-existent toolbar" }
        toolbar.title = title
    }

    fun setToolbarTitle(@StringRes titleRes: Int) = setToolbarTitle(getString(titleRes))

    override fun openBrowser(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    val view: View by lazy {
        findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
    }

    override val mainScheduler: Scheduler = AndroidSchedulers.mainThread()

    override val ioScheduler: Scheduler = Schedulers.io()
}
