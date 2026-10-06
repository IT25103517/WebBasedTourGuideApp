/**
 * SHARED FILE - fades a section in the first time it scrolls into view.
 *
 * The 'reveal' class (which sets opacity to 0) is added by JavaScript, never
 * in the markup. That way, if JavaScript or IntersectionObserver is not
 * available, the content is simply visible instead of invisible.
 *
 * Uses IntersectionObserver rather than a scroll listener, so it costs nothing
 * while the visitor scrolls, and respects prefers-reduced-motion.
 */
import { useEffect, useRef } from 'react';

export function useReveal() {
  const ref = useRef(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return undefined;

    const target = el.querySelector('[data-reveal]') || el;

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduce || typeof IntersectionObserver === 'undefined') return undefined;

    // already on screen when the page loads, so do not hide it at all
    const box = target.getBoundingClientRect();
    if (box.top < window.innerHeight) return undefined;

    target.classList.add('reveal');

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('in');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
    );

    observer.observe(target);

    // safety net: never leave a section hidden
    const timer = setTimeout(() => target.classList.add('in'), 2500);

    return () => { observer.disconnect(); clearTimeout(timer); };
  }, []);

  return ref;
}
