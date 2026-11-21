package app.localcart.ui.customer

import app.localcart.ui.components.ShopThumb
import app.localcart.ui.components.AwningEdge
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.localcart.core.AppContainer
import app.localcart.data.api.ApiException
import app.localcart.data.api.CatalogItemDto
import app.localcart.data.api.ShopDetailResponse
import app.localcart.domain.Distance
import app.localcart.domain.Hours
import app.localcart.domain.Money
import app.localcart.domain.ShopCategories
import app.localcart.domain.Unit as SellUnit
import app.localcart.ui.components.EmptyState
import app.localcart.ui.components.ErrorState
import app.localcart.ui.components.HairlineDivider
import app.localcart.ui.components.InlineNotice
import app.localcart.ui.components.LoadingState
import app.localcart.ui.components.PrimaryButton
import app.localcart.ui.components.SectionLabel
import app.localcart.ui.components.Sheet
import app.localcart.ui.components.StatusTag
import app.localcart.ui.components.Thumb
import app.localcart.ui.components.Tone
import app.localcart.ui.nav.Nav
import app.localcart.ui.theme.LocalFigures
import app.localcart.ui.theme.Palette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopDetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val detail: ShopDetailResponse? = null,
    val opening: Boolean = false,
    val actionError: String? = null,
)

class ShopDetailViewModel(private val container: AppContainer, private val shopId: String) : ViewModel() {
    private val _state = MutableStateFlow(ShopDetailState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = it.detail == null, error = null) }
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = false, detail = container.shops.detail(shopId)) }
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun openChat(onOpened: (String) -> Unit) {
        if (_state.value.opening) return
        _state.update { it.copy(opening = true, actionError = null) }
        viewModelScope.launch {
            try {
                val peer = container.peers.openWithShop(shopId)
                _state.update { it.copy(opening = false) }
                onOpened(peer.nexconnId)
            } catch (e: ApiException) {
                _state.update { it.copy(opening = false, actionError = e.message) }
            }
        }
    }
}

@Composable
fun ShopDetailScreen(container: AppContainer, shopId: String, nav: Nav) {
    val vm: ShopDetailViewModel = viewModel(key = "shop-$shopId") { ShopDetailViewModel(container, shopId) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val detail = state.detail

    Column(Modifier.fillMaxSize().background(Palette.Paper)) {
        when {
            state.loading -> {
                AppTopBarOnHaldi(title = "Shop", onBack = nav::back)
                LoadingState()
            }
            state.error != null -> {
                AppTopBarOnHaldi(title = "Shop", onBack = nav::back)
                ErrorState(state.error!!, onRetry = vm::load)
            }
            detail != null -> {
                val shop = detail.shop
                Box(Modifier.weight(1f)) {
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        item(key = "header") {
                            Column(Modifier.fillMaxWidth().background(Palette.Haldi)) {
                                AppTopBarOnHaldi(
                                    title = null,
                                    onBack = nav::back,
                                    onCall = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${shop.phone}"))) },
                                )
                                Row(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ShopThumb(shop.photoUrl, shop.category, size = 84.dp, onHaldi = true)
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(shop.name, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 30.sp))
                                        Text(ShopCategories.label(shop.category), style = MaterialTheme.typography.bodyMedium, color = Palette.Ink.copy(alpha = 0.72f))
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            if (shop.openNow) StatusTag("Open now", Tone.Positive) else StatusTag("Closed now", Tone.Negative)
                                            if (shop.rating != null) {
                                                Icon(Icons.Filled.Star, contentDescription = null, tint = Palette.Ink, modifier = Modifier.size(15.dp))
                                                Text("%.1f (%d)".format(shop.rating, shop.ratingCount), style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                            AwningEdge()
                        }
                        item(key = "facts") {
                            Sheet(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), padding = PaddingValues(vertical = 12.dp)) {
                                Row(Modifier.fillMaxWidth()) {
                                    Fact("Hours", Hours.range(shop.opensAt, shop.closesAt), Modifier.weight(1f))
                                    FactDivider()
                                    Fact("Distance", Distance.format(shop.distanceKm) ?: "Unknown", Modifier.weight(1f))
                                    FactDivider()
                                    Fact(
                                        "Delivery",
                                        if (shop.deliversToYou == false) "Pickup only" else "${"%.1f".format(shop.deliveryRadiusKm)} km",
                                        Modifier.weight(1f),
                                        warn = shop.deliversToYou == false,
                                    )
                                }
                            }
                        }
                        item(key = "where") {
                            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                IconLine(Icons.Outlined.LocationOn, shop.addressText)
                                IconLine(Icons.Outlined.Payments, if (shop.upiId != null) "Pay by UPI or cash on delivery" else "Cash on delivery")
                            }
                        }
                        item(key = "how") {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)).background(Palette.HaldiTint).padding(14.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "In the chat, type or say your list, like \"2 kg aloo, 1 litre Amul milk, 5 eggs\", and tap Make cart. The shop confirms prices with a bill.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        item(key = "catalog-label") {
                            SectionLabel("Catalog, ${detail.items.size} ${if (detail.items.size == 1) "item" else "items"}", Modifier.padding(top = 8.dp))
                        }
                        if (detail.items.isEmpty()) {
                            item(key = "empty") {
                                EmptyState(
                                    title = "No items listed yet",
                                    body = "You can still chat with the shop and ask what they have.",
                                    icon = Icons.Outlined.Inventory2,
                                )
                            }
                        }
                        items(detail.items, key = { it.id }) { item ->
                            Column(Modifier.animateItem()) {
                                CatalogRow(item)
                                HairlineDivider(Modifier.padding(start = 78.dp))
                            }
                        }
                    }
                }
                Column(
                    Modifier.fillMaxWidth().background(Palette.Sheet).navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    state.actionError?.let {
                        InlineNotice(it)
                        Spacer(Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        if (shop.openNow) "Chat to order" else "Chat with shop",
                        onClick = { vm.openChat { peer -> nav.chat(peer) } },
                        loading = state.opening,
                        icon = Icons.Outlined.ChatBubbleOutline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!shop.openNow) {
                        Text(
                            "Closed now. You can message them; carts open at ${Hours.pretty(shop.opensAt)}.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Back and call actions on the haldi header, drawn under the status bar. */
@Composable
private fun AppTopBarOnHaldi(title: String?, onBack: () -> Unit, onCall: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(Palette.Haldi).statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back") }
        if (title != null) Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        else Spacer(Modifier.weight(1f))
        if (onCall != null) {
            IconButton(onClick = onCall) { Icon(Icons.Outlined.Call, contentDescription = "Call shop") }
        }
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier = Modifier, warn: Boolean = false) {
    Column(modifier.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = if (warn) Palette.Chilli else Palette.Ink,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun FactDivider() {
    Box(Modifier.width(1.dp).height(36.dp).background(Palette.Rule))
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Palette.InkMuted, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted)
    }
}

@Composable
private fun CatalogRow(item: CatalogItemDto) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).alpha(if (item.inStock) 1f else 0.55f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumb(item.photoUrl, item.name, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!item.inStock) Text("Out of stock", style = MaterialTheme.typography.labelMedium, color = Palette.Chilli)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(Money.format(item.pricePaise), style = LocalFigures.current.medium)
            Text("per ${SellUnit.of(item.unit).label}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
