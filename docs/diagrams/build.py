#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram."""

import pathlib

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {
		"ink": "#10151B",
		"muted": "#6A7482",
		"hair": "#D7DCE3",
		"card": "#FFFFFF",
		"band": "#F3F5F8",
		"accent": "#2563EB",
		"accentSoft": "#E8EFFD",
		"fail": "#C8372F",
		"failSoft": "#FBECEB",
	},
	"dark": {
		"ink": "#E8EDF4",
		"muted": "#95A0AF",
		"hair": "#2B3542",
		"card": "#151B23",
		"band": "#121820",
		"accent": "#7BA6FF",
		"accentSoft": "#1B2740",
		"fail": "#FF8A82",
		"failSoft": "#2E1B1B",
	},
}


class Canvas:
	def __init__(self, width, height, pal):
		self.w, self.h, self.p = width, height, pal
		self.parts = []

	def add(self, markup):
		self.parts.append(markup)

	def text(self, x, y, value, size=13, color="ink", anchor="middle", weight="400", spacing=None, mono=False):
		extra = f' letter-spacing="{spacing}"' if spacing else ""
		family = MONO if mono else FONT
		self.add(
			f'<text x="{x}" y="{y}" font-family="{family}" font-size="{size}" font-weight="{weight}" '
			f'fill="{self.p[color]}" text-anchor="{anchor}"{extra}>{value}</text>'
		)

	def label(self, x, y, value, color="muted", spacing="0.08em"):
		self.text(x, y, value.upper(), size=10.5, color=color, anchor="start", weight="600", spacing=spacing)

	def card(self, x, y, w, h, title, sub=None, tone="plain", dashed=False, mono_title=False):
		stroke = {"plain": "hair", "accent": "accent", "fail": "fail"}[tone]
		fill = {"plain": "card", "accent": "accentSoft", "fail": "failSoft"}[tone]
		dash = ' stroke-dasharray="5 4"' if dashed else ""
		self.add(
			f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="{self.p[fill]}" '
			f'stroke="{self.p[stroke]}" stroke-width="1.25"{dash}/>'
		)
		cx = x + w / 2
		if sub:
			self.text(cx, y + h / 2 - 3, title, size=14, weight="550", mono=mono_title)
			self.text(cx, y + h / 2 + 17, sub, size=12, color="muted")
		else:
			self.text(cx, y + h / 2 + 5, title, size=14, weight="550", mono=mono_title)

	def line(self, d, tone="accent", dashed=False, head=True, width=1.4):
		dash = ' stroke-dasharray="6 5"' if dashed else ""
		marker = f' marker-end="url(#head-{tone})"' if head else ""
		self.add(
			f'<path d="{d}" fill="none" stroke="{self.p[tone]}" stroke-width="{width}" '
			f'stroke-linecap="round" stroke-linejoin="round"{dash}{marker}/>'
		)

	def dot(self, x, y, tone="accent", r=4.5):
		self.add(f'<circle cx="{x}" cy="{y}" r="{r}" fill="{self.p[tone]}"/>')

	def band(self, x, y, w, h):
		self.add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="14" fill="{self.p["band"]}"/>')

	def render(self):
		defs = "".join(
			f'<marker id="head-{tone}" markerWidth="9" markerHeight="9" refX="7.4" refY="4.5" orient="auto">'
			f'<path d="M0.5,1 L7.6,4.5 L0.5,8" fill="none" stroke="{self.p[tone]}" stroke-width="1.4" '
			f'stroke-linecap="round" stroke-linejoin="round"/></marker>'
			for tone in ("accent", "fail", "muted", "hair")
		)
		body = "\n  ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" '
			f'width="{self.w}" height="{self.h}" role="img">\n'
			f'  <defs>{defs}</defs>\n  {body}\n</svg>\n'
		)


def pipeline(pal):
	c = Canvas(1040, 300, pal)
	c.band(248, 44, 536, 150)
	c.label(268, 70, "one batcher")

	y, h, w = 92, 68, 136
	c.card(24, y, w, h, "Producer", "consumer, handler, job")
	c.card(268, y, w, h, "RecordBuffer", "cuts by size or age")
	c.card(448, y, w, h, "Queue", "bounded, holds batches")
	c.card(628, y, w, h, "Writer thread", "one batch at a time")
	c.card(872, y, 144, h, "Storage", "ClickHouse, anything")
	c.card(872, 216, 144, 60, "WriteFallback", "queue, file, log", tone="fail")

	mid = y + h / 2
	c.line(f"M166,{mid} L262,{mid}")
	c.text(214, mid - 13, "accept(record)", size=11, color="muted", mono=True)
	c.line(f"M410,{mid} L442,{mid}")
	c.text(426, mid - 13, "batch", size=11, color="muted")
	c.line(f"M590,{mid} L622,{mid}")
	c.line(f"M770,{mid} L866,{mid}")
	c.text(818, mid - 13, "persist(batch)", size=11, color="muted", mono=True)

	c.line(f"M696,{y + h} L696,246 L866,246", tone="fail", dashed=True)
	c.text(712, 238, "write failed", size=11, color="fail", anchor="start")
	return c


