/**
 * SHARED FILE - photography used across the site.
 *
 * These are real Unsplash photo IDs (verified, free licence, no attribution
 * required although we credit Unsplash in the footer). Every URL goes through
 * unsplashUrl() so we request exactly the size we render, which keeps the page
 * fast. If a photo ever fails to load, <SmartImage> falls back to a stable
 * placeholder so the layout never breaks.
 */

/** Builds a correctly sized, cropped Unsplash URL. */
export function unsplashUrl(id, w = 1200, h = 800) {
  return `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=${w}&h=${h}&q=80`;
}

/** Deterministic fallback, used only when Unsplash cannot be reached. */
export function fallbackUrl(seed, w = 1200, h = 800) {
  return `https://picsum.photos/seed/${encodeURIComponent(seed)}/${w}/${h}`;
}

export const HERO_IMAGE = '1580794749460-76f97b7180d8';
export const AUTH_IMAGE = '1586193804147-64d5c02ef9c1';

/**
 * The destinations shown on the home page. `query` is what we send to the
 * packages API when a tourist clicks the card, so it must match the
 * `destination` values used in the database.
 */
export const DESTINATIONS = [
  {
    name: 'Sigiriya',
    region: 'Central Province',
    blurb: 'A fifth century palace on top of a two hundred metre rock.',
    query: 'Sigiriya',
    photo: '1567157802189-aadc856131dc',
    seed: 'sigiriya-rock',
  },
  {
    name: 'Ella',
    region: 'Uva Province',
    blurb: 'Hill country trains, waterfalls and the Nine Arch Bridge.',
    query: 'Ella',
    photo: '1566766189268-ecac9118f2b7',
    seed: 'ella-nine-arch',
  },
  {
    name: 'Galle',
    region: 'Southern Province',
    blurb: 'A walled Dutch fort where every street ends at the sea.',
    query: 'Galle',
    photo: '1509982724584-2ce0d4366d8b',
    seed: 'galle-fort',
  },
  {
    name: 'Kandy',
    region: 'Central Province',
    blurb: 'The last royal capital, built around a lake and a relic.',
    query: 'Kandy',
    photo: '1576235282476-debff2a4d0b9',
    seed: 'kandy-temple',
  },
  {
    name: 'Yala',
    region: 'Southern Province',
    blurb: 'The best chance of seeing a wild leopard anywhere on earth.',
    query: 'Yala',
    photo: '1566650576880-6740b03eaad1',
    seed: 'yala-leopard',
  },
  {
    name: 'Mirissa',
    region: 'Southern Province',
    blurb: 'Blue whales offshore, palm-backed sand along the whole bay.',
    query: 'Mirissa',
    photo: '1522310193626-604c5ef8be43',
    seed: 'mirissa-beach',
  },
  {
    name: 'Nuwara Eliya',
    region: 'Central Province',
    blurb: 'Cold mornings and tea estates that run to the horizon.',
    query: 'Nuwara Eliya',
    photo: '1544015759-237f87d55ef3',
    seed: 'nuwara-eliya-tea',
  },
  {
    name: 'Polonnaruwa',
    region: 'North Central Province',
    blurb: 'A medieval capital best seen from the seat of a bicycle.',
    query: 'Polonnaruwa',
    photo: '1588598198321-9735fd52455b',
    seed: 'polonnaruwa-ruins',
  },
];

/** Photos reused for tour packages, chosen by destination name. */
const PACKAGE_PHOTOS = {
  sigiriya:     '1539576776193-2c07122e5fee',
  ella:         '1574611122955-5baa61496637',
  galle:        '1568843240915-b512cc9b4415',
  kandy:        '1665849050332-8d5d7e59afb6',
  yala:         '1559372118-ad4a946a686f',
  mirissa:      '1554904636-b8d94ef62915',
  'nuwara eliya': '1586511623600-cb6f44f647d8',
  polonnaruwa:  '1580803834205-0e64baf9d13d',
  colombo:      '1566299597203-225f611b865f',
  anuradhapura: '1580889272861-dc2dbea5468d',
};

/**
 * Picks a photo for a package. Uses the guide's own image_url when they set
 * one, then a photo matching the destination, then a stable placeholder.
 */
export function packageImage(pkg, w = 900, h = 640) {
  if (pkg?.image_url) return pkg.image_url;
  const key = String(pkg?.destination || '').trim().toLowerCase();
  const match = Object.keys(PACKAGE_PHOTOS).find((k) => key.includes(k));
  if (match) return unsplashUrl(PACKAGE_PHOTOS[match], w, h);
  return fallbackUrl(`tour-${pkg?.package_id || key || 'default'}`, w, h);
}

export function packageImageFallback(pkg, w = 900, h = 640) {
  return fallbackUrl(`tour-${pkg?.package_id || pkg?.destination || 'default'}`, w, h);
}
