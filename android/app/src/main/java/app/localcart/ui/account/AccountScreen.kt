package app.localcart.ui.account

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.localcart.BuildConfig
import app.localcart.chat.ChatConnection
import app.localcart.core.AppContainer
import app.localcart.data.api.ApiException
import app.localcart.data.api.ReleaseDto
import app.localcart.data.repo.Session
import app.localcart.ui.components.Field
import app.localcart.ui.components.HairlineDivider
import app.localcart.ui.components.InlineNotice
import app.localcart.ui.components.KeyValueRow
import app.localcart.ui.components.PrimaryButton
import app.localcart.ui.components.ScreenHeading
import app.localcart.ui.components.SectionLabel
import app.localcart.ui.components.Sheet
import app.localcart.ui.components.StatusTag
import app.localcart.ui.components.Tone
import app.localcart.ui.nav.Nav
import app.localcart.ui.theme.Palette
import kotlinx.coroutines.launch

private const val PRIVACY_URL = "https://localcart-api.vercel.app/privacy"

@Composable
fun AccountScreen(container: AppContainer, nav: Nav) {
    val session by container.session.session.collectAsState()
    val connection by container.chat.connection.collectAsState()
    val user = (session as? Session.SignedIn)?.user ?: return
    val context = LocalContext.current
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    var update by remember { mutableStateOf<ReleaseDto?>(null) }
    LaunchedEffect(Unit) {
        update = container.auth.latestRelease()?.takeIf { it.versionCode > BuildConfig.VERSION_CODE }
    }
    val open = { url: String -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeading(title = "Account")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AnimatedVisibility(update != null, enter = fadeIn() + expandVertically()) {
                val release = update ?: return@AnimatedVisibility
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Palette.Haldi).clickable { open(release.apkUrl) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("LocalCart ${release.versionName} is ready", style = MaterialTheme.typography.titleSmall)
                        Text(release.notes.ifBlank { "Tap to download the update." }, style = MaterialTheme.typography.bodySmall, color = Palette.Ink.copy(alpha = 0.75f))
                    }
                    Text("Update", style = MaterialTheme.typography.labelLarge)
                }
            }
            Sheet(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Palette.Haldi),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(user.name.trim().firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.titleLarge)
                            Text("+91 ${user.phone}", style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
                        }
                        StatusTag(if (user.isShopkeeper) "Shopkeeper" else "Customer", Tone.Neutral)
                    }
                    Spacer(Modifier.height(12.dp))
                    HairlineDivider()
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow(
                        "Chat",
                        when (connection) {
                            ChatConnection.Connected -> "Connected"
                            ChatConnection.Connecting, ChatConnection.Reconnecting -> "Connecting"
                            ChatConnection.Offline -> "Offline"
                            ChatConnection.SignedInElsewhere -> "Open on another phone"
                            ChatConnection.Failed -> "Couldn't connect"
                            ChatConnection.Idle -> "Not connected"
                        },
                        valueColor = if (connection == ChatConnection.Connected) Palette.Leaf else Palette.InkMuted,
                    )
                }
            }
            if (!user.isShopkeeper) {
                SectionLabel("Delivery")
                Sheet(Modifier.padding(horizontal = 16.dp), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    SettingRow(Icons.Outlined.Place, "Delivery location", user.addressText ?: "No address set", onClick = nav::location)
                }
            } else {
                SectionLabel("Shop")
                Sheet(Modifier.padding(horizontal = 16.dp), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    SettingRow(Icons.Outlined.Storefront, "Shop settings", "Name, photo, hours, delivery radius, UPI ID", onClick = nav::shopSettings)
                }
            }
            SectionLabel("Notifications")
            Text(
                "While LocalCart is open or in the background, new messages, carts and bills alert you instantly. " +
                    "Alerts after the app is fully closed need push notifications, which this build does not include yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.InkMuted,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            SectionLabel("About", Modifier.padding(top = 8.dp))
            Sheet(Modifier.padding(horizontal = 16.dp), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Column {
                    SettingRow(Icons.Outlined.Shield, "Privacy", "What we store and who can see it", onClick = { open(PRIVACY_URL) })
                    HairlineDivider(Modifier.padding(start = 56.dp))
                    SettingRow(Icons.AutoMirrored.Outlined.Logout, "Sign out", "Stop receiving messages on this phone", onClick = { confirmSignOut = true })
                    HairlineDivider(Modifier.padding(start = 56.dp))
                    SettingRow(Icons.Outlined.DeleteOutline, "Delete account", "Remove your account and orders for good", onClick = { deleting = true }, danger = true)
                }
            }
            Text(
                "LocalCart ${BuildConfig.VERSION_NAME}. Chat by Nexconn.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(20.dp),
            )
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out of LocalCart?") },
            text = { Text("You'll stop receiving messages on this phone until you sign in again.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; container.signOut() }) { Text("Sign out", color = Palette.Chilli) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Stay signed in") } },
        )
    }
    if (deleting) {
        DeleteAccountDialog(container, isShopkeeper = user.isShopkeeper, onDismiss = { deleting = false })
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, body: String, onClick: () -> Unit, danger: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) Palette.Chilli else Palette.Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (danger) Palette.Chilli else Palette.Ink)
            Text(body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Palette.InkFaint)
    }
}

/** Deleting is permanent, so it asks for the password again and says exactly what goes. */
@Composable
private fun DeleteAccountDialog(container: AppContainer, isShopkeeper: Boolean, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Delete your account?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (isShopkeeper) {
                        "Your account, your shop, its catalog and every order with it are deleted right away. This can't be undone."
                    } else {
                        "Your account and your orders are deleted right away. This can't be undone."
                    },
                )
                Field(
                    password,
                    { password = it; error = null },
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                )
                error?.let { InlineNotice(it) }
            }
        },
        confirmButton = {
            PrimaryButton(
                "Delete account",
                destructive = true,
                loading = busy,
                enabled = password.isNotEmpty(),
                onClick = {
                    busy = true
                    scope.launch {
                        try {
                            container.auth.deleteAccount(password)
                            container.signOut()
                        } catch (e: ApiException) {
                            error = e.message
                            busy = false
                        }
                    }
                },
            )
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Keep my account") } },
    )
}
