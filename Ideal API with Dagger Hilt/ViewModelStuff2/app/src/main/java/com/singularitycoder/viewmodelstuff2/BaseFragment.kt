package com.singularitycoder.viewmodelstuff2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.singularitycoder.viewmodelstuff2.helpers.extensions.*
import com.singularitycoder.viewmodelstuff2.helpers.utils.doAfter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

// This fragment is without a view
// Receive Broadcasts
// Screenshot protection
// Enable double backpress

@ExperimentalCoroutinesApi
@AndroidEntryPoint
open class BaseFragment : Fragment() {

    companion object {
        fun newInstance() = BaseFragment()
    }

    private lateinit var nnContext: Context
    private lateinit var nnActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        nnContext = context
        nnActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun RecyclerView.setUpScrollListener(onScroll: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> }) {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.OnScrollListener
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("dx: $dx, dy: $dy")
                val bottomNav = nnActivity.findViewById<CardView>(R.id.card_bottom_nav)
                if (dy > 0) {
                    bottomNav.gone()
                    activity?.hideKeyboard()
                } else {
                    bottomNav.visible()
                }
                onScroll.invoke(recyclerView, dx, dy)
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    fun TextView.showOfflineStrip() {
        this.apply {
            text = context.getString(R.string.offline).toUpCase()
            visibility = View.VISIBLE
            setBackgroundColor(context.color(android.R.color.holo_red_dark))
            setTextColor(context.color(R.color.white))
        }
    }

    fun TextView.showOnlineStrip() {
        this.apply {
            if (text == context.getString(R.string.online).toUpCase()) return@apply
            text = context.getString(R.string.online).toUpCase()
            visibility = View.VISIBLE
            setBackgroundColor(context.color(android.R.color.holo_green_dark))
            setTextColor(context.color(R.color.white))
        }
        this.hideNetworkStripAfter5Sec()
    }

    private fun TextView.hideNetworkStripAfter5Sec() {
        doAfter(5.seconds()) { this.visibility = View.GONE }
    }
}
