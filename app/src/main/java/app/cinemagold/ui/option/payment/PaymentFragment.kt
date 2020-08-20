package app.cinemagold.ui.option.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.cinemagold.R
import kotlinx.android.synthetic.main.fragment_payment.view.*


class PaymentFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_payment, container, false)
        rootView.payment_paypal.setOnClickListener {
            val uri = Uri.parse("https://www.paypal.com/paypalme/imarkeTrading")
            val i = Intent(Intent.ACTION_VIEW, uri)
            startActivity(i)
        }
        rootView.payment_venmo.setOnClickListener {
            val uri = Uri.parse("https://venmo.com/rafapanentertainment")
            val i = Intent(Intent.ACTION_VIEW, uri)
            startActivity(i)
        }
        rootView.payment_cashapp.setOnClickListener {
            val uri = Uri.parse("http://cash.me/\$rafapanentertainment")
            val i = Intent(Intent.ACTION_VIEW, uri)
            startActivity(i)
        }
        return rootView
    }
}
