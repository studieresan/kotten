package se.studieresan.studs.events.presenters

import se.studieresan.studs.data.Address
import se.studieresan.studs.data.Event
import se.studieresan.studs.events.contracts.EventDetailContract

class EventDetailPresenter(
        view: EventDetailContract.View,
        private val event: Event
) : EventDetailContract.Presenter {
    private var view: EventDetailContract.View? = view

    override fun didPressCompanyLocation() = view!!.openGoogleMapsNavigation(Address(event.location))

    override fun didClickPreEventFormButton() {
        event.getPreEventForm()?.let { form ->
            view!!.openBrowser(form)
        }
    }

    override fun didClickPostEventFormButton() {
        event.getPostEventForm()?.let { form ->
            view!!.openBrowser(form)
        }
    }

    override fun onCleanup() {
        view = null
    }
}