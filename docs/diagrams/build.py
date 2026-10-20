#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

The drawings borrow the language of a metro scheme:

- A thread is a line of its own colour: blue for the caller, amber for the
  deadline thread, green for the writer and for records written, red for the
  failure path. Light grey is a state where nothing runs, dark grey records set
  aside. Modules reuse green and blue for the two kinds of service.
- Lines are 6 px thick and run at 0, 45 and 90 degrees. Corners are rounded
  with a radius of three line widths, half as much again on 45-degree turns.
  Lines in a bundle keep a gap of one line width and concentric corners.
- A plain stop is a stub on the side of its label. A hand-off between lines is
  a white circle or capsule with a dark outline; a stop on one line is a white
  circle in the line's colour. A line ends in a bar, a station, or a badge
  with its name.
- Every line is solid: a lighter colour, never a dash, marks what is inactive.
- Labels sit next to their stop and never cross a line: the name in bold, the
  explanation below it in grey. Code identifiers are monospaced.
- All diagrams share one scale and are embedded at their native width.
"""

import math
import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

# bg must match the page: GitHub's light and dark canvas, so stations read as holes in the line.
PALETTE = {
	"light": {
		"bg": "#FFFFFF", "ink": "#1F2328", "soft": "#656D76", "rule": "#8C959F",
		"blue": "#1665D8", "amber": "#F2A100", "green": "#1A9850", "red": "#E5432A", "grey": "#CDD3DA",
		"on": {"blue": "#FFFFFF", "amber": "#1F2328", "green": "#FFFFFF", "red": "#FFFFFF", "grey": "#1F2328"},
	},
	"dark": {
		"bg": "#0D1117", "ink": "#E6EDF3", "soft": "#8D96A0", "rule": "#6E7681",
		"blue": "#4C9AFF", "amber": "#F5B73B", "green": "#3FB96B", "red": "#FF6A4D", "grey": "#3A424C",
		"on": {"blue": "#0D1117", "amber": "#0D1117", "green": "#0D1117", "red": "#0D1117", "grey": "#E6EDF3"},
	},
}

LW = 6  # line width
PITCH = 12  # distance between line centres in a bundle
R90, R45 = 3 * LW, 4.5 * LW  # corner radius for 90- and 45-degree turns
STOP = 8.5  # outer radius of a circle or capsule station
RING = 3  # its outline
NAME, NOTE, CODE, BADGE = 14, 12.5, 13, 11.5


def width_of(s, size, bold=True, mono=False):
	"""Rough advance width of a string, good enough to size badges and keep labels apart."""
	if mono:
		return len(s) * size * 0.6
	narrow, wide = set("ijlI.,:;'!|() "), set("mwMW")
	k = 0.0
	for ch in s:
		k += 0.3 if ch in narrow else 0.86 if ch in wide else 0.66 if ch.isupper() else 0.56
	return k * size * (1.04 if bold else 1.0)


def rounded(points, radii):
	"""Polyline with each inner corner replaced by an arc of the given radius."""
	d = [f"M{points[0][0]:.2f},{points[0][1]:.2f}"]
	for i in range(1, len(points) - 1):
		(x0, y0), (x1, y1), (x2, y2) = points[i - 1], points[i], points[i + 1]
		ax, ay, bx, by = x1 - x0, y1 - y0, x2 - x1, y2 - y1
		la, lb = math.hypot(ax, ay), math.hypot(bx, by)
		ux, uy, vx, vy = ax / la, ay / la, bx / lb, by / lb
		turn = math.acos(max(-1.0, min(1.0, ux * vx + uy * vy)))
		r = radii[i - 1]
		t = r * math.tan(turn / 2)
		sweep = 1 if ux * vy - uy * vx > 0 else 0
		d.append(f"L{x1 - ux * t:.2f},{y1 - uy * t:.2f}")
		d.append(f"A{r:.2f},{r:.2f} 0 0 {sweep} {x1 + vx * t:.2f},{y1 + vy * t:.2f}")
	d.append(f"L{points[-1][0]:.2f},{points[-1][1]:.2f}")
	return " ".join(d)


def offset(points, dist):
	"""The same polyline moved sideways by dist, positive to the left on screen; corner radii adjusted."""
	segs = []
	for (x0, y0), (x1, y1) in zip(points, points[1:]):
		length = math.hypot(x1 - x0, y1 - y0)
		nx, ny = (y1 - y0) / length, -(x1 - x0) / length
		segs.append(((x0 + nx * dist, y0 + ny * dist), (x1 + nx * dist, y1 + ny * dist)))
	out = [segs[0][0]]
	for (p0, p1), (q0, q1) in zip(segs, segs[1:]):
		dx1, dy1, dx2, dy2 = p1[0] - p0[0], p1[1] - p0[1], q1[0] - q0[0], q1[1] - q0[1]
		den = dx1 * dy2 - dy1 * dx2
		s = ((q0[0] - p0[0]) * dy2 - (q0[1] - p0[1]) * dx2) / den
		out.append((p0[0] + dx1 * s, p0[1] + dy1 * s))
	out.append(segs[-1][1])
	return out


def natural_radii(points, dist=0.0):
	radii = []
	for i in range(1, len(points) - 1):
		(x0, y0), (x1, y1), (x2, y2) = points[i - 1], points[i], points[i + 1]
		ax, ay, bx, by = x1 - x0, y1 - y0, x2 - x1, y2 - y1
		cos = (ax * bx + ay * by) / (math.hypot(ax, ay) * math.hypot(bx, by))
		base = R45 if cos > 0.5 else R90
		radii.append(base + dist * (1 if ax * by - ay * bx > 0 else -1))
	return radii


class Art:
	def __init__(self, width, height, pal, top=0):
		self.w, self.h, self.top, self.p, self.parts = width, height, top, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="middle", weight="400", mono=False):
		self.add(
			f'<text x="{x:.1f}" y="{y:.1f}" font-family="{MONO if mono else FONT}" font-size="{size}" '
			f'font-weight="{weight}" fill="{self.p[color]}" text-anchor="{anchor}">{s}</text>'
		)

	def name(self, x, y, s, anchor="middle", color="ink", mono=False):
		self.t(x, y, s, size=CODE if mono else NAME, color=color, anchor=anchor, weight="650", mono=mono)

	def label(self, x, y, title, note=None, anchor="middle", mono=False, below=True):
		"""Name with an optional grey note; y is the baseline of the line nearest the stop."""
		if below:
			self.name(x, y, title, anchor=anchor, mono=mono)
			if note:
				self.t(x, y + 17, note, anchor=anchor)
		else:
			self.name(x, y - (17 if note else 0), title, anchor=anchor, mono=mono)
			if note:
				self.t(x, y, note, anchor=anchor)

	# lines ------------------------------------------------------------
	def line(self, points, color, dist=0.0, cap="butt"):
		pts = offset(points, dist) if dist else points
		self.add(
			f'<path d="{rounded(pts, natural_radii(points, dist))}" stroke="{self.p[color]}" '
			f'stroke-width="{LW}" fill="none" stroke-linecap="{cap}" stroke-linejoin="round"/>'
		)

	def bar(self, x, y, color, vertical=True):
		"""Terminus: a bar across the end of the line."""
		h = 1.5 * LW
		if vertical:
			self.add(f'<rect x="{x - LW / 2:.1f}" y="{y - h:.1f}" width="{LW}" height="{2 * h}" fill="{self.p[color]}"/>')
		else:
			self.add(f'<rect x="{x - h:.1f}" y="{y - LW / 2:.1f}" width="{2 * h}" height="{LW}" fill="{self.p[color]}"/>')

	def stub(self, x, y, color, side):
		"""Plain stop: a stub on the side of its label."""
		reach = LW / 2 + 7
		dx, dy = {"up": (0, -1), "down": (0, 1), "left": (-1, 0), "right": (1, 0)}[side]
		x2, y2 = x + dx * reach, y + dy * reach
		self.add(
			f'<path d="M{x},{y} L{x2},{y2}" stroke="{self.p[color]}" stroke-width="{LW}" stroke-linecap="butt"/>'
		)

	def station(self, x1, y1, x2=None, y2=None, color="ink"):
		"""A circle, or a capsule from (x1, y1) to (x2, y2); dark outline for a hand-off."""
		x2 = x1 if x2 is None else x2
		y2 = y1 if y2 is None else y2
		inset = STOP - RING / 2
		self.add(
			f'<rect x="{min(x1, x2) - inset:.2f}" y="{min(y1, y2) - inset:.2f}" width="{abs(x2 - x1) + 2 * inset:.2f}" '
			f'height="{abs(y2 - y1) + 2 * inset:.2f}" rx="{inset:.2f}" fill="{self.p["bg"]}" '
			f'stroke="{self.p[color]}" stroke-width="{RING}"/>'
		)

	def badge(self, x, y, text, color, anchor="middle"):
		"""Line name on a capsule of the line's colour; returns its left and right edges."""
		w = width_of(text, BADGE) + 18
		h = 19
		left = {"middle": x - w / 2, "start": x, "end": x - w}[anchor]
		self.add(
			f'<rect x="{left:.1f}" y="{y - h / 2:.1f}" width="{w:.1f}" height="{h}" rx="{h / 2}" fill="{self.p[color]}"/>'
		)
		self.add(
			f'<text x="{left + w / 2:.1f}" y="{y + 4.1:.1f}" font-family="{FONT}" font-size="{BADGE}" '
			f'font-weight="700" fill="{self.p["on"][color]}" text-anchor="middle">{text}</text>'
		)
		return left, left + w

	def bracket(self, x1, x2, y, depth=6):
		self.add(
			f'<path d="M{x1},{y - depth} L{x1},{y} L{x2},{y} L{x2},{y - depth}" stroke="{self.p["rule"]}" '
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
	a = Art(880, 228, pal, top=22)
	y, top = 124, 124 - PITCH
	buffer, q0, q1, writer, store = 352, 476, 568, 640, 822

	# deadline thread comes down into the buffer and runs beside the caller to the queue
	_, right = a.badge(24, 52, "deadline thread", "amber", anchor="start")
	a.line([(right, 52), (250, 52), (310, top), (q0, top)], "amber")
	_, right = a.badge(24, y, "caller’s thread", "blue", anchor="start")
	a.line([(right, y), (q0, y)], "blue")
	a.line([(q1, y), (store, y)], "green")
	low = y + 58
	a.line([(writer, y), (writer, y + 22), (writer + 36, low), (store, low)], "red")

	a.stub(180, y, "blue", "up")
	a.label(180, y - 20, "producer", "records, one at a time", below=False)

	a.station(buffer, top, buffer, y)
	a.label(buffer, y + 34, "record buffer", "cut at 500 records or 1 second")

	a.station(q0, top, q1, y)
	for i in range(3):
		a.add(
			f'<rect x="{q1 - 22 - i * 20}" y="{top - 1}" width="14" height="{PITCH + 2}" rx="2" fill="{pal["soft"]}"/>'
		)
	a.label((q0 + q1) / 2, y + 34, "queue", "at most 16 batches")

	a.station(writer, y, color="green")
	a.label(writer, y - 20, "writer", "one write at a time", below=False)
	a.badge((writer + store) / 2, y, "writer thread", "green")

	a.station(store, y, color="green")
	a.label(store, y - 20, "ClickHouse", "one part per write", below=False)

	a.badge((writer + 36 + store) / 2, low, "write failed", "red")
	a.station(store, low, color="red")
	a.label(store, low + 32, "WriteFallback", "queue, file, log", mono=True)
	return a


# ---------------------------------------------------------------------- 2
def recovery(pal):
	a = Art(720, 436, pal, top=2)
	x, end, step = 260, 520, 92
	ys = [80, 80 + step, 80 + 2 * step]
	drop = 40

	a.badge(x, 30, "writer thread", "green")
	a.line([(x, 40), (x, ys[0])], "green")
	last = ys[2] + step
	a.line([(x, ys[0]), (x, last), (x + drop, last + drop), (end, last + drop)], "red")
	# each step forks: what was written leaves to the right, the rest goes down the red line
	branches = [(ys[0], "green", "500 records written"), (ys[1], "green", "499 written"), (ys[2], "rule", "1 record parked")]
	for y0, color, outcome in branches:
		a.line([(x, y0), (x + drop, y0 + drop), (end, y0 + drop)], color)
		a.bar(end, y0 + drop, color)
		a.name(end + 18, y0 + drop + 5, outcome, anchor="start")
	a.bar(end, last + drop, "red")
	a.name(end + 18, last + drop + 5, "lost", anchor="start")
	a.t(end + 18, last + drop + 22, "ERROR in the log, a metric", anchor="start")

	for y0, failed in zip(ys, ("fails", "1 still fails", "fails")):
		a.t(x + 14, y0 + drop + 34, failed, color="red", anchor="start", weight="600")

	rows = [
		("persist(batch)", "the whole batch, one write"),
		("persist(record)", "the same batch, record by record"),
		("WriteFallback", "whatever the retry could not write"),
	]
	for y0, (title, note) in zip(ys, rows):
		a.station(x, y0)
		a.name(x - 24, y0 + 1, title, anchor="end", mono=True)
		a.t(x - 24, y0 + 18, note, anchor="end")
	return a


# ---------------------------------------------------------------------- 3
def lifecycle(pal):
	a = Art(880, 104, pal, top=34)
	y = 84
	start, stop, drained, x0, x1 = 236, 512, 716, 32, 856
	segments = [(x0, start, "grey", "NEW"), (start, stop, "blue", "RUNNING"), (stop, drained, "green", "STOPPING"), (drained, x1, "grey", "STOPPED")]
	for left, right, color, state in segments:
		a.line([(left, y), (right, y)], color)
	a.bar(x0 + LW / 2, y, "grey")
	a.bar(x1 - LW / 2, y, "grey")
	for left, right, color, state in segments:
		a.badge((left + right) / 2, y, state, color)
	for x, event in ((start, "start"), (stop, "stop"), (drained, "queue drained")):
		a.station(x, y)
		a.name(x, y - 22, event)
	a.t((start + stop) / 2, y + 34, "accept allowed")
	a.t((stop + drained) / 2, y + 34, "accept rejected, buffer drains")
	return a


# ---------------------------------------------------------------------- 4
def shutdown(pal):
	a = Art(880, 216, pal, top=22)
	writer, blue, amber = 112, 112 - PITCH, 112 - 2 * PITCH
	x0, arrives, closes, remaining, drains, budget, parked = 128, 176, 372, 492, 648, 742, 808

	for yy, title in ((amber, "deadline thread"), (blue, "caller’s thread"), (writer, "writer thread")):
		a.t(x0 - 12, yy + 4, title, size=BADGE, color="ink", anchor="end", weight="650")
	a.line([(x0, amber), (closes, amber)], "amber")
	a.line([(x0, blue), (closes, blue)], "blue")
	a.line([(x0, writer), (budget, writer)], "green")
	a.line([(budget, writer), (parked, writer)], "red")

	a.station(arrives, amber, arrives, writer)
	a.label(arrives, amber - 20, "stop arrives", "after the message listeners", below=False)
	a.station(closes, amber, closes, blue)
	a.label(closes, amber - 20, "buffer closes", "deadline thread cancelled", below=False)

	a.station(remaining, writer, color="green")
	a.label(remaining, writer + 34, "remaining batch", "goes ahead of the queue")
	a.station(drains, writer, color="green")
	a.label(drains, writer + 34, "writer drains", "one batch at a time")
	a.station(parked, writer, color="red")
	a.label(parked, writer + 34, "leftovers parked")
	a.t(parked, writer + 51, "WriteFallback", mono=True)

	a.bracket(closes, budget, 196)
	a.t((closes + budget) / 2, 218, "shutdown-await-period", color="ink", mono=True, size=CODE)
	return a


# ---------------------------------------------------------------------- 5
def modules(pal):
	a = Art(530, 288, pal, top=2)
	x, jvm = 220, 220 - PITCH
	ys = {"starter": 80, "spring": 136, "micrometer": 196, "core": 252}

	a.badge(x, 30, "Spring Boot service", "green")
	a.t(x + 76, 34, "takes this one", anchor="start")
	a.line([(x, 40), (x, ys["core"])], "green")

	left, right = a.badge(24, 140, "plain JVM service", "blue", anchor="start")
	a.t((left + right) / 2, 168, "takes these two")
	a.line([(right, 140), (jvm - 30, 140), (jvm, 170), (jvm, ys["core"])], "blue")

	a.stub(x, ys["starter"], "green", "right")
	a.stub(x, ys["spring"], "green", "right")
	a.station(jvm, ys["micrometer"], x, ys["micrometer"])
	a.station(jvm, ys["core"], x, ys["core"])

	rows = [
		("starter", "batcher-spring-boot-starter", "one dependency, nothing of its own"),
		("spring", "batcher-spring-boot", "auto-configuration, properties, lifecycle"),
		("micrometer", "batcher-micrometer", "metrics, optional"),
		("core", "batcher-core", "the engine and every interface, no Spring"),
	]
	for key, title, note in rows:
		a.name(x + 26, ys[key] - 1, title, anchor="start", mono=True)
		a.t(x + 26, ys[key] + 16, note, anchor="start")
	return a


DIAGRAMS = {"pipeline": pipeline, "recovery": recovery, "lifecycle": lifecycle, "shutdown": shutdown, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
