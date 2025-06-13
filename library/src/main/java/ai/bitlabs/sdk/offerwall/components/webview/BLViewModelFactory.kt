package ai.bitlabs.sdk.offerwall.components.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BLViewModelFactory(
    private val token: String,
    private val uid: String,
    private val listenerId: Int,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BLWebViewViewModel(token, uid, listenerId) as T
}