/**
 * The current Android release. `/download` redirects here and the app reads `/api/release` to offer
 * updates, so publishing a new APK (scripts/upload-apk.ts) only needs this file changed and a deploy.
 */
export const LATEST_RELEASE = {
  versionCode: 3,
  versionName: '1.1.1',
  apkUrl: 'https://8eiqfkaqqyue5xri.public.blob.vercel-storage.com/localcart/releases/LocalCart-1.1.1.apk',
  sizeMb: 25,
  minSdk: 26,
  notes: 'Chat reconnects by itself when you come back to the app, and a few screens are smoother.',
} as const;

/** The narrated demo of a full order, on the website and in the README. */
export const DEMO_VIDEO_URL = 'https://8eiqfkaqqyue5xri.public.blob.vercel-storage.com/localcart/demo/LocalCart-demo-1.1.1.mp4';
