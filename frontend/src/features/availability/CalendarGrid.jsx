/**
 * FR-02 - month grid. Each day is coloured by its state:
 * AVAILABLE, BLOCKED, BOOKED or UNSET. Member: Ashfak M.A.M.
 */
const DOW = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export default function CalendarGrid({ days, selected = [], onToggleDay }) {
  if (!days?.length) return <p className="muted">No dates to show.</p>;

  // pad the first week so the 1st lands under the right weekday
  const firstDow = new Date(`${days[0].date}T00:00:00Z`).getUTCDay();
  const blanks = Array.from({ length: firstDow }, (_, i) => `blank-${i}`);

  return (
    <>
      <div className="calendar">
        {DOW.map((d) => <div className="dow" key={d}>{d}</div>)}
        {blanks.map((k) => <div className="cal-day blank" key={k} />)}
        {days.map((d) => {
          const isSelected = selected.includes(d.date);
          const locked = d.state === 'BOOKED';
          return (
            <button
              type="button"
              key={d.date}
              className={`cal-day ${d.state}${isSelected ? ' selected' : ''}`}
              disabled={locked}
              aria-pressed={isSelected}
              title={locked ? `Already booked (#${d.booking_id})` : d.note || d.state.toLowerCase()}
              onClick={() => !locked && onToggleDay && onToggleDay(d)}
            >
              <b>{Number(d.date.slice(8, 10))}</b>
              <small>{d.state === 'UNSET' ? '' : d.state.slice(0, 4)}</small>
            </button>
          );
        })}
      </div>

      <div className="legend">
        <span><i style={{ background: 'var(--good-a)', borderColor: 'var(--good)' }} /> Available</span>
        <span><i style={{ background: 'var(--bad-a)', borderColor: 'var(--bad)' }} /> Blocked</span>
        <span><i style={{ background: 'var(--info-a)', borderColor: 'var(--info)' }} /> Booked</span>
        <span><i style={{ background: 'var(--surface)' }} /> Not set</span>
      </div>
    </>
  );
}
