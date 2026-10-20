#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

Rules the drawings follow: no frame or fill that carries no meaning, one
typeface and three sizes, labels next to what they label, colour only where it
means failure, records and batches drawn to the same scale everywhere.
"""

import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {"ink": "#14181D", "soft": "#5C6570", "mute": "#949CA6", "hair": "#C9CFD6", "bad": "#C0392F"},
	"dark": {"ink": "#E9EDF2", "soft": "#9BA5B1", "mute": "#6F7985", "hair": "#39424D", "bad": "#FF8177"},
}

NAME, NOTE, TINY = 13.5, 11.5, 10.5


class Art:
	def __init__(self, width, height, pal):
		self.w, self.h, self.p, self.parts = width, height, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="middle", weight="400", mono=False, spacing=None):
		gap = f' letter-spacing="{spacing}"' if spacing else ""
		self.add(
			f'<text x="{x}" y="{y}" font-family="{MONO if mono else FONT}" font-size="{size}" '
			f'font-weight="{weight}" fill="{self.p[color]}" text-anchor="{anchor}"{gap}>{s}</text>'
		)

	def name(self, x, y, s, anchor="middle", color="ink", mono=False):
		self.t(x, y, s, size=NAME, color=color, anchor=anchor, weight="550", mono=mono)

	def caps(self, x, y, s, anchor="middle", color="mute"):
		self.t(x, y, s.upper(), size=TINY, color=color, anchor=anchor, weight="600", spacing="0.1em")

	# marks ------------------------------------------------------------
	def rule(self, x1, y1, x2, y2, color="hair", width=1, dashed=False, head=False):
		dash = ' stroke-dasharray="5 4"' if dashed else ""
		cap = f' marker-end="url(#tip-{color})"' if head else ""
		self.add(
			f'<path d="M{x1},{y1} L{x2},{y2}" stroke="{self.p[color]}" stroke-width="{width}" '
			f'fill="none" stroke-linecap="round"{dash}{cap}/>'
		)

	def path(self, d, color="hair", width=1, dashed=False, head=False):
		dash = ' stroke-dasharray="5 4"' if dashed else ""
		cap = f' marker-end="url(#tip-{color})"' if head else ""
		self.add(
			f'<path d="{d}" stroke="{self.p[color]}" stroke-width="{width}" fill="none" '
			f'stroke-linecap="round" stroke-linejoin="round"{dash}{cap}/>'
		)

	def dot(self, x, y, color="ink", r=3.6):
		self.add(f'<circle cx="{x}" cy="{y}" r="{r}" fill="{self.p[color]}"/>')

	def tick(self, x, base, height, color="soft", width=1.6):
		self.add(
			f'<path d="M{x},{base} L{x},{base - height}" stroke="{self.p[color]}" '
			f'stroke-width="{width}" stroke-linecap="butt"/>'
		)

	def block(self, x, base, w, h, color="ink", opacity=1.0):
		self.add(f'<rect x="{x}" y="{base - h}" width="{w}" height="{h}" fill="{self.p[color]}" opacity="{opacity}"/>')

	def outline(self, x, y, w, h, color="hair"):
		self.add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="none" stroke="{self.p[color]}" stroke-width="1"/>')

	def brace(self, x1, y, x2, depth=7, color="hair", up=False):
		k = -depth if up else depth
		self.path(f"M{x1},{y} L{x1},{y + k} L{x2},{y + k} L{x2},{y}", color=color)

	def svg(self):
		tips = "".join(
			f'<marker id="tip-{c}" markerWidth="9" markerHeight="9" refX="7.2" refY="4.5" orient="auto">'
			f'<path d="M0.8,1.2 L7.4,4.5 L0.8,7.8" fill="none" stroke="{self.p[c]}" stroke-width="1.2" '
			f'stroke-linecap="round" stroke-linejoin="round"/></marker>'
			for c in ("ink", "soft", "mute", "hair", "bad")
		)
		body = "\n  ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" '
			f'width="{self.w}" height="{self.h}" role="img">\n  <defs>{tips}</defs>\n  {body}\n</svg>\n'
		)


# ---------------------------------------------------------------------- 1
def pipeline(pal):
	a = Art(1040, 244, pal)
	base = 128

	zones = [(40, "producer"), (232, "record buffer"), (476, "queue"), (648, "writer thread"), (820, "clickhouse")]
	for x, label in zones:
		a.caps(x + 10, 40, label, anchor="start")
		if x > 40:
			a.rule(x, 28, x, base + 5, color="hair", dashed=False, width=0.6)
	a.rule(40, base, 1000, base, color="hair", head=True)

	for i in range(7):
		a.tick(72 + i * 22, base, 16)
	a.t(136, base - 36, "records, one at a time")

	for i in range(26):
		a.tick(252 + i * 6, base, 16)
	a.brace(252, base - 26, 402, depth=6, up=True)
	a.t(327, base - 42, "500 of them, or one second")
	a.rule(424, base, 424, base - 28, color="ink", width=1.4)
	a.t(424, base - 36, "cut", color="ink")

	for i in range(3):
		a.block(498 + i * 44, base, 32, 16, color="mute")
	a.t(564, base - 36, "batches wait, at most 16")

	a.block(690, base, 32, 16, color="ink")
	a.t(706, base - 36, "one write at a time", color="ink")

	a.outline(876, base - 22, 56, 22, color="ink")
	a.t(904, base - 36, "one part per write")

	a.path(f"M706,{base + 8} L706,{base + 58} L862,{base + 58}", color="bad", dashed=True, head=True)
	a.t(718, base + 50, "write failed", color="bad", anchor="start")
	a.name(876, base + 63, "WriteFallback", anchor="start", color="bad")
	a.t(876, base + 81, "queue, file, log", anchor="start", color="mute")
	return a


# ---------------------------------------------------------------------- 2
def recovery(pal):
	a = Art(800, 316, pal)
	rows = [
		(64, "persist(batch)", True, "the whole batch, one write", "500 records written", "ink"),
		(136, "persist(record)", True, "the same batch, record by record", "499 written, 1 still fails", "ink"),
		(208, "WriteFallback", False, "whatever the retry could not write", "1 record parked", "ink"),
		(280, "lost", False, "the fallback failed as well", "ERROR in the log, a metric", "bad"),
	]
	for (y, *_), (y_next, *_) in zip(rows, rows[1:]):
		middle = (y + y_next) / 2
		a.rule(44, y + 8, 44, middle - 13, color="hair")
		a.rule(44, middle + 9, 44, y_next - 8, color="hair")
		a.t(44, middle + 4, "fails", size=TINY, color="bad")

	for y, title, mono, note, outcome, color in rows:
		a.dot(44, y, color="bad" if color == "bad" else "ink", r=3.2)
		a.name(76, y + 4, title, anchor="start", color=color, mono=mono)
		a.t(76, y + 22, note, anchor="start", color="mute")
		a.t(760, y + 4, outcome, anchor="end", color=color if color == "bad" else "soft")
	return a


# ---------------------------------------------------------------------- 3
def lifecycle(pal):
	a = Art(1040, 180, pal)
	base = 92
	points = [(110, "NEW"), (390, "RUNNING"), (670, "STOPPING"), (950, "STOPPED")]
	a.rule(60, base, 1000, base, color="hair")

	for x, label in points:
		a.dot(x, base)
		a.name(x, base - 28, label)

	for x0, x1, label in ((110, 390, "start"), (390, 670, "stop"), (670, 950, "queue drained")):
		a.t((x0 + x1) / 2, base - 11, label, color="mute")

	a.add(f'<rect x="390" y="{base + 15}" width="280" height="3" fill="{pal["ink"]}"/>')
	a.add(f'<rect x="670" y="{base + 15}" width="280" height="3" fill="{pal["hair"]}"/>')
	a.t(530, base + 38, "accept allowed", color="ink")
	a.t(810, base + 38, "accept rejected, buffer drains")
	return a


# ---------------------------------------------------------------------- 4
def shutdown(pal):
	a = Art(1040, 262, pal)
	base = 128
	a.rule(48, base, 1000, base, color="hair", head=True)
	steps = [
		(96, "stop arrives", "after the message listeners", "up", "ink"),
		(320, "buffer closes", "deadline thread cancelled", "down", "ink"),
		(540, "remaining batch", "goes ahead of the queue", "up", "ink"),
		(762, "writer drains", "one batch at a time", "down", "ink"),
		(950, "leftovers parked", "WriteFallback", "up", "bad"),
	]
	for x, title, note, side, color in steps:
		a.dot(x, base, color=color)
		if side == "up":
			a.name(x, base - 32, title, color=color)
			a.t(x, base - 15, note, color="mute")
		else:
			a.name(x, base + 30, title, color=color)
			a.t(x, base + 47, note, color="mute")

	a.brace(320, base + 72, 762, depth=8)
	a.t(541, base + 98, "shutdown-await-period", mono=True, color="soft")
	return a


# ---------------------------------------------------------------------- 5
def modules(pal):
	a = Art(1040, 288, pal)
	a.name(100, 60, "batcher-spring-boot-starter", anchor="start", mono=True)
	a.t(390, 60, "one dependency, nothing of its own", anchor="start", color="mute")
	a.t(830, 56, "a Spring Boot service", anchor="start", color="soft")
	a.t(830, 74, "takes this one", anchor="start", color="mute")

	rows = [
		(126, "batcher-spring-boot", "auto-configuration, properties, lifecycle"),
		(182, "batcher-micrometer", "metrics, optional"),
		(238, "batcher-core", "the engine and every interface, no Spring"),
	]
	a.rule(108, 78, 108, 238, color="hair")
	for y, title, note in rows:
		a.rule(108, y, 134, y, color="hair")
		a.name(146, y + 4, title, anchor="start", mono=True)
		a.t(390, y + 4, note, anchor="start", color="mute")

	a.path("M790,178 L800,178 L800,242 L790,242", color="hair")
	a.t(830, 204, "a plain JVM service", anchor="start", color="soft")
	a.t(830, 222, "takes these two", anchor="start", color="mute")
	return a


DIAGRAMS = {"pipeline": pipeline, "recovery": recovery, "lifecycle": lifecycle, "shutdown": shutdown, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
