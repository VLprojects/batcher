#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

Rules the drawings follow:

- Show the thing, not a box with its name. A record is a small square, a batch
  is a bar, a container is a plate with slots, drawn to one scale everywhere.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting it out (draining, parked), red is a
  failure, grey is structure. In the module map green is Spring and blue is
  plain JVM. Text on amber is black.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
- One grid of 4 px. Space inside a group is smaller than space between groups.
- Two text sizes: the name in bold, the explanation in grey below it, both
  aligned to the left edge of what they label. Code identifiers are monospaced.
- All diagrams share one scale and are embedded at their native width.
"""

import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {
		"bg": "#FFFFFF", "ink": "#1F2328", "soft": "#59636E", "mute": "#8C959F",
		"plate": "#F2F4F6", "slot": "#DDE2E7", "line": "#AFB8C1",
		"blue": "#1F6FEB", "green": "#1A7F37", "amber": "#E5A000", "red": "#CF222E",
		"blue-plate": "#E8F0FE", "green-plate": "#E6F4EA", "amber-plate": "#FDF3D7", "red-plate": "#FDE8E8",
		"on-green": "#FFFFFF", "on-blue": "#FFFFFF", "on-amber": "#1F2328",
	},
	"dark": {
		"bg": "#0D1117", "ink": "#E6EDF3", "soft": "#9198A1", "mute": "#6E7681",
		"plate": "#161B22", "slot": "#2A313A", "line": "#4A525C",
		"blue": "#4493F8", "green": "#3FB950", "amber": "#E3A21A", "red": "#F85149",
		"blue-plate": "#0F213A", "green-plate": "#10261A", "amber-plate": "#2B210A", "red-plate": "#2E1416",
		"on-green": "#0D1117", "on-blue": "#0D1117", "on-amber": "#0D1117",
	},
}

NAME, NOTE, CODE = 14, 12.5, 13
REC, PITCH = 6, 8  # record square and its pitch
BAR_W, BAR_H, BAR_PITCH = 6, 28, 10  # batch bar


class Art:
	def __init__(self, width, height, pal):
		self.w, self.h, self.p, self.parts = width, height, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="start", weight="400", mono=False):
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

	def svg(self):
		body = "\n  ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" '
			f'width="{self.w}" height="{self.h}" role="img">\n  {body}\n</svg>\n'
		)


# ---------------------------------------------------------------------- 1
def pipeline(pal):
	a = Art(880, 248, pal)
	top, h, gap = 64, 60, 32
	mid = top + h / 2
	cols = [("producer", 112), ("record buffer", 216), ("queue", 184), ("writer thread", 96), ("ClickHouse", 112)]
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

	# producer: records arriving one by one; no plate, it is outside the batcher
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
		a.bar(ox + i * BAR_PITCH, mid - BAR_H / 2, "green")

	# failure: straight down from the writer to the fallback
	fy = top + h + 48
	a.plate(wx, fy, ww, h, "amber-plate")
	for i in range(3):
		a.record(wx + ww / 2 - 17 + i * 14, fy + h / 2 - REC / 2, "amber")
	wm = wx + ww / 2
	a.path(f"M{wm},{top + h + 8} L{wm},{fy - 14}", "red")
	a.add(f'<path d="M{wm - 4},{fy - 15} L{wm},{fy - 8} L{wm + 4},{fy - 15} Z" fill="{pal["red"]}"/>')
	a.t(wm - 12, top + h + 32, "write failed", color="red", weight="600", anchor="end")
	a.label(cx, fy + h / 2 - 4, "WriteFallback", "queue, file, log", mono=True)
	return a


# ---------------------------------------------------------------------- 2
def recovery(pal):
	# the batch is a grid of records; a red spine follows the one record that keeps failing
	a = Art(688, 308, pal)
	gx, h, step = 248, 44, 80
	ncol, nrow = 24, 3
	gw = 16 * 2 + ncol * PITCH - (PITCH - REC)
	ox = gx + gw + 24  # outcome column
	bc, br = 17, 1  # where the bad record sits in the grid
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
		("WriteFallback", "whatever the retry could not write", "1 record parked"),
		("lost", "the fallback failed as well", "ERROR in the log, a metric"),
	]
	for i, (title, note, outcome) in enumerate(rows):
		y = 16 + i * step
		last = i == len(rows) - 1
		a.label(16, y + 18, title, note, mono=not last, color="red" if last else "ink")
		a.t(ox, y + h / 2 + 4.5, outcome, color="red" if last else "ink", weight="600" if last else "400")

	y1, y2, y3, y4 = (16 + i * step for i in range(4))
	grid(y1, "blue", fill="blue")
	grid(y2, "red")
	a.plate(rx + REC / 2 - 22, y3, 44, h, "amber-plate")
	a.record(rx, y3 + h / 2 - REC / 2, "amber")
	a.record(rx, y4 + h / 2 - REC / 2, "red")
	down(y1 + h + 6, y2 - 6)
	down(y2 + h + 6, y3 - 6)
	down(y3 + h + 6, y4 + h / 2 - REC / 2 - 6)
	for y in (y1, y3):
		a.t(rx - 8, y + h + 22, "fails", color="red", anchor="end")
	return a


# ---------------------------------------------------------------------- 3
def lifecycle(pal):
	a = Art(880, 92, pal)
	top, h, gap = 44, 32, 4
	segments = [
		(112, "plate", "ink", "soft", "NEW", None),
		(328, "green", "on-green", "on-green", "RUNNING", "accept allowed"),
		(300, "amber", "on-amber", "on-amber", "STOPPING", "accept rejected, buffer drains"),
		(96, "plate", "ink", "soft", "STOPPED", None),
	]
	x, cuts = 16, []
	for w, fill, ink, soft, state, note in segments:
		a.rect(x, top, w, h, fill, r=4)
		a.t(x + 12, top + h / 2 + 4.5, state, color=ink, weight="650")
		if note:
			a.t(x + 12 + len(state) * 9.2 + 12, top + h / 2 + 4.5, note, color=soft)
		x += w + gap
		cuts.append(x - gap / 2)
	for cx, event in zip(cuts, ("start", "stop", "queue drained")):
		a.name(cx, 28, event, anchor="middle")
		a.path(f"M{cx},{34} L{cx},{top - 4}", "line")
	return a


# ---------------------------------------------------------------------- 4
def shutdown(pal):
	a = Art(880, 260, pal)
	lx = 120  # lanes start
	lane_h, buf_y, wr_y = 32, 64, 112
	stop, budget, end = 336, 712, 864

	a.name(lx - 16, buf_y + 21, "buffer", anchor="end")
	a.name(lx - 16, wr_y + 21, "writer", anchor="end")

	# buffer: accepts until stop, closed after it
	a.rect(lx, buf_y, stop - lx - 2, lane_h, "green", r=4)
	a.t(lx + 12, buf_y + 20.5, "accepts", color="on-green", weight="600")
	a.rect(stop + 2, buf_y, end - stop - 2, lane_h, "plate", r=4)
	a.t(stop + 14, buf_y + 20.5, "rejects", color="soft", weight="600")

	# writer: a batch now and then; after stop the remaining batch, then the queue back to back
	a.rect(lx, wr_y, end - lx, lane_h, "plate", r=4)
	by, bh = wr_y + 6, lane_h - 12
	for x in range(lx + 20, stop - 10, 34):
		a.bar(x, by, h=bh)
	rest = stop + 14
	a.bar(rest, by, h=bh)
	drain = rest + 3 * BAR_PITCH
	for x in range(drain, budget - 6, BAR_PITCH):
		a.bar(x, by, h=bh)
	for x in range(budget + 12, budget + 12 + 4 * BAR_PITCH, BAR_PITCH):
		a.bar(x, by, "amber", h=bh)

	a.path(f"M{stop},{buf_y - 8} L{stop},{wr_y + lane_h + 8}", "ink")
	a.label(stop - 12, 24, "stop arrives", "after the message listeners", anchor="end")
	a.label(stop + 12, 24, "buffer closes", "deadline thread cancelled")
	a.path(f"M{budget},{wr_y - 8} L{budget},{wr_y + lane_h + 8}", "ink")

	ly = wr_y + lane_h + 30
	a.label(rest - 2, ly, "remaining batch", "goes ahead of the queue")
	a.label(drain + 168, ly, "writer drains", "one batch at a time")
	a.label(budget + 12, ly, "leftovers parked", "WriteFallback", note_mono=True)

	by2 = ly + 40
	a.path(f"M{stop},{by2 - 6} L{stop},{by2} L{budget},{by2} L{budget},{by2 - 6}", "mute", width=1.25)
	a.t((stop + budget) / 2, by2 + 20, "shutdown-await-period", color="ink", mono=True, size=CODE, anchor="middle")
	return a


# ---------------------------------------------------------------------- 5
def modules(pal):
	# each layer depends on the ones below it; micrometer is optional for both kinds of service
	a = Art(584, 268, pal)
	x, w, h, gap = 16, 360, 56, 4
	rows = [
		("green-plate", "batcher-spring-boot-starter", "one dependency, nothing of its own"),
		("green-plate", "batcher-spring-boot", "auto-configuration, lifecycle"),
		("blue-plate", "batcher-micrometer", "metrics, optional"),
		("blue-plate", "batcher-core", "the engine and every interface, no Spring"),
	]
	for i, (fill, title, note) in enumerate(rows):
		y = 16 + i * (h + gap)
		a.plate(x, y, w, h, fill)
		a.label(x + 16, y + 24, title, note, mono=True)

	mx = x + w + 16

	def bracket(y1, y2, color):
		a.path(f"M{mx},{y1 + 2} L{mx + 6},{y1 + 2} L{mx + 6},{y2 - 2} L{mx},{y2 - 2}", color, width=2)

	bracket(16, 16 + h, "green")
	a.label(mx + 20, 16 + 24, "a Spring Boot service", "takes this one")
	y3 = 16 + 2 * (h + gap)
	bracket(y3, y3 + 2 * h + gap, "blue")
	a.label(mx + 20, y3 + h - 4, "a plain JVM service", "takes these two")
	return a


DIAGRAMS = {"pipeline": pipeline, "recovery": recovery, "lifecycle": lifecycle, "shutdown": shutdown, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
