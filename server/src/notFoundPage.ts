/** Browser-facing 404 in the site's style; API paths keep the JSON error envelope. */
export const NOT_FOUND_HTML = `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Page not found | LocalCart</title><meta name="robots" content="noindex">
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Anek+Devanagari:wdth,wght@75..125,300..800&display=swap">
<link rel="stylesheet" href="/styles.css"></head>
<body><main class="doc"><div class="wrap wrap-narrow">
<img src="/favicon.svg" alt="" width="56" height="56">
<h1 style="margin-top:28px">This shelf is empty.</h1>
<p>There’s no page at this address. It may have moved, or the link has a typo.</p>
<p style="margin-top:28px"><a class="btn btn-ink" href="/">Go to the LocalCart home page</a></p>
</div></main></body></html>`;
