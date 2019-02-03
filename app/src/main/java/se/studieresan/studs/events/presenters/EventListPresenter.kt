package se.studieresan.studs.events.presenters

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.studieresan.studs.data.StudsService
import se.studieresan.studs.events.contracts.EventListContract

class EventListPresenter(
    private val view: EventListContract.View,
    private val studsService: StudsService
) : EventListContract.Presenter {

    private var disposable: Disposable? = null

    override fun loadEvents() {
        view.showEventListLoading()

        disposable?.dispose()
        disposable = studsService.getEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                events -> view.showEventList(ArrayList(events.data.allEvents))
            }, {
                err -> view.showEventListLoadingError(err)
            })
    }

    override fun onCleanup() {
        disposable?.dispose()
        disposable = null
    }
}