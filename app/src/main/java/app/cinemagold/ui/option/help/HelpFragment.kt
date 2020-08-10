package app.cinemagold.ui.option.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.cinemagold.R
import kotlinx.android.synthetic.main.fragment_help.view.*


class HelpFragment : Fragment() {
    val message = "Me gustaria obtener ayuda con mi cuenta de CinemaGOLD.";
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_help, container, false)
        rootView.help_ian.setOnClickListener {
            redirectWhatsapp("593984915871")
        }
        rootView.help_ryan.setOnClickListener {
            redirectWhatsapp("593968894091")
        }
        rootView.help_mateo.setOnClickListener {
            redirectWhatsapp("593969053908")
        }

        rootView.help_messenger_website.setOnClickListener {
            redirectMessenger("CinemaGoldEntretenimiento")
        }
        return rootView
    }

    private fun redirectWhatsapp(phone : String){
        val uri = Uri.parse("https://wa.me/$phone?text=$message")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i)
    }

    private fun redirectMessenger(fbUserId : String){
        val uri = Uri.parse("http://m.me/$fbUserId")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i)
    }
}
