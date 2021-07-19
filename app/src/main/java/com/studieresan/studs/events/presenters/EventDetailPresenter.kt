package com.studieresan.studs.events.presenters

import com.studieresan.studs.events.contracts.EventDetailContract
import io.reactivex.disposables.Disposable

class EventDetailPresenter(
        view: EventDetailContract.View,
) : EventDetailContract.Presenter {

    private var view: EventDetailContract.View? = view

    private var disposable: Disposable? = null


    override fun onCleanup() {
        disposable?.dispose().also {
            disposable = null
        }
        view = null
    }
}