def recovery(pal):
	c = Canvas(1100, 212, pal)
	y, h, w = 112, 68, 190
	c.card(24, y, w, h, "persist(batch)", "the whole batch at once", mono_title=True)
	c.card(314, y, w, h, "persist(record)", "one record at a time", mono_title=True)
	c.card(604, y, w, h, "WriteFallback", "park the record")
	c.card(894, y, 170, h, "Lost", "ERROR and a metric", tone="fail")

	mid = y + h / 2
	for x0, x1, caption in ((214, 308, "batch failed"), (504, 598, "record failed"), (794, 888, "fallback failed")):
		c.line(f"M{x0},{mid} L{x1},{mid}", tone="fail", dashed=True)
		c.text((x0 + x1) / 2, mid - 14, caption, size=11, color="fail")

	for cx, caption in ((119, "written"), (409, "written"), (699, "parked")):
		c.line(f"M{cx},{y - 6} L{cx},54", tone="accent")
		c.text(cx, 42, caption, size=11.5, color="accent")

	return c


def lifecycle(pal):
	c = Canvas(940, 208, pal)
	states = [
		(24, "NEW", "threads not started", "plain"),
		(256, "RUNNING", "accept allowed", "accent"),
		(488, "STOPPING", "accept rejected,|buffer still drains", "plain"),
		(720, "STOPPED", "threads gone", "plain"),
	]
	y, h, w = 64, 48, 180
	for x, name, caption, tone in states:
		c.card(x, y, w, h, name, tone=tone)
		for i, line in enumerate(caption.split("|")):
			c.text(x + w / 2, y + h + 26 + i * 17, line, size=12, color="muted")

	mid = y + h / 2
	for x0, x1, caption in ((212, 250, "start"), (444, 482, "stop"), (676, 714, "drained")):
		c.line(f"M{x0},{mid} L{x1},{mid}")
		c.text((x0 + x1) / 2, mid - 16, caption, size=11.5, color="muted")
	return c


def shutdown(pal):
	c = Canvas(1040, 300, pal)
	axis_y = 150
	c.line(f"M40,{axis_y} L1000,{axis_y}", tone="hair", head=False, width=1.25)

	steps = [
		(100, "stop() arrives", "after the listeners", "above", "accent"),
		(320, "buffer stops accepting", "deadline thread cancelled", "below", "accent"),
		(552, "remaining batch", "goes ahead of the queue", "above", "accent"),
		(764, "writer drains", "one batch at a time", "below", "accent"),
		(946, "leftovers parked", "WriteFallback", "above", "fail"),
	]
	for x, title, sub, side, tone in steps:
		c.dot(x, axis_y, tone=tone)
		if side == "above":
			c.line(f"M{x},{axis_y - 11} L{x},{axis_y - 30}", tone="hair", head=False, width=1.25)
			c.text(x, axis_y - 58, title, size=13, weight="550")
			c.text(x, axis_y - 40, sub, size=11.5, color="muted")
		else:
			c.line(f"M{x},{axis_y + 11} L{x},{axis_y + 30}", tone="hair", head=False, width=1.25)
			c.text(x, axis_y + 48, title, size=13, weight="550")
			c.text(x, axis_y + 66, sub, size=11.5, color="muted")

	c.add(
		f'<path d="M320,242 L320,252 L764,252 L764,242" fill="none" stroke="{pal["hair"]}" '
		f'stroke-width="1.25" stroke-linecap="round"/>'
	)
	c.text(542, 272, "shutdown-await-period", size=11.5, color="muted", mono=True)
	return c


def modules(pal):
	c = Canvas(900, 420, pal)
	c.card(60, 24, 240, 52, "Spring Boot service")
	c.card(600, 24, 240, 52, "Plain JVM service")
	c.card(60, 112, 240, 64, "batcher-spring-boot-starter", "dependencies only", dashed=True, mono_title=True)
	c.card(60, 216, 240, 72, "batcher-spring-boot", "auto-configuration, properties", mono_title=True)
	c.card(600, 216, 240, 72, "batcher-micrometer", "metrics adapter", mono_title=True)
	c.card(240, 324, 420, 68, "batcher-core", "engine and interfaces, no Spring", tone="accent", mono_title=True)

	c.line("M180,76 L180,106")
	c.line("M180,176 L180,210")
	c.line("M180,288 L180,338 L234,338")
	c.line("M300,252 L594,252", dashed=True)
	c.text(447, 242, "optional", size=11.5, color="muted")
	c.line("M720,76 L720,210", dashed=True)
	c.text(736, 148, "optional", size=11.5, color="muted", anchor="start")
	c.line("M720,288 L720,338 L666,338")
	c.line("M840,50 L870,50 L870,366 L666,366")
	return c


DIAGRAMS = {
	"pipeline": pipeline,
	"recovery": recovery,
	"lifecycle": lifecycle,
	"shutdown": shutdown,
	"modules": modules,
}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for name, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{name}-{theme}.svg").write_text(build(pal).render())
	print(f"written: {len(DIAGRAMS) * 2} files in {OUT}")
