// One-time downloader: fetches a clean, relevant photo for each product from the
// Pexels API and saves it locally under src/main/resources/static/images/products/<id>.jpg
// so the customer site serves them instantly (inventory is fixed, cache once).
//
// Usage:  PEXELS_API_KEY=your_key node download-product-images.mjs
// Get a free key at https://www.pexels.com/api/ (200 requests/hour is plenty for 160 items).

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(__dirname, 'src/main/resources/static/images/products');
const API = 'http://localhost:8080/api/products';
const KEY = process.env.PEXELS_API_KEY;
const CONCURRENCY = 6;

if (!KEY) {
    console.error('Missing PEXELS_API_KEY. Run:  PEXELS_API_KEY=your_key node download-product-images.mjs');
    process.exit(1);
}

// Build a concise, relevant search query from the product name.
function buildQuery(product) {
    const stop = new Set(['with','and','the','for','pack','set','kit','pressure','treated',
        'inch','inches','foot','feet','sq','cu','gallon','piece','count','pieces','pack','x']);
    const words = (product.name || '')
        .toLowerCase()
        .replace(/\d+([a-z]*)?/g, ' ')   // drop numbers + attached units
        .replace(/[^a-z\s]/g, ' ')       // drop punctuation
        .split(/\s+/)
        .filter(w => w.length > 2 && !stop.has(w));
    const q = words.slice(0, 3).join(' ').trim();
    return q || (product.category || 'hardware tool');
}

async function pexelsSearch(query) {
    const url = `https://api.pexels.com/v1/search?query=${encodeURIComponent(query)}&per_page=1&orientation=landscape`;
    const res = await fetch(url, { headers: { Authorization: KEY }, signal: AbortSignal.timeout(20000) });
    if (res.status === 429) throw new Error('rate-limited');
    if (!res.ok) throw new Error('search HTTP ' + res.status);
    const data = await res.json();
    const p = data.photos && data.photos[0];
    return p ? (p.src.large || p.src.medium || p.src.original) : null;
}

async function downloadImage(imgUrl, dest) {
    const res = await fetch(imgUrl, { redirect: 'follow', signal: AbortSignal.timeout(20000) });
    if (!res.ok) throw new Error('image HTTP ' + res.status);
    const buf = Buffer.from(await res.arrayBuffer());
    if (buf.length < 1000) throw new Error('image too small');
    fs.writeFileSync(dest, buf);
}

async function handleProduct(product) {
    const dest = path.join(OUT, `${product.productId}.jpg`);
    // Try the product-name query, then fall back to category, then department.
    const queries = [buildQuery(product), product.category, product.department].filter(Boolean);
    for (const q of queries) {
        try {
            const imgUrl = await pexelsSearch(q);
            if (!imgUrl) continue;
            await downloadImage(imgUrl, dest);
            return { ok: true };
        } catch (e) {
            if (e.message === 'rate-limited') return { ok: false, id: product.productId, err: 'rate-limited' };
        }
    }
    return { ok: false, id: product.productId, err: 'no image' };
}

async function run() {
    fs.mkdirSync(OUT, { recursive: true });
    const products = await (await fetch(API)).json();
    console.log(`Fetching Pexels photos for ${products.length} products -> ${OUT}`);

    let ok = 0, fail = 0;
    const failed = [];
    for (let i = 0; i < products.length; i += CONCURRENCY) {
        const batch = products.slice(i, i + CONCURRENCY);
        const results = await Promise.all(batch.map(handleProduct));
        for (const r of results) { if (r.ok) ok++; else { fail++; failed.push(r); } }
        process.stdout.write(`\r  ${ok + fail}/${products.length}  (ok ${ok}, fail ${fail})   `);
    }
    console.log(`\nDone. ${ok} downloaded, ${fail} failed.`);
    if (failed.length) console.log('Failed:', failed.map(f => `${f.id}(${f.err})`).join(', '));
}

run().catch(e => { console.error(e); process.exit(1); });
