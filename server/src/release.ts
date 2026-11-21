/**
 * The current Android release. `/download` redirects here and the app reads `/api/release` to offer
 * updates, so publishing a new APK (scripts/upload-apk.ts) only needs this file changed and a deploy.
 */
export const LATEST_RELEASE = {
  versionCode: 1,
  versionName: '1.0.0',
  apkUrl: 'https://8eiqfkaqqyue5xri.public.blob.vercel-storage.com/localcart/releases/LocalCart-1.0.0.apk',
  sizeMb: 25,
  minSdk: 26,
  notes: 'First release.',
} as const;
