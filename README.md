<p align="center">
  <img src="server/public/favicon.svg" width="84" height="84" alt="LocalCart logo: a striped shop awning over a chat bubble">
</p>

<h1 align="center">LocalCart</h1>

<p align="center"><strong>Order from the shop on your street, by chat.</strong><br>
अपनी गली की दुकान से, चैट पर।</p>

<p align="center">
  <a href="https://localcart-api.vercel.app">Website</a> ·
  <a href="https://localcart-api.vercel.app/demo">Watch the demo (5½ min)</a> ·
  <a href="https://localcart-api.vercel.app/download">Download the Android app (v1.1.1, 25 MB)</a> ·
  <a href="https://github.com/happypapa636/Local-Cart/releases">Releases</a> ·
  <a href="https://localcart-api.vercel.app/api/health">API status</a> ·
  <a href="https://localcart-api.vercel.app/privacy">Privacy</a>
</p>

---

In India, people already order from the neighbourhood kirana on WhatsApp: *"bhaiya, 2 kilo pyaaz, 1 litre
Amul doodh, 6 ande bhej do"*. It works, but it's unstructured. The shopkeeper retypes the list and guesses
prices, nobody keeps track of who has paid, and the customer never knows when the order will arrive.

**LocalCart keeps the chat and adds the structure.** A customer types or speaks their list in Hindi,
English or both. AI turns it into a cart matched to *that shop's* catalog. The shop confirms availability
and prices with a bill in one tap. The customer pays cash on delivery or by UPI, and a live order card in
the same chat moves from Accepted to Packed to Out for delivery to Delivered on both phones.

It's built on **[Nexconn](https://docs.nexconn.ai) Chat**: the Android Chat SDK on the phones, and the
Platform Chat API on the server, which posts the order cards and moves them along.

## Demo video

