package app.cinemagold.ui.option.notification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import app.cinemagold.R
import app.cinemagold.injection.ApplicationContextInjector
import kotlinx.android.synthetic.main.fragment_notification.view.*
import kotlinx.android.synthetic.main.item_notification.view.*
import javax.inject.Inject

class NotificationFragment: Fragment() {
    @Inject
    lateinit var viewModel: NotificationViewModel

    override fun onAttach(context: Context) {
        (this.activity?.application as ApplicationContextInjector).applicationComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Observers
        viewModel.notifications.observe(this){_ ->
            buildNotifications()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_notification, container, false)
        return rootView
    }

    private fun buildNotifications(){
        val notifications = viewModel.notifications.value
        val containerView = requireView().notification_container
        for(notification in notifications!!.iterator()){
            val notificationView = layoutInflater.inflate(R.layout.item_notification, null)
            notificationView.item_notification_text.text = notification.name
            containerView.addView(notificationView)
        }
    }
}