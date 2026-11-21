package app.localcart

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.localcart.core.AppContainer
import app.localcart.data.repo.Session
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LocalCartApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Bring chat back when the app returns to the foreground or the network comes back.
        container.chat.watchNetwork()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = container.chat.nudge()
        })
        // Connect to Nexconn whenever someone is signed in (including after process death),
        // and disconnect on sign-out.
        container.appScope.launch {
            container.session.session
                .map { (it as? Session.SignedIn)?.user?.id }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId != null) container.chat.start() else container.chat.stop()
                }
        }
    }
}