**[Watch one order, start to finish](https://localcart-api.vercel.app/demo)** (5½ minutes, narrated). It's
recorded straight off the phone, against the live backend: a Hinglish list becomes a cart, the shop bills it
from its own side of the app, and the order card moves to delivered on both sides, with an explanation of
how Nexconn carries each step. The file is also attached to the
[v1.1.1 release](https://github.com/happypapa636/Local-Cart/releases/tag/v1.1.1).

## How an order works

1. **Send your list the way you'd text the shop.** Type it, or hold the mic and say it. Hindi, English or
   Hinglish all work.
2. **Tap Make cart.** Gemini reads the message or voice note and matches each line to the shop's catalog,
   in the right unit and at the shop's price. Anything it can't match stays on the cart for the shop to
   price. The server posts it into the chat as a **Cart card**.
3. **The shop sends a bill.** The shopkeeper ticks what's in stock, fixes prices or quantities, adds a
   delivery fee and sends a **Bill card** back into the same chat.
4. **Pay and watch it come.** Cash on delivery, or UPI (a real `upi://pay` intent with the shop's UPI ID
   and the exact amount). The **Order card** moves Accepted → Packed → Out for delivery → Delivered on
   both phones, in place, without anyone sending a new message. Then rate the shop.

## Features

**If you're ordering**
- Sign up with a mobile number; share GPS or type an address.
- Shops within 10 km, open ones first, with category filters and search by shop *or* item name.
- Shop pages with catalog, prices, stock, hours, distance, delivery radius, payment options, rating and a
  call button.
- Edit a cart until the shop bills it (quantities by unit step, remove lines, add from the catalog).
- Accept the bill and pay cash or UPI, then tap "I've paid"; share a delivery pin as a location message.
- Order history with a progress bar per order, and one-tap reorder at today's prices.

**If you run a shop**
- Set up the shop: photo (camera or gallery), GPS pin, delivery radius, opening hours, UPI ID.
- Catalog with photos, units, prices and stock toggles. Paste a price list ("Aloo 30/kg, Amul Taaza 500ml
  28 …") and AI turns it into rows to review and save.
- Dashboard: open/closed switch, today's sales, orders by stage, unread chats, all updating live.
- Bill editor: availability, quantity and price per line, delivery fee, note. Then Packed, Out for
  delivery, Delivered, payment confirmation, or cancel with a reason the customer sees.

**Chat everywhere:** text, photos, HD voice notes and locations; typing indicators, read receipts, unread
badges, conversation list, offline history, failed-send retry and in-app alerts.

**Account:** in-app update banner when a newer APK is published, a privacy note, and account deletion
(password-confirmed; removes the account, shop, catalog and orders).

## How LocalCart uses Nexconn

Data centre: **Singapore** (`AreaCode.SG` on the phones, `https://api.sg-light-api.com` on the server).
SDK: `ai.nexconn.chat:chat:26.4.2`.

```
 Android app (Compose)                         Vercel (Hono API)                 External
 ┌──────────────────────────┐   HTTPS/JSON    ┌──────────────────────────┐
 │ ui/ screens + viewmodels │ ──────────────▶ │ /api/auth, /api/shops,   │──▶ Neon Postgres (source of truth)
 │ data/ repositories       │   Bearer JWT    │ /api/orders, /api/chats  │──▶ Gemini (order + catalog parsing)
 │ chat/ NexconnChat wrapper│                 │ services/orders.ts       │──▶ Vercel Blob (photos, APK)
 └───────────┬──────────────┘                 └───────────┬──────────────┘
             │ Nexconn Android Chat SDK                   │ Platform Chat API (signed REST)
             ▼                                            ▼
        ┌───────────────────── Nexconn (Singapore data centre) ─────────────────────┐
        │ direct channels, text / image / HD voice / location messages, custom      │
        │ card messages (lcr:cart, lcr:bill, lcr:status), message metadata, message │
        │ update, read receipts, typing, unread counts, history                     │
        └────────────────────────────────────────────────────────────────────────────┘
```

### The order lives in the chat

Postgres holds the order and is the source of truth. The server posts each card **once**, as a custom
Nexconn message sent with `hasMetadata`. Every later change (placed, billed, accepted, packed, paid,
cancelled, rated) is a **message metadata** update on the same message, so both phones receive
`onMessageMetadataUpdated` and redraw the card where it already sits. Editing a cart uses **message
update**, so the original Cart card changes for both people. Whenever a card is on screen the app also
re-reads the order from the API, so a missed realtime event can't leave a card wrong.

The metadata snapshot every card renders from is `{ status, payment, rev, reason, rating, at }`
([`server/src/services/cards.ts`](server/src/services/cards.ts)).

### Every Nexconn feature in use

| Nexconn feature | What LocalCart does with it | Code |
| --- | --- | --- |
| `NCEngine.initialize` (`AreaCode.SG`, push off) | SDK set up once per process with the App Key served by our backend (`GET /api/config`), refreshed on every start so a new Nexconn app needs no app update | `android/.../chat/NexconnChat.kt` |
| `NCEngine.connect` + connection status handler | Server-issued token; refresh and reconnect on 31004 / 31020 / `TOKEN_INCORRECT`; connecting, offline and "open on another phone" states in the UI | `NexconnChat.kt`, `ui/chat/ChatScreen.kt`, `ui/nav/LocalCartRoot.kt` |
| Direct channels | One channel per customer and shop pair | `DirectChannel(peerId)` |
| Text messages with `needReceipt` | Everyday chat, and the typed order the AI reads | `NexconnChat.sendText()` |
| HD voice messages | Spoken orders (AAC, 44.1 kHz) the shop can play; the same audio goes to Gemini | `media/Media.kt`, `NexconnChat.sendVoice()` |
| Image messages | Product photos with upload progress | `NexconnChat.sendImage()` |
| Location messages (`RC:LBSMsg`) | Delivery pins with a JPEG map preview. SDK 26.4.2 rejects client-side location sends (31610), so the server sends the same built-in type through the Platform API as the user; both phones receive and render it via the SDK | `NexconnChat.sendLocation()`, `server/src/routes/chat.ts` |
| **Custom message types** | `lcr:cart`, `lcr:bill`, `lcr:status` cards, registered with `registerCustomMessages` before connect | `chat/CardMessages.kt`, `ui/chat/Cards.kt` |
| **Message metadata** | Live card state, set by the server on every order change | `server/src/services/orders.ts`, `ui/chat/ChatViewModel.kt` |
| **Message update** | Cart edits replace the original card's content (`onMessagesUpdated`) | `server/src/nexconn/client.ts` |
| Read receipts | `sendReadReceiptResponse` when seen; ticks from `onMessageReceiptResponse` and `getMessageReadReceiptInfo` | `NexconnChat.markRead()` |
| Typing indicators | `sendTypingStatus` while composing; animated "typing…" in the chat header | `NexconnChat.sendTyping()` |
| Unread counts | Badge on the Chats tab, per-chat badges, unread chats on the shop dashboard; `clearUnreadCount` on open | `NexconnChat.refreshUnread()` |
| Conversation list | `createChannelsQuery`, resolved to shop and customer names through our API | `ui/chat/ChatListScreen.kt` |
| History pagination | `createMessagesQuery` pages older messages from the local database | `ChatViewModel.loadMore()` |
| Failed-send retry | `deleteMessagesForMe`, then resend the same content | `NexconnChat.resend()` |
| Platform Chat API | Token issue (`/v4/auth/access-token/issue`), user info (`/v4/user/update`), server-sent cards (`/v4/direct-channel/message/send` with `hasMetadata` and `isEchoToSender`), metadata (`/v4/channel-type/message/metadata/set`), message update (`/v4/direct-channel/message/update`), all signed with SHA1(secret + nonce + timestamp) | `server/src/nexconn/client.ts`, `server/src/nexconn/signature.ts` |

### Moving to a new Nexconn app

The App Key and secret live only in server env vars. Cached chat tokens in Postgres record the App Key
that issued them (`users.nexconn_token_app_key`), so a token from an old app is never handed out, and the
Android app fetches the App Key on each start and drops its cached token when the key changes. To switch:

```bash
# server/.env.local and Vercel (production + development)
NEXCONN_APP_KEY=...  NEXCONN_APP_SECRET=...  NEXCONN_API_BASE=https://api.sg-light-api.com
npx tsx scripts/sync-nexconn-users.ts --apply   # register existing users (shop name/photo) in the new app
vercel deploy --prod
```

Chat history from the old app stays in the old app; orders and their state carry over, because they live
in Postgres.

## Design

LocalCart takes its look from a kirana shopfront, on the website and in the app alike:

- **Colour.** Haldi `#F6C445` (painted-signboard yellow) for brand surfaces, khata ink `#16213E` for text,
  dhaniya `#0E6B4A` for actions and "open", mirchi `#D2412F` for alerts, on paper `#F4F6F3`.
- **Type.** One family, [Anek Devanagari](https://fonts.google.com/specimen/Anek+Devanagari), for English
  and Hindi alike. Its width axis carries the hierarchy: condensed and heavy for signboard headlines and
  rupee figures, normal width for reading. Bills use tabular figures so columns of rupees line up.
- **Mark.** A striped shop awning over a chat bubble with typing dots: the shop is typing. It's the app
  icon (with a themed-icon layer), the favicon and the launch screen.
- **Motif.** Headers end in the awning's scalloped edge. Cart, Bill and Order cards carry haldi, dhaniya
  and ink header bands so each document is easy to spot in a long chat.
- **Motion.** Purposeful: screens slide forward and back, lists animate items in, card status changes
  animate in place, the current delivery step pulses, and loading shows shimmering skeletons. The website
  has one orchestrated moment: a bill slip that prints from a chat message, then a scroll-driven phone demo.

## Website

The site at **https://localcart-api.vercel.app** is static HTML, CSS and a little JavaScript in
[`server/public/`](server/public), served by Vercel's CDN from the same project as the API: landing page,
privacy note, favicon set, web manifest, Open Graph image, robots and sitemap. `/download` always
redirects to the latest APK, and `/api/release` tells the app when there's a newer one.

## Tech stack

- **Android:** Kotlin 2.2, Jetpack Compose (Material 3, custom theme), type-safe Navigation, Retrofit +
  kotlinx.serialization, Coil, core-splashscreen, manual DI, Android Keystore (AES-GCM) for the session.
  minSdk 26, target 36, R8, signed release.
- **Backend:** TypeScript (strict), Hono on Vercel, Drizzle ORM on Neon Postgres, zod on every input,
  bcrypt + JWT, Vercel Blob, Gemini via `@google/genai` (structured JSON, model fallback, retries),
  Postgres-backed rate limiting.

## Run it yourself

### Backend

```bash
cd server
npm install
vercel env pull .env.local --yes   # or copy .env.example and fill it in
npm run db:migrate
npm run dev                        # site and API on http://localhost:3106
npm test                           # unit tests + the full order loop on real Neon, Nexconn and Gemini
```

Environment variables (names only): `DATABASE_URL`, `DATABASE_URL_UNPOOLED`, `BLOB_READ_WRITE_TOKEN`,
`NEXCONN_APP_KEY`, `NEXCONN_APP_SECRET`, `NEXCONN_API_BASE`, `NEXCONN_ID_PREFIX`, `JWT_SECRET`,
`GEMINI_API_KEY`, `GEMINI_MODEL`, `GEMINI_FALLBACK_MODEL`. The App Secret never leaves the server.

### Android

```bash
cd android
./gradlew :app:assembleDebug                                              # talks to production
./gradlew :app:assembleDebug -Plocalcart.apiBaseUrl=http://10.0.2.2:3106/  # local backend
./gradlew :app:testDebugUnitTest
```

### Try an order with one phone

Shops show up within 10 km of the customer. To get a demo shop next to you, sign up as a customer in the
app, then let the script play the shopkeeper:

```bash
cd server
npx tsx scripts/e2e-order-flow.ts shop:setup <your-lat> <your-lng>   # an open shop with a catalog nearby
# in the app: open the shop, chat, Make cart, Send to shop
npx tsx scripts/e2e-order-flow.ts shop:bill                          # the shop bills it
# in the app: Accept and pay
npx tsx scripts/e2e-order-flow.ts shop:advance                       # run up to three times: packed, out, delivered
npx tsx scripts/e2e-order-flow.ts cleanup                            # remove the demo shop again
```

### Release

```bash
cd android && ./gradlew :app:assembleRelease          # needs android/keystore.properties (see below)
cd ../server && npx tsx scripts/upload-apk.ts ../android/app/build/outputs/apk/release/app-release.apk 1.1.0
# set the printed URL and version in server/src/release.ts, then:
vercel deploy --prod
```

The release keystore is not in git. Create one with `keytool -genkeypair -v -keystore
localcart-release.jks -alias localcart -keyalg RSA -keysize 2048 -validity 10000`, and put `storeFile`,
`storePassword`, `keyAlias` and `keyPassword` in `android/keystore.properties` (gitignored). Updates must
be signed with the same key.

## Limitations

- **No push after the app is closed.** The shared Nexconn console has no FCM credentials. Messages alert in
  real time while LocalCart is open or in the background; nothing arrives after the process is killed
  until the app reopens.
- **UPI confirmation is manual.** The customer marks the order paid and the shop confirms. There's no
  payment gateway, so LocalCart never handles money.
- **Chat history is per device.** It comes from the SDK's local database; after a reinstall, older messages
  aren't re-downloaded. Orders and their state always load from the server.
- **One phone per account at a time** (Nexconn default). A second sign-in shows "open on another phone".

Contributor notes, the folder map and conventions are in [AGENTS.md](AGENTS.md).
