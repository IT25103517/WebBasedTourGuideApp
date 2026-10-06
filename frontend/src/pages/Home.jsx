/**
 * The public landing page.
 *
 * Everything below the hero is real data from the API, not mock content:
 * the tours come from FR-01, the ratings from FR-06 and the promotions
 * from FR-05.
 */
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { MagnifyingGlass, ShieldCheck, CalendarCheck, Compass, ArrowRight } from '@phosphor-icons/react';

import SmartImage from '../components/SmartImage.jsx';
import { Empty, TileSkeletons, Rating, Avatar } from '../components/Ui.jsx';
import { useReveal } from '../lib/useReveal.js';
import { packagesApi } from '../features/packages/packages.api.js';
import { reviewsApi } from '../features/reviews/reviews.api.js';
import { money } from '../api/client.js';
import {
  DESTINATIONS, HERO_IMAGE, unsplashUrl, fallbackUrl,
  packageImage, packageImageFallback,
} from '../lib/images.js';

/* ------------------------------------------------------------------ hero -- */
function Hero() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ destination: '', date: '', travellers: '2' });

  function search(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    if (form.destination) params.set('destination', form.destination);
    if (form.date) params.set('date', form.date);
    if (form.travellers) params.set('travellers', form.travellers);
    navigate(`/packages${params.toString() ? `?${params}` : ''}`);
  }

  const today = new Date().toISOString().slice(0, 10);

  return (
    <section className="hero">
      <div className="hero-media">
        <SmartImage
          eager
          src={unsplashUrl(HERO_IMAGE, 2000, 1200)}
          fallback={fallbackUrl('sri-lanka-hero', 2000, 1200)}
          alt="Sigiriya rock fortress rising above the forest in central Sri Lanka"
        />
      </div>
      <div className="hero-scrim" />

      <div className="hero-inner">
        <h1>See Sri Lanka with someone who lives there</h1>
        <p>Verified local guides, published prices, and real reviews from travellers who took the tour.</p>

        <form className="searchbar" onSubmit={search}>
          <div className="field">
            <label htmlFor="s-dest">Where to</label>
            <select
              id="s-dest"
              value={form.destination}
              onChange={(e) => setForm({ ...form, destination: e.target.value })}
            >
              <option value="">Anywhere in Sri Lanka</option>
              {DESTINATIONS.map((d) => (
                <option key={d.name} value={d.query}>{d.name}</option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="s-date">When</label>
            <input
              id="s-date" type="date" min={today}
              value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })}
            />
          </div>

          <div className="field">
            <label htmlFor="s-people">Travellers</label>
            <select
              id="s-people"
              value={form.travellers}
              onChange={(e) => setForm({ ...form, travellers: e.target.value })}
            >
              {[1, 2, 3, 4, 5, 6, 8, 10].map((n) => (
                <option key={n} value={n}>{n} {n === 1 ? 'traveller' : 'travellers'}</option>
              ))}
            </select>
          </div>

          <button type="submit" className="btn btn-lg">
            <MagnifyingGlass size={17} weight="bold" /> Search
          </button>
        </form>
      </div>
    </section>
  );
}

