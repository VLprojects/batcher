#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

Rules the drawings follow:

- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting it out (draining, parked), red is a failure,
  grey is inactive. In the module map green is Spring and blue is plain JVM.
- The path a thing takes is a 3 px line with rounded caps. It runs at 0, 45 or
  90 degrees, and its corners are rounded: three line widths, half as much again
  on a 45-degree turn. Every line is solid; a lighter colour, never a dash,
  marks what is inactive.
- A point on the path is a white circle with a coloured ring. Records are
  ticks, batches are blocks, drawn to the same scale everywhere.
- Structure stays in the background: hairlines, grey small caps.
- Labels sit next to what they label and never cross a line: the name in bold,
  the explanation below it in grey. Code identifiers are monospaced.
- All diagrams share one scale and are embedded at their native width.
"""

import math
import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

# bg matches GitHub's light and dark canvas, so a station reads as a hole in its line.
PALETTE = {
	"light": {
		"bg": "#FFFFFF", "ink": "#1F2328", "soft": "#59636E", "mute": "#8C959F", "hair": "#D1D9E0",
		"blue": "#1F6FEB", "green": "#1A7F37", "amber": "#D18A00", "red": "#CF222E", "grey": "#BCC4CC",
		"amber-text": "#9A6700",
	},
	"dark": {
		"bg": "#0D1117", "ink": "#E6EDF3", "soft": "#9198A1", "mute": "#6E7681", "hair": "#30363D",
		"blue": "#4493F8", "green": "#3FB950", "amber": "#E3A21A", "red": "#F85149", "grey": "#4A525C",
		"amber-text": "#E3A21A",
	},
}

LW = 3  # line width
R90, R45 = 3 * LW, 4.5 * LW  # corner radius on 90- and 45-degree turns
STOP, RING = 5.5, 2.25  # station: outer radius and ring
NAME, NOTE, CODE, CAPS = 13.5, 12, 13, 10.5


def rounded(points):
	"""Polyline with every inner corner replaced by an arc."""
	d = [f"M{points[0][0]:.2f},{points[0][1]:.2f}"]
	for (x0, y0), (x1, y1), (x2, y2) in zip(points, points[1:], points[2:]):
		ax, ay, bx, by = x1 - x0, y1 - y0, x2 - x1, y2 - y1
		la, lb = math.hypot(ax, ay), math.hypot(bx, by)
		ux, uy, vx, vy = ax / la, ay / la, bx / lb, by / lb
		cos = max(-1.0, min(1.0, ux * vx + uy * vy))
		r = R45 if cos > 0.5 else R90
		t = r * math.tan(math.acos(cos) / 2)
		sweep = 1 if ux * vy - uy * vx > 0 else 0
		d.append(f"L{x1 - ux * t:.2f},{y1 - uy * t:.2f}")
		d.append(f"A{r},{r} 0 0 {sweep} {x1 + vx * t:.2f},{y1 + vy * t:.2f}")
	d.append(f"L{points[-1][0]:.2f},{points[-1][1]:.2f}")
	return " ".join(d)


class Art:
	def __init__(self, width, height, pal, top=0):
		self.w, self.h, self.top, self.p, self.parts = width, height, top, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="middle", weight="400", mono=False, spacing=None):
		gap = f' letter-spacing="{spacing}"' if spacing else ""
		self.add(
			f'<text x="{x:.1f}" y="{y:.1f}" font-family="{MONO if mono else FONT}" font-size="{size}" '
			f'font-weight="{weight}" fill="{self.p[color]}" text-anchor="{anchor}"{gap}>{s}</text>'
		)

	def name(self, x, y, s, anchor="middle", color="ink", mono=False):
		self.t(x, y, s, size=CODE if mono else NAME, color=color, anchor=anchor, weight="650", mono=mono)

	def caps(self, x, y, s, anchor="middle", color="soft"):
		self.t(x, y, s.upper(), size=CAPS, color=color, anchor=anchor, weight="700", spacing="0.08em")

	def label(self, x, y, title, note=None, anchor="middle", mono=False, note_mono=False):
		"""Bold name with a grey note under it; y is the baseline of the name."""
		self.name(x, y, title, anchor=anchor, mono=mono)
		if note:
			self.t(x, y + 17, note, anchor=anchor, mono=note_mono)

	# marks ------------------------------------------------------------
	def line(self, points, color, width=LW):
		self.add(
			f'<path d="{rounded(points)}" stroke="{self.p[color]}" stroke-width="{width}" fill="none" '
			f'stroke-linecap="round" stroke-linejoin="round"/>'
		)

	def hair(self, x1, y1, x2, y2):
		self.add(f'<path d="M{x1},{y1} L{x2},{y2}" stroke="{self.p["hair"]}" stroke-width="1"/>')

	def station(self, x, y, color="ink"):
		self.add(
			f'<circle cx="{x}" cy="{y}" r="{STOP - RING / 2}" fill="{self.p["bg"]}" '
			f'stroke="{self.p[color]}" stroke-width="{RING}"/>'
		)

	def end(self, x, y, color):
		"""The end of a path: a solid dot."""
		self.add(f'<circle cx="{x}" cy="{y}" r="{STOP - 1}" fill="{self.p[color]}"/>')

	def tip(self, x, y, color):
		self.add(
			f'<path d="M{x - 7},{y - 5} L{x},{y} L{x - 7},{y + 5}" stroke="{self.p[color]}" stroke-width="{LW}" '
			f'fill="none" stroke-linecap="round" stroke-linejoin="round"/>'
		)

	def tick(self, x, base, height, color="blue"):
		self.add(
			f'<path d="M{x},{base} L{x},{base - height}" stroke="{self.p[color]}" stroke-width="1.6"/>'
		)

	def block(self, x, base, w, h, color="blue"):
		self.add(f'<rect x="{x}" y="{base - h}" width="{w}" height="{h}" rx="2" fill="{self.p[color]}"/>')

	def bracket(self, x, y1, y2, color, reach=8):
		"""A coloured bracket on the right of a group of rows."""
		self.line([(x - reach, y1), (x, y1), (x, y2), (x - reach, y2)], color)

	def brace(self, x1, x2, y, depth=6):
		self.add(
			f'<path d="M{x1},{y - depth} L{x1},{y} L{x2},{y} L{x2},{y - depth}" stroke="{self.p["mute"]}" '
			f'stroke-width="1.25" fill="none"/>'
		)

	def svg(self):
		body = "\n  ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 {self.top} {self.w} {self.h}" '
			f'width="{self.w}" height="{self.h}" role="img">\n  {body}\n</svg>\n'
		)


# ---------------------------------------------------------------------- 1
def pipeline(pal):
	a = Art(880, 222, pal, top=22)
	y = 120
	zones = [(24, "producer"), (196, "record buffer"), (420, "queue"), (580, "writer thread"), (724, "clickhouse")]
	for x, title in zones:
		a.caps(x + (0 if x == 24 else 12), 44, title, anchor="start")
		if x > 24:
			a.hair(x, 32, x, y + 12)
	a.line([(24, y), (856, y)], "blue")
	a.tip(858, y, "blue")

	for i in range(7):
		a.tick(44 + i * 20, y - 6, 14)
	a.t(104, y - 32, "records, one at a time")

	for i in range(24):
		a.tick(214 + i * 6.5, y - 6, 14)
	a.brace(214, 364, y - 26, depth=-5)
	a.t(289, y - 36, "500 of them, or one second")
	a.add(f'<path d="M392,{y - 3} L392,{y - 30}" stroke="{pal["ink"]}" stroke-width="2"/>')
	a.t(392, y - 38, "cut", color="ink")

	for i in range(3):
		a.block(446 + i * 40, y - 6, 28, 14)
	a.t(500, y - 32, "batches wait, at most 16")

	writer = 652
	a.block(writer - 14, y - 6, 28, 14)
	a.t(writer, y - 32, "one write at a time", color="ink")

	a.block(772, y - 6, 40, 14, color="green")
	a.t(792, y - 32, "one part per write")

	low = y + 64
	a.line([(writer, y), (writer, y + 20), (writer + 44, low), (742, low)], "red")
	a.station(748, low, "amber")
	a.t(writer - 12, y + 40, "write failed", color="red", anchor="end", weight="600")
	a.label(764, low + 5, "WriteFallback", "queue, file, log", anchor="start", mono=True)
	return a


# ---------------------------------------------------------------------- 2
def recovery(pal):
	a = Art(720, 360, pal, top=0)
	x, end, step, drop = 268, 520, 80, 32
	ys = [36, 36 + step, 36 + 2 * step]
	last = ys[2] + step

	a.line([(x, ys[0]), (x, last), (x + drop, last + drop), (end, last + drop)], "red")
	# each step forks: what was written leaves to the right, the rest goes on down
	outcomes = [(ys[0], "green", "500 records written"), (ys[1], "green", "499 written"), (ys[2], "amber", "1 record parked")]
	for y0, color, outcome in outcomes:
		a.line([(x, y0), (x + drop, y0 + drop), (end, y0 + drop)], color)
		a.end(end, y0 + drop, color)
		a.name(end + 16, y0 + drop + 5, outcome, anchor="start")
	a.end(end, last + drop, "red")
	a.label(end + 16, last + drop + 5, "lost", "ERROR in the log, a metric", anchor="start")

	for y0, failed in zip(ys, ("fails", "1 still fails", "fails")):
		a.t(x + 12, y0 + drop + 30, failed, color="red", anchor="start", weight="600")

	rows = [
		("persist(batch)", "the whole batch, one write"),
		("persist(record)", "the same batch, record by record"),
		("WriteFallback", "whatever the retry could not write"),
	]
	for y0, (title, note) in zip(ys, rows):
		a.station(x, y0)
		a.label(x - 20, y0 + 1, title, note, anchor="end", mono=True)
	return a


# ---------------------------------------------------------------------- 3
def lifecycle(pal):
	a = Art(880, 100, pal, top=12)
	y = 78
	x0, start, stop, drained, x1 = 32, 220, 470, 760, 856
	states = [
		(x0, start, "grey", "NEW", None),
		(start, stop, "green", "RUNNING", "accept allowed"),
		(stop, drained, "amber", "STOPPING", "accept rejected, buffer drains"),
		(drained, x1, "grey", "STOPPED", None),
	]
	for left, right, color, state, note in states:
		a.line([(left, y), (right, y)], color)
		middle = (left + right) / 2
		text = {"grey": "soft", "amber": "amber-text"}.get(color, color)
		a.caps(middle, y - 34, state, color=text)
		if note:
			a.t(middle, y - 16, note)
	for x, event in ((start, "start"), (stop, "stop"), (drained, "queue drained")):
		a.station(x, y)
		a.t(x, y + 26, event, color="ink", weight="650")
	return a


# ---------------------------------------------------------------------- 4
def shutdown(pal):
	a = Art(880, 196, pal, top=34)
	y = 120
	arrives, closes, remaining, drains, budget, parked = 104, 284, 464, 644, 728, 808
	a.line([(24, y), (arrives, y)], "green")
	a.line([(arrives, y), (budget, y)], "amber")
	a.line([(budget, y), (parked, y)], "red")
	steps = [
		(arrives, "stop arrives", "after the message listeners", "up"),
		(closes, "buffer closes", "deadline thread cancelled", "down"),
		(remaining, "remaining batch", "goes ahead of the queue", "up"),
		(drains, "writer drains", "one batch at a time", "down"),
	]
	for x, title, note, side in steps:
		a.station(x, y, "ink" if x == arrives else "amber")
		a.label(x, y - 36 if side == "up" else y + 30, title, note)
	a.station(parked, y, "amber")
	a.label(parked, y - 36, "leftovers parked", "WriteFallback", note_mono=True)

	a.brace(closes, budget, y + 78)
	a.t((closes + budget) / 2, y + 100, "shutdown-await-period", color="ink", mono=True, size=CODE)
	return a


# ---------------------------------------------------------------------- 5
def modules(pal):
	a = Art(600, 270, pal, top=0)
	x, kid, text, margin = 36, 64, 82, 432
	rows = [
		(40, "batcher-spring-boot-starter", "one dependency, nothing of its own"),
		(110, "batcher-spring-boot", "auto-configuration, properties, lifecycle"),
		(170, "batcher-micrometer", "metrics, optional"),
		(230, "batcher-core", "the engine and every interface, no Spring"),
	]
	mid = [y - 4.5 for y, *_ in rows]
	for m in mid[1:]:
		a.line([(x, mid[0]), (x, m), (kid, m)], "green")
	a.station(x, mid[0], "green")
	for m in mid[1:]:
		a.station(kid, m, "green")

	a.name(x + 18, rows[0][0], rows[0][1], anchor="start", mono=True)
	a.t(x + 18, rows[0][0] + 17, rows[0][2], anchor="start")
	for y, title, note in rows[1:]:
		a.label(text, y, title, note, anchor="start", mono=True)

	a.bracket(margin, rows[0][0] - 12, rows[0][0] + 21, "green")
	a.label(margin + 16, rows[0][0], "a Spring Boot service", "takes this one", anchor="start")
	a.bracket(margin, rows[2][0] - 12, rows[3][0] + 21, "blue")
	a.label(margin + 16, (rows[2][0] + rows[3][0]) / 2 + 2, "a plain JVM service", "takes these two", anchor="start")
	return a


DIAGRAMS = {"pipeline": pipeline, "recovery": recovery, "lifecycle": lifecycle, "shutdown": shutdown, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
