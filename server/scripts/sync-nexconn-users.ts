/**
 * Registers every LocalCart user with the Nexconn app in NEXCONN_APP_KEY. Run once after moving to a
 * new Nexconn app (new App Key): each user gets a fresh token from the new app, and shopkeepers are
 * registered under their shop name and photo so chats show the right identity before they reconnect.
 *
 *   npx tsx scripts/sync-nexconn-users.ts            # dry run: counts only
 *   npx tsx scripts/sync-nexconn-users.ts --apply
 */
import { config } from 'dotenv';

config({ path: '.env.local' });

const apply = process.argv.includes('--apply');
const { eq } = await import('drizzle-orm');
const { db } = await import('../src/db/client.js');
const { shops, users } = await import('../src/db/schema.js');
const { env } = await import('../src/env.js');
const { issueAccessToken } = await import('../src/nexconn/client.js');

const appKey = env().NEXCONN_APP_KEY;
const rows = await db().select({ user: users, shop: shops }).from(users).leftJoin(shops, eq(shops.ownerId, users.id));
const stale = rows.filter((r) => r.user.nexconnTokenAppKey !== appKey);
console.log(`${rows.length} users, ${stale.length} not yet registered with app ${appKey}`);
if (!apply) {
  console.log('Dry run. Pass --apply to register them.');
  process.exit(0);
}

let ok = 0;
for (const { user, shop } of stale) {
  try {
    const token = await issueAccessToken(user.nexconnId, shop?.name ?? user.name, shop?.photoUrl);
    await db().update(users).set({ nexconnToken: token, nexconnTokenAppKey: appKey }).where(eq(users.id, user.id));
    ok++;
  } catch (err) {
    console.error(`failed for ${user.nexconnId}:`, err instanceof Error ? err.message : err);
  }
}
console.log(`Registered ${ok}/${stale.length}`);
