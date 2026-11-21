import { Hono } from 'hono';
import { bodyLimit } from 'hono/body-limit';
import { secureHeaders } from 'hono/secure-headers';
import type { AuthVars } from './lib/auth.js';
import { errorResponse } from './lib/http.js';
import { NOT_FOUND_HTML } from './notFoundPage.js';
import { DEMO_VIDEO_URL, LATEST_RELEASE } from './release.js';
import { authRoutes } from './routes/auth.js';
import { catalogRoutes } from './routes/catalog.js';
import { chatRoutes } from './routes/chat.js';
import { orderRoutes } from './routes/orders.js';
import { shopRoutes } from './routes/shops.js';
import { uploadRoutes } from './routes/uploads.js';

const app = new Hono<{ Variables: AuthVars }>();

app.use('*', secureHeaders());
app.use('/api/*', bodyLimit({ maxSize: 4.4 * 1024 * 1024, onError: (c) => c.json({ error: { code: 'too_large', message: 'That upload is too large.' } }, 413) }));

// The landing page, privacy note and icons are static files in public/, served by Vercel's CDN. Vercel's
// Hono preset sends "/" to this function, so it hands back the static home page, cached at the edge.
app.get('/', async (c) => {
  const page = await fetch(new URL('/home', c.req.url), { signal: AbortSignal.timeout(5000) });
  if (!page.ok) return c.redirect('/home', 302);
  return c.body(await page.text(), 200, {
    'content-type': 'text/html; charset=utf-8',
    'cache-control': 'public, max-age=0, s-maxage=600, stale-while-revalidate=86400',
  });
});
app.get('/api', (c) =>
  c.json({ name: 'LocalCart API', source: 'https://github.com/happypapa636/Local-Cart', health: '/api/health' }),
);
app.get('/api/health', (c) => c.json({ ok: true, time: new Date().toISOString() }));
app.get('/api/release', (c) => c.json({ release: LATEST_RELEASE }));
// A stable link for the latest APK, used by the site and the README.
app.get('/download', (c) => c.redirect(LATEST_RELEASE.apkUrl, 302));
app.get('/demo', (c) => c.redirect(DEMO_VIDEO_URL, 302));

app.route('/api', authRoutes);
app.route('/api', chatRoutes);
app.route('/api', uploadRoutes);
app.route('/api', catalogRoutes);
app.route('/api', shopRoutes);
app.route('/api', orderRoutes);

app.notFound((c) => {
  if (!c.req.path.startsWith('/api') && (c.req.header('accept') ?? '').includes('text/html')) {
    return c.html(NOT_FOUND_HTML, 404);
  }
  return c.json({ error: { code: 'not_found', message: 'No such endpoint.' } }, 404);
});
app.onError(errorResponse);

export default app;
