/**
 * SHARED FILE - an <img> that never leaves a hole in the layout.
 *
 * Photos come from Unsplash over the internet. If a photo cannot be reached
 * (no connection during a demo, or the photo was removed) we swap in a stable
 * placeholder instead of showing a broken image icon. Until either loads we
 * show a shimmering block of the right shape, so nothing jumps around.
 *
 * The parent element must be positioned (the placeholder is absolute).
 */
import { useEffect, useState } from 'react';

export default function SmartImage({
  src,
  fallback,
  alt = '',
  className = '',
  eager = false,
  style,
  ...rest
}) {
  const [current, setCurrent] = useState(src);
  const [loaded, setLoaded] = useState(false);
  const [usedFallback, setUsedFallback] = useState(false);

  // if the parent passes a different photo, start again
  useEffect(() => {
    setCurrent(src);
    setLoaded(false);
    setUsedFallback(false);
  }, [src]);

  function handleError() {
    if (!usedFallback && fallback && current !== fallback) {
      setUsedFallback(true);
      setCurrent(fallback);
    } else {
      // give up quietly rather than looping forever
      setLoaded(true);
    }
  }

  return (
    <>
      {!loaded && (
        <span className="skeleton" style={{ position: 'absolute', inset: 0 }} aria-hidden="true" />
      )}
      <img
        {...rest}
        src={current}
        alt={alt}
        loading={eager ? 'eager' : 'lazy'}
        fetchpriority={eager ? 'high' : undefined}
        decoding="async"
        className={className}
        onLoad={() => setLoaded(true)}
        onError={handleError}
        style={{ opacity: loaded ? 1 : 0, transition: 'opacity .45s ease', ...style }}
      />
    </>
  );
}