/* ---------------------------------------------------------- destinations -- */
function Destinations() {
  const ref = useReveal();

  return (
    <section className="section" ref={ref} id="destinations">
      <div className="shell" data-reveal>
        <div className="row-between" style={{ marginBottom: 28 }}>
          <div>
            <h2>Where do you want to go</h2>
            <p className="lede mb-0">Eight places our guides know street by street.</p>
          </div>
          <Link to="/packages" className="btn btn-ghost">
            All tours <ArrowRight size={16} />
          </Link>
        </div>

        <div className="mosaic">
          {DESTINATIONS.map((d, i) => (
            <Link
              key={d.name}
              className="dest"
              to={`/packages?destination=${encodeURIComponent(d.query)}`}
            >
              <SmartImage
                src={unsplashUrl(d.photo, i === 0 ? 1200 : 700, i === 0 ? 900 : 560)}
                fallback={fallbackUrl(d.seed, 900, 700)}
                alt={`${d.name}, ${d.region}`}
              />
              <div className="dest-text">
                <h3>{d.name}</h3>
                <p>{i === 0 || i === 5 ? d.blurb : d.region}</p>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}

/* --------------------------------------------------------- featured tours -- */
function FeaturedTours() {
  const ref = useReveal();
  const [tours, setTours] = useState(null);

  useEffect(() => {
    packagesApi.listPublished({})
      .then((r) => setTours(r.data.slice(0, 6)))
      .catch(() => setTours([]));
  }, []);

  return (
    <section className="section section-tint" ref={ref}>
      <div className="shell" data-reveal>
        <div className="row-between" style={{ marginBottom: 28 }}>
          <div>
            <h2>Tours people are booking</h2>
            <p className="lede mb-0">Published by verified guides, priced per person.</p>
          </div>
        </div>

        {tours === null ? <TileSkeletons count={3} />
          : tours.length === 0 ? (
            <Empty title="No tours published yet">
              Once a guide publishes a tour package it appears here.
            </Empty>
          ) : (
            <div className="grid grid-3">
              {tours.map((p) => (
                <Link key={p.package_id} to={`/packages/${p.package_id}`} className="tile">
                  <div className="tile-media">
                    <SmartImage
                      src={packageImage(p)}
                      fallback={packageImageFallback(p)}
                      alt={p.title}
                    />
                  </div>
                  <div className="tile-body">
                    <h3 className="tile-title">{p.title}</h3>
                    <p className="tile-meta">
                      {p.destination} · {p.duration_days} {p.duration_days === 1 ? 'day' : 'days'}
                    </p>
                    <p className="tile-meta" style={{ marginTop: 6 }}>
                      <Rating value={p.guide_rating} />
                    </p>
                    <div className="tile-foot">
                      <span className="price">{money(p.base_price)} <small>per person</small></span>
                      <span className="tile-guide">{p.guide_name}</span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
      </div>
    </section>
  );
}

/* ---------------------------------------------------------- how it works -- */
function HowItWorks() {
  const ref = useReveal();

  const steps = [
    {
      icon: <Compass size={21} weight="duotone" />,
      title: 'Find a guide',
      body: 'Filter by destination, price and length. Every guide shows their experience, languages and rating.',
    },
    {
      icon: <CalendarCheck size={21} weight="duotone" />,
      title: 'Pick a free date',
      body: 'You only see dates the guide has actually opened, so a request is never wasted.',
    },
    {
      icon: <ShieldCheck size={21} weight="duotone" />,
      title: 'Travel, then review',
      body: 'Pay the guide directly on the day. Afterwards you can rate the tour for the next traveller.',
    },
  ];

  return (
    <section className="section" ref={ref}>
      <div className="shell" data-reveal>
        <h2 style={{ marginBottom: 34 }}>How booking works</h2>
        <div className="steps">
          {steps.map((s) => (
            <div className="step" key={s.title}>
              <span className="step-mark">{s.icon}</span>
              <h3>{s.title}</h3>
              <p>{s.body}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* --------------------------------------------------------------- reviews -- */
function RecentReviews() {
  const ref = useReveal();
  const [reviews, setReviews] = useState(null);

  useEffect(() => {
    // gather the newest reviews across the guides that have any
    packagesApi.listPublished({})
      .then(async (r) => {
        const guideIds = [...new Set(r.data.map((p) => p.guide_id))].slice(0, 4);
        const results = await Promise.all(
          guideIds.map((id) => reviewsApi.listByGuide(id).catch(() => null))
        );
        const all = results
          .filter(Boolean)
          .flatMap((res) => res.data.reviews || [])
          .sort((a, b) => new Date(b.review_date) - new Date(a.review_date))
          .slice(0, 3);
        setReviews(all);
      })
      .catch(() => setReviews([]));
  }, []);

  if (reviews !== null && reviews.length === 0) return null;

  return (
    <section className="section section-tint" ref={ref}>
      <div className="shell" data-reveal>
        <h2 style={{ marginBottom: 30 }}>What travellers said</h2>

        {reviews === null ? (
          <div className="grid grid-3">
            {[0, 1, 2].map((i) => (
              <div className="card" key={i}>
                <div className="skeleton sk-line" style={{ width: '35%' }} />
                <div className="skeleton sk-line" style={{ width: '92%', marginTop: 14 }} />
                <div className="skeleton sk-line" style={{ width: '78%' }} />
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-3">
            {reviews.map((r) => (
              <figure className="card" key={r.review_id} style={{ margin: 0 }}>
                <Rating value={r.rating} />
                <blockquote className="quote">{r.comment}</blockquote>
                <figcaption className="quote-by">
                  <Avatar name={r.tourist_name} />
                  <span>
                    <b style={{ color: 'var(--ink)' }}>{r.tourist_name}</b>
                    <br />
                    {r.package_title}
                  </span>
                </figcaption>
              </figure>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

/* ------------------------------------------------------------------ page -- */
export default function Home() {
  return (
    <>
      <Hero />
      <Destinations />
      <FeaturedTours />
      <HowItWorks />
      <RecentReviews />
    </>
  );
}
