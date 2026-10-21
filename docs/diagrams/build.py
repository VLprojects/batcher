#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

Rules the drawings follow:

- Show the thing, not a box with its name. A record is a small square, a batch
  is a bar, a container is a plate with slots, drawn to one scale everywhere.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting it out (draining, parked), red is a
  failure, grey is structure. On the dark canvas coloured plates are bright
  with dark text: dark orange, green or blue there turns muddy. States keep their colour wherever they appear:
  new light blue, running green, stopping amber, stopped slate. In the module
  map green is Spring and blue is plain JVM. Text on bright fills is dark.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
- One grid of 4 px. Space inside a group is smaller than space between groups.
- Two text sizes: the name in bold, the explanation in grey below it, both
  aligned to the left edge of what they label.
- Every part is named in plain lowercase words, types included (batch writer,
  write fallback). Only what one would type is monospaced: calls, artifacts,
  config keys. Product names keep their capitals.
- All diagrams share one scale and are embedded at their native width. Each
  sits on its own rounded surface, the colour of a GitHub code block, so it
  reads as one piece whatever the page behind it.
- README.md embeds each one twice, with #gh-light-mode-only and
  #gh-dark-mode-only: GitHub hides one by its own theme setting, while a
  <picture> media query would follow the OS instead.
"""

import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {
		"bg": "#F6F8FA", "ink": "#1F2328", "soft": "#59636E", "mute": "#8C959F",
		"plate": "#FFFFFF", "slot": "#DDE2E7", "line": "#AFB8C1",
		"wash": "#FFFFFF", "card": "#E3E8ED", "bar": "#BEC7D0",
		"blue": "#1A7CFF", "green": "#22C063", "amber": "#FF9F2E", "red": "#EE2D62",
		"sky": "#9DD1FF", "slate": "#7A8594", "lime": "#22C063",
		"blue-plate": "#E3EFFF", "blue-ink": "#1F2328", "green-plate": "#DDF7E7", "green-ink": "#1F2328", "green-glyph": "#22C063", "amber-plate": "#FFEEDC", "amber-ink": "#1F2328", "amber-glyph": "#FF9F2E",
		"green-soft": "#3A7550", "blue-soft": "#41679C", "amber-soft": "#8F4600", "green-text": "#12873F", "amber-text": "#B85300",
		"on-green": "#0B2915", "on-blue": "#FFFFFF", "on-amber": "#2E1700", "on-sky": "#0B2340", "on-slate": "#FFFFFF", "on-lime": "#0B2915",
	},
	"dark": {
		"bg": "#151B23", "ink": "#E6EDF3", "soft": "#9198A1", "mute": "#6E7681",
		"plate": "#1D242D", "slot": "#323B46", "line": "#515B66",
		"wash": "#1D242D", "card": "#29313C", "bar": "#454F5A",
		"blue": "#4493F8", "green": "#3FB950", "amber": "#FF9F4A", "red": "#FF4F7B",
		"sky": "#6E9BFF", "slate": "#545D68", "lime": "#5BF08A",
		"blue-plate": "#6E9BFF", "blue-ink": "#06173D", "green-plate": "#5BF08A", "green-ink": "#03240E", "green-glyph": "#0D7A32", "amber-plate": "#FFB46B", "amber-ink": "#1F1206", "amber-glyph": "#8A3F00",
		"green-soft": "#0D5A26", "blue-soft": "#1B3A80", "amber-soft": "#4A2808", "green-text": "#03240E", "amber-text": "#FF9F4A",
		"on-green": "#0D1117", "on-blue": "#0D1117", "on-amber": "#0D1117", "on-sky": "#06173D", "on-slate": "#E6EDF3", "on-lime": "#03240E",
	},
}

NAME, NOTE, CODE = 14, 12.5, 13
PAD = 12  # surface around the drawing, on top of its own 16 px margin
REC, PITCH = 6, 8  # record square and its pitch
BAR_W, BAR_H, BAR_PITCH = 6, 28, 10  # batch bar


class Art:
	def __init__(self, width, height, pal):
		self.w, self.h, self.p, self.parts = width, height, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="start", weight="400", mono=False):
		s = s.replace("<", "&lt;").replace(">", "&gt;")
		self.add(
			f'<text x="{x:.1f}" y="{y:.1f}" font-family="{MONO if mono else FONT}" font-size="{size}" '
			f'font-weight="{weight}" fill="{self.p[color]}" text-anchor="{anchor}">{s}</text>'
		)

	def name(self, x, y, s, anchor="start", color="ink", mono=False):
		self.t(x, y, s, size=CODE if mono else NAME, color=color, anchor=anchor, weight="600", mono=mono)

	def label(self, x, y, title, note=None, anchor="start", mono=False, color="ink", note_mono=False):
		"""Name with a grey note below; y is the baseline of the name."""
		self.name(x, y, title, anchor=anchor, color=color, mono=mono)
		if note:
			self.t(x, y + 18, note, anchor=anchor, mono=note_mono)

	# shapes -----------------------------------------------------------
	def rect(self, x, y, w, h, color, r=2):
		self.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" rx="{r}" fill="{self.p[color]}"/>')

	def plate(self, x, y, w, h, color="plate"):
		self.rect(x, y, w, h, color, r=6)

	def record(self, x, y, color="blue"):
		self.rect(x, y, REC, REC, color, r=1)

	def bar(self, x, y, color="blue", h=BAR_H):
		self.rect(x, y, BAR_W, h, color, r=1.5)

	def path(self, d, color="line", width=1.5):
		self.add(
			f'<path d="{d}" stroke="{self.p[color]}" stroke-width="{width}" fill="none" '
			f'stroke-linecap="round" stroke-linejoin="round"/>'
		)

	def arrow(self, x1, y, x2, color="line"):
		"""Horizontal arrow with a solid head that ends at x2."""
		self.path(f"M{x1},{y} L{x2 - 6},{y}", color)
		self.add(f'<path d="M{x2 - 7},{y - 4} L{x2},{y} L{x2 - 7},{y + 4} Z" fill="{self.p[color]}"/>')

	def harrow(self, x1, y, x2, color="line", width=1.5):
		"""Horizontal arrow from x1 to x2 in either direction, the head touching x2."""
		d = 1 if x2 > x1 else -1
		self.path(f"M{x1},{y} L{x2 - d * 6},{y}", color, width)
		self.add(f'<path d="M{x2 - d * 7},{y - 4} L{x2},{y} L{x2 - d * 7},{y + 4} Z" fill="{self.p[color]}"/>')

	def varrow(self, x, y1, y2, color="line", width=1.5):
		"""Vertical arrow pointing down to y2."""
		self.path(f"M{x},{y1} L{x},{y2 - 6}", color, width)
		self.add(f'<path d="M{x - 4},{y2 - 7} L{x},{y2} L{x + 4},{y2 - 7} Z" fill="{self.p[color]}"/>')

	def svg(self):
		w, h = self.w + 2 * PAD, self.h + 2 * PAD
		body = "\n    ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}" role="img">\n'
			f'  <rect width="{w}" height="{h}" rx="12" fill="{self.p["bg"]}"/>\n'
			f'  <g transform="translate({PAD},{PAD})">\n    {body}\n  </g>\n</svg>\n'
		)


def ink(hue):
	"""Title colour on a plate of this hue: dark on the bright ones."""
	return hue + "-ink" if hue in ("amber", "green", "blue") else "ink"


# ---------------------------------------------------------------------- 1
def pipeline(pal):
	a = Art(880, 248, pal)
	top, h, gap = 64, 60, 32
	mid = top + h / 2
	cols = [("consumer", 112), ("record buffer", 216), ("queue", 184), ("writer thread", 96), ("ClickHouse", 112)]
	xs, x = [], 16
	for _, w in cols:
		xs.append(x)
		x += w + gap
	(px, pw), (bx, bw), (qx, qw), (wx, ww), (cx, cw) = [(x, w) for x, (_, w) in zip(xs, cols)]
	notes = ["records, one at a time", "cut at 500 records or 1 second", "at most 16 batches", "one write at a time", "one part per write"]
	for x, (title, _), note in zip(xs, cols, notes):
		a.label(x, 26, title, note)
	for (x0, w0), x1 in zip([(px, pw), (bx, bw), (qx, qw), (wx, ww)], [bx, qx, wx, cx]):
		a.arrow(x0 + w0 + 8, mid, x1 - 8)

	# consumer: records arriving one by one; no plate, it is outside the batcher
	for i in range(5):
		a.record(px + i * 22, mid - REC / 2)

	# record buffer: slots for records, partly filled
	a.plate(bx, top, bw, h)
	ncol, nrow = 23, 3
	ox, oy = bx + 16, mid - (nrow * PITCH - (PITCH - REC)) / 2
	for c in range(ncol):
		for r in range(nrow):
			a.record(ox + c * PITCH, oy + r * PITCH, "blue" if c * nrow + r < 44 else "slot")

	# queue: 16 slots for batches, 3 waiting at the front
	a.plate(qx, top, qw, h)
	ox = qx + (qw - (16 * BAR_PITCH - (BAR_PITCH - BAR_W))) / 2
	for i in range(16):
		a.bar(ox + i * BAR_PITCH, mid - BAR_H / 2, "blue" if i >= 13 else "slot")

	# writer: room for one batch
	a.plate(wx, top, ww, h)
	a.bar(wx + ww / 2 - BAR_W / 2, mid - BAR_H / 2)

	# clickhouse: a part per write
	a.plate(cx, top, cw, h, "green-plate")
	ox = cx + (cw - (6 * BAR_PITCH - (BAR_PITCH - BAR_W))) / 2
	for i in range(6):
		a.bar(ox + i * BAR_PITCH, mid - BAR_H / 2, "green-glyph")

	# failure: straight down from the writer to the fallback
	fy = top + h + 48
	a.plate(wx, fy, ww, h, "amber-plate")
	for i in range(3):
		a.record(wx + ww / 2 - 17 + i * 14, fy + h / 2 - REC / 2, "amber-glyph")
	wm = wx + ww / 2
	a.path(f"M{wm},{top + h + 8} L{wm},{fy - 14}", "red")
	a.add(f'<path d="M{wm - 4},{fy - 15} L{wm},{fy - 8} L{wm + 4},{fy - 15} Z" fill="{pal["red"]}"/>')
	a.t(wm - 12, top + h + 32, "write failed", color="red", weight="600", anchor="end")
	a.label(cx, fy + h / 2 - 4, "write fallback", "queue, file, log")
	return a


# ---------------------------------------------------------------------- 2
def recovery(pal):
	# the batch is a grid of records; a red spine follows the one record that keeps failing
	a = Art(880, 308, pal)
	gx, h, step = 296, 44, 80
	ncol, nrow = 36, 3
	gw = 16 * 2 + ncol * PITCH - (PITCH - REC)
	ox = gx + gw + 24  # outcome column
	bc, br = 24, 1  # where the bad record sits in the grid
	rx = gx + 16 + bc * PITCH  # its x in every row

	def grid(y, bad, fill="green"):
		a.plate(gx, y, gw, h)
		oy = y + (h - (nrow * PITCH - (PITCH - REC))) / 2
		for c in range(ncol):
			for r in range(nrow):
				a.record(gx + 16 + c * PITCH, oy + r * PITCH, bad if (c, r) == (bc, br) else fill)

	def down(y1, y2):
		a.path(f"M{rx + REC / 2},{y1} L{rx + REC / 2},{y2 - 6}", "red")
		a.add(f'<path d="M{rx + REC / 2 - 4},{y2 - 7} L{rx + REC / 2},{y2} L{rx + REC / 2 + 4},{y2 - 7} Z" fill="{pal["red"]}"/>')

	rows = [
		("persist(batch)", "the whole batch, one write", "500 records written"),
		("persist(record)", "the same batch, record by record", "499 written, 1 still fails"),
		("write fallback", "whatever the retry could not write", "1 record parked"),
		("lost", "the fallback failed as well", "ERROR in the log, a metric"),
	]
	for i, (title, note, outcome) in enumerate(rows):
		y = 16 + i * step
		last = i == len(rows) - 1
		a.label(16, y + 18, title, note, mono="(" in title, color="red" if last else "ink")
		a.t(ox, y + h / 2 + 4.5, outcome, color="red" if last else "ink", weight="600" if last else "400")

	y1, y2, y3, y4 = (16 + i * step for i in range(4))
	grid(y1, "blue", fill="blue")
	grid(y2, "red")
	a.plate(rx + REC / 2 - 22, y3, 44, h, "amber-plate")
	a.record(rx, y3 + h / 2 - REC / 2, "amber-glyph")
	a.record(rx, y4 + h / 2 - REC / 2, "red")
	down(y1 + h + 6, y2 - 6)
	down(y2 + h + 6, y3 - 6)
	down(y3 + h + 6, y4 + h / 2 - REC / 2 - 6)
	for y in (y1, y3):
		a.t(rx - 8, y + h + 22, "fails", color="red", anchor="end")
	return a


# ---------------------------------------------------------------------- 3
STATES = {  # state: fill, text
	"new": ("sky", "on-sky"),
	"running": ("lime", "on-lime"),  # the green of the plates, not of the lines
	"stopping": ("amber", "on-amber"),
	"stopped": ("slate", "on-slate"),
}


def state_bar(a, x, y, w, h, state, note=None):
	fill, ink = STATES[state]
	a.rect(x, y, w, h, fill, r=4)
	a.t(x + 12, y + h / 2 + 4.5, state, color=ink, weight="650")
	if note:
		a.t(x + 12 + len(state) * 8 + 12, y + h / 2 + 4.5, note, color=ink)


def lifecycle(pal):
	a = Art(880, 92, pal)
	top, h, gap = 44, 32, 4
	segments = [
		(112, "new", None),
		(328, "running", "accept allowed"),
		(300, "stopping", "accept rejected, buffer drains"),
		(96, "stopped", None),
	]
	x, cuts = 16, []
	for w, state, note in segments:
		state_bar(a, x, top, w, h, state, note)
		x += w + gap
		cuts.append(x - gap / 2)
	for cx, event in zip(cuts, ("start", "stop", "queue drained")):
		a.name(cx, 28, event, anchor="middle")
		a.path(f"M{cx},{34} L{cx},{top - 4}", "line")
	return a


# ---------------------------------------------------------------------- 4
def shutdown(pal):
	# the state strip on top is the lifecycle bar; the lanes below show what each thread does meanwhile
	a = Art(880, 300, pal)
	lx = 112  # lanes start
	st_y, st_h = 64, 28
	lane_h, buf_y, wr_y = 40, 100, 144
	stop, budget, done, end = 336, 696, 776, 864
	bottom = wr_y + lane_h

	state_bar(a, lx, st_y, stop - lx - 2, st_h, "running")
	state_bar(a, stop + 2, st_y, done - stop - 4, st_h, "stopping")
	state_bar(a, done + 2, st_y, end - done - 2, st_h, "stopped")

	a.name(lx - 16, buf_y + 25, "buffer", anchor="end")
	a.name(lx - 16, wr_y + 25, "writer", anchor="end")
	a.plate(lx, buf_y, end - lx, lane_h)
	a.plate(lx, wr_y, end - lx, lane_h)

	# buffer: records keep arriving until stop, nothing after
	for x in range(lx + 20, stop - 12, 22):
		a.record(x, buf_y + lane_h / 2 - REC / 2)
	a.t(stop + 16, buf_y + 24.5, "new records rejected", color="soft")

	# writer: a batch now and then; after stop the queue back to back, the buffer's last batch at its tail
	by, bh = wr_y + 8, lane_h - 16
	for x in range(lx + 24, stop - 10, 36):
		a.bar(x, by, h=bh)
	rest = stop + 16
	for x in range(rest, budget - 8, BAR_PITCH):
		a.bar(x, by, h=bh)
	for x in range(budget + 16, budget + 16 + 5 * BAR_PITCH, BAR_PITCH):
		a.bar(x, by, "amber", h=bh)

	a.path(f"M{stop},{st_y - 8} L{stop},{bottom + 8}", "ink")
	a.path(f"M{budget},{wr_y - 8} L{budget},{bottom + 8}", "ink")
	a.label(stop - 12, 24, "stop arrives", "after the message listeners", anchor="end")
	a.label(stop + 12, 24, "buffer closes", "its last batch joins the queue")

	ly = bottom + 30
	a.label(rest - 4, ly, "writer drains the queue", "one batch at a time, while the budget lasts")
	a.label(budget + 12, ly, "leftovers parked", "in the write fallback")

	by2 = ly + 40
	a.path(f"M{stop},{by2 - 6} L{stop},{by2} L{budget},{by2} L{budget},{by2 - 6}", "mute", width=1.25)
	a.t((stop + budget) / 2, by2 + 20, "shutdown-await-period", color="ink", mono=True, size=CODE, anchor="middle")
	return a


# ---------------------------------------------------------------------- 5
def modules(pal):
	# the group is one container: core underneath, the starter on top; a service on the left points
	# at what it puts in its build
	a = Art(880, 268, pal)
	cx, cy, cw, ch = 208, 16, 656, 236
	a.rect(cx, cy, cw, ch, "plate", r=14)  # outer radius = inner radius + padding
	a.t(cx + 16, cy + 26, "group", color="soft")
	a.name(cx + 60, cy + 26, "pro.vlprojects", mono=True)

	ix, iw, h, gap = cx + 12, cw - 24, 56, 8
	half = (iw - gap) / 2
	y1 = cy + 40
	y2, y3 = y1 + h + gap, y1 + 2 * (h + gap)

	def card(x, y, w, hue, title, note):
		a.rect(x, y, w, h, hue + "-plate", r=6)
		a.name(x + 16, y + 24, title, mono=True, color=ink(hue))
		a.t(x + 16, y + 42, note, color=hue + "-soft")

	card(ix, y1, iw, "green", "batcher-spring-boot-starter", "one dependency, nothing of its own")
	card(ix, y2, half, "blue", "batcher-micrometer", "metrics, optional")
	card(ix + half + gap, y2, half, "green", "batcher-spring-boot", "auto-configuration, lifecycle")
	card(ix, y3, iw, "blue", "batcher-core", "the engine and every interface, no Spring")

	def chip(y, fill, text, note):
		a.rect(16, y - 16, 160, 32, fill, r=16)
		a.t(32, y + 4.5, text, color="on-" + fill, weight="650")
		a.t(32, y + 36, note)

	def head(x, y, color):
		x -= 2
		a.add(f'<path d="M{x - 7},{y - 4} L{x},{y} L{x - 7},{y + 4} Z" fill="{pal[color]}"/>')

	c1, c2, c3 = y1 + h / 2, y2 + h / 2, y3 + h / 2
	chip(c1, "green", "Spring Boot service", "gets all four through it")
	a.path(f"M176,{c1} L{ix - 8},{c1}", "green", width=2)
	head(ix, c1, "green")

	cj, fx, r = (c2 + c3) / 2, 192, 8
	chip(cj, "blue", "plain JVM service", "picks what it needs")
	a.path(f"M176,{cj} L{fx},{cj}", "blue", width=2)
	for cy_ in (c2, c3):
		sign = -1 if cy_ < cj else 1
		a.path(f"M{fx},{cj} L{fx},{cy_ - sign * r} Q{fx},{cy_} {fx + r},{cy_} L{ix - 8},{cy_}", "blue", width=2)
		head(ix, cy_, "blue")
	return a


# ---------------------------------------------------------------------- 6
def types(pal):
	# the pipeline again, as types: the same left-to-right chain, and under each type what it asks or
	# where its failures go; what travels between types is drawn as the thing itself
	a = Art(880, 316, pal)
	ws, gap, h = (156, 204, 196, 172), 40, 76
	xs, x = [], 16
	for w in ws:
		xs.append(x)
		x += w + gap
	top, low = 16, 16 + h + 48

	def card(i, y, title, lines, hue=None, tag=None):
		x, w = xs[i], ws[i]
		a.rect(x, y, w, h, (hue + "-plate") if hue else ("card" if y == top else "wash"), r=6)
		a.name(x + 16, y + 26, title, color=ink(hue))
		if tag:
			a.t(x + w - 16, y + 26, tag, color=hue + "-text", weight="600", anchor="end")
		for j, (line, mono) in enumerate(lines):
			a.t(x + 16, y + 48 + j * 18, line, color=(hue + "-soft") if hue else "soft", mono=mono, size=12 if mono else NOTE)

	card(0, top, "batcher", [("accept(record)", True), ("start(), stop()", True)])
	card(1, top, "record buffer", [("holds records and", False), ("cuts them into batches", False)])
	card(2, top, "writing batch queue", [("put(batch)", True), ("take(), confirm()", True)])
	card(3, top, "batch writer", [("persist(batch)", True), ("persist(record)", True)], "green", "yours")

	card(0, low, "batch", [("records, an id and the", False), ("first record's arrival", False)])
	a.bar(xs[0] + ws[0] - 16 - BAR_W, low + 13, h=16)
	card(1, low, "batch ready listener", [("takes each cut batch;", False), ("default: the batcher", False)])
	card(2, low, "put result", [("in queue, queue full", False), ("or queue closed", False)])
	card(3, low, "write fallback", [("what was not written,", False), ("and why", False)], "amber")

	# along the chain: an arrow and what it carries, centred in the gap
	mid = top + h / 2
	for i, glyph in enumerate(("records", "batch", "batch")):
		x1, x2 = xs[i] + ws[i], xs[i + 1]
		cx = (x1 + x2) / 2
		a.harrow(x1 + 8, mid + 8, x2 - 8)
		if glyph == "records":
			for k in (-1, 0, 1):
				a.record(cx + k * 8 - REC / 2, mid - REC)
		else:
			a.bar(cx - BAR_W / 2, mid - 16, h=16)

	# down from each type: what it hands out, or where a failed write goes
	for i, text, color in ((1, "hands to", "line"), (2, "returns", "line"), (3, "write failed", "red")):
		cx = xs[i] + ws[i] / 2
		a.varrow(cx, top + h + 8, low - 8, color)
		a.t(cx - 12, top + h + 28.5, text, color="red" if color == "red" else "soft", weight="600" if color == "red" else "400", anchor="end")

	y = low + h + gap
	a.rect(16, y, 848, 44, "wash", r=6)
	a.name(32, y + 27, "meters")
	a.t(xs[1] + 16, y + 27, "every type reports here")
	a.t(848, y + 27, "batcher-micrometer", mono=True, size=12, anchor="end")
	a.t(848 - 18 * 7.2 - 8, y + 27, "Micrometer is in", anchor="end")
	return a


# ---------------------------------------------------------------------- 7
def sequence(pal):
	# sequence diagram: the consumer thread fills a batch, the buffer thread cuts one by age, the writer thread
	# writes a batch that fails once; each thread is a plate over the lifelines it uses, the queue belongs to none
	a = Art(880, 504, pal)
	parts = [
		("batcher", 76, "card"), ("record buffer", 116, "card"), ("queue", 72, "card"),
		("recovering writer", 144, "card"), ("batch writer", 108, "green-plate"),
		("write fallback", 124, "amber-plate"),
	]
	xs, x = [], 156
	for i, (_, w, _) in enumerate(parts):
		if i:
			x += parts[i - 1][1] / 2 + 21 + w / 2
		xs.append(x)
	BT, RB, Q, W, BW, WF = xs
	P, end = 32, 488  # the consumer is the thread itself: its calls start at the plate's edge
	pr, wr = Q - 20, Q + 20  # the thread plates stop short of the queue

	def region(x1, y1, x2, y2, text):
		a.rect(x1, y1, x2 - x1, y2 - y1, "wash", r=8)
		a.t(x1 + 16, y1 + 24, text, color="soft", weight="600")

	region(16, 68, pr, 244, "consumer thread")
	region(16, 260, pr, 372, "buffer thread")
	region(wr, 188, 864, end, "writer thread")

	# a lifeline ends with its thread's plate
	for x, y2 in zip(xs, (244, 372, 400, end, end, end)):
		a.path(f"M{x},52 L{x},{y2}", "slot", width=1.5)
	for x, (name, w, fill) in zip(xs, parts):
		a.rect(x - w / 2, 16, w, 36, fill, r=6)
		a.name(x, 39, name, anchor="middle", color=ink(fill.split("-")[0]))

	# activation is grey structure; where something real is held, the bar is the thing itself:
	# the batch in the queue and in the writer, the records written one by one, the record parked
	for x, y1, y2 in ((BT, 112, 228), (RB, 148, 204), (RB, 300, 356), (W, 236, 472)):
		a.rect(x - 4, y1, 8, y2 - y1, "bar", r=2)
	a.bar(Q - BAR_W / 2, 190, h=66)
	a.bar(Q - BAR_W / 2, 334, h=66)  # waits for the writer to finish the first one
	a.bar(BW - BAR_W / 2, 270, h=52)
	for i in range(11):
		a.record(BW - REC / 2, 343 + i * PITCH, "red" if i == 9 else "green")
	a.record(WF - REC / 2, 459, "amber")

	def msg(x1, x2, y, label, color="ink", mono=False, edge=5, tx=None):
		d = 1 if x2 > x1 else -1
		a.harrow(x1 + d * edge, y, x2 - d * 5, color)
		tx = tx or x1 + d * 12
		text = {"ink": "ink", "line": "soft", "amber": "amber-text"}.get(color, color)
		a.t(tx, y - 9, label, color=text, mono=mono, size=12 if mono else NOTE, anchor="start" if d > 0 else "end")

	for y in (132, 316):
		a.add(f'<circle cx="{P}" cy="{y}" r="3.5" fill="{pal["ink"]}"/>')
	msg(P, BT, 132, "accept(record)", mono=True, edge=0)
	msg(BT, RB, 164, "append")
	msg(RB, Q, 196, "a full batch", "blue")
	msg(BT, P, 220, "returns, no I/O", "line")

	msg(P, RB, 316, "max age reached", edge=0)
	msg(RB, Q, 340, "a short batch", "blue")

	msg(Q, W, 252, "next batch", "blue", tx=wr + 16)
	msg(W, BW, 284, "persist(batch)", mono=True)
	msg(BW, W, 316, "throws", "red")

	# the retry: a bracket and a count instead of a loop frame
	a.rect(W - 18, 340, 2, 88, "line", r=1)
	a.t(W - 28, 380, "for each of", anchor="end")
	a.t(W - 28, 398, "500 records", anchor="end")
	msg(W, BW, 356, "persist(record)", mono=True)
	msg(BW, W, 388, "499 written", "line")
	msg(BW, W, 420, "1 throws", "red")

	msg(W, WF, 464, "the record that failed", "amber")
	return a


DIAGRAMS = {
	"pipeline": pipeline, "recovery": recovery, "lifecycle": lifecycle, "shutdown": shutdown, "modules": modules,
	"types": types, "sequence": sequence,
}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
