import { config } from 'dotenv';

config({ path: '.env.local' });

const { serve } = await import('@hono/node-server');
const { serveStatic } = await import('@hono/node-server/serve-static');
const { Hono } = await import('hono');
const { default: app } = await import('./index.js');

// On Vercel, public/ is served by the CDN with clean URLs (vercel.json); mirror that locally.
const local = new Hono();
local.use('*', serveStatic({ root: './public', rewriteRequestPath: (p) => (p === '/privacy' ? '/privacy.html' : p) }));
local.all('*', (c) => app.fetch(c.req.raw));

const port = Number(process.env.PORT ?? 3106);
serve({ fetch: local.fetch, port }, (info) => {
  console.log(`LocalCart API listening on http://localhost:${info.port}`);
});
