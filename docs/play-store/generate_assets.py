"""Generate Play Store hi-res icon (512) and feature graphic (1024x500)."""

from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent
SCREENSHOT_DIR = Path.home() / "Downloads" / "Telegram Desktop"

BG = (0x12, 0x14, 0x1A)
SURFACE = (0x1C, 0x1F, 0x28)
ON_BG = (0xE8, 0xEA, 0xF0)
MUTED = (0x9A, 0xA3, 0xB2)
ACCENT = (0x7E, 0xB8, 0xA4)
BOARD_LIGHT = (0x8F, 0xA8, 0x9A)
BOARD_DARK = (0x2E, 0x38, 0x44)
ARROW = (0xE3, 0xA8, 0x7C)

# Position after 1.e4 d5 2.exd5 Qxd5 3.Nc3 Qe5+ 4.Qe2 Nc6 5.Nf3 Qxe2+ 6.Bxe2 Nb4
# rank 8 -> 1, file a -> h. Last move is Nb4 from c6 (knight jump), not c5.
POSITION = {
    (0, 0): ("R", False),
    (2, 0): ("B", False),
    (4, 0): ("K", False),
    (5, 0): ("B", False),
    (6, 0): ("N", False),
    (7, 0): ("R", False),
    (0, 1): ("P", False),
    (1, 1): ("P", False),
    (2, 1): ("P", False),
    (4, 1): ("P", False),
    (5, 1): ("P", False),
    (6, 1): ("P", False),
    (7, 1): ("P", False),
    (1, 4): ("N", False),  # b4
    (2, 5): ("N", True),  # c3
    (5, 5): ("N", True),  # f3
    (0, 6): ("P", True),
    (1, 6): ("P", True),
    (2, 6): ("P", True),
    (3, 6): ("P", True),
    (4, 6): ("B", True),  # e2
    (5, 6): ("P", True),
    (6, 6): ("P", True),
    (7, 6): ("P", True),
    (0, 7): ("R", True),
    (2, 7): ("B", True),
    (4, 7): ("K", True),
    (7, 7): ("R", True),
}

# Clean squares in 02-board.png (no last-move overlay) used as sprite sources.
# (file, rank_from_top) on the screenshot.
SPRITE_CELLS = {
    ("P", True): (0, 6),   # a2
    ("R", True): (0, 7),   # a1
    ("B", True): (2, 7),   # c1
    ("N", True): (2, 5),   # c3
    ("K", True): (4, 7),   # e1
    ("P", False): (0, 1),  # a7
    ("R", False): (0, 0),  # a8
    ("B", False): (2, 0),  # c8
    ("N", False): (6, 0),  # g8
    ("K", False): (4, 0),  # e8
}

# Pixel edges of the 8 files / ranks on 02-board.png (1080x2132 crop).
SHOT_X = [56, 159, 261, 364, 467, 570, 672, 775, 878]
SHOT_Y = [19, 122, 224, 327, 430, 533, 635, 738, 841]

FONTS = Path(r"C:\Windows\Fonts")


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONTS / name), size)


def rounded_paste(canvas: Image.Image, src: Image.Image, xy: tuple[int, int], radius: int) -> None:
    mask = Image.new("L", src.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, src.width, src.height), radius=radius, fill=255)
    canvas.paste(src, xy, mask)


def _dist2(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return (a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2


def extract_piece_sprites() -> dict[tuple[str, bool], Image.Image]:
    """Cut filled Android glyphs out of the native board screenshot."""
    shot = Image.open(ROOT / "screenshots" / "02-board.png").convert("RGB")
    sprites: dict[tuple[str, bool], Image.Image] = {}
    light_cut = 34 * 34
    dark_cut = 26 * 26
    for key, (file, rank) in SPRITE_CELLS.items():
        cell = shot.crop((SHOT_X[file], SHOT_Y[rank], SHOT_X[file + 1], SHOT_Y[rank + 1]))
        rgba = cell.convert("RGBA")
        px = rgba.load()
        w, h = rgba.size
        on_light = (file + rank) % 2 == 0
        board = BOARD_LIGHT if on_light else BOARD_DARK
        cut = light_cut if on_light else dark_cut
        for y in range(h):
            for x in range(w):
                r, g, b, _a = px[x, y]
                if _dist2((r, g, b), board) <= cut:
                    px[x, y] = (0, 0, 0, 0)
        sprites[key] = rgba
    return sprites


def draw_icon() -> Image.Image:
    """512 px Play icon — same 6-square mark as the adaptive launcher."""
    size = 512
    img = Image.new("RGB", (size, size), BG)
    draw = ImageDraw.Draw(img)

    def u(n: float) -> int:
        return round(n * size / 108)

    squares = [(30, 30), (54, 30), (42, 42), (66, 42), (42, 54), (54, 66)]
    side = u(12)
    radius = max(4, u(1.2))
    for x, y in squares:
        x0, y0 = u(x), u(y)
        draw.rounded_rectangle((x0, y0, x0 + side - 1, y0 + side - 1), radius=radius, fill=ACCENT)
    return img


def _blend(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return (
        round(a[0] + (b[0] - a[0]) * t),
        round(a[1] + (b[1] - a[1]) * t),
        round(a[2] + (b[2] - a[2]) * t),
    )


def _edge(i: int, n: int) -> int:
    return round(i * n / 8)


def draw_board(board_px: int) -> Image.Image:
    img = Image.new("RGB", (board_px, board_px), BG)
    draw = ImageDraw.Draw(img)
    sprites = extract_piece_sprites()
    last_from = (2, 2)  # c6
    last_to = (1, 4)  # b4  — knight jump (1,2)

    def cell(file: int, rank: int) -> tuple[int, int, int, int]:
        return (
            _edge(file, board_px),
            _edge(rank, board_px),
            _edge(file + 1, board_px),
            _edge(rank + 1, board_px),
        )

    def center(file: int, rank: int) -> tuple[float, float]:
        x0, y0, x1, y1 = cell(file, rank)
        return ((x0 + x1) / 2, (y0 + y1) / 2)

    for file in range(8):
        for rank in range(8):
            light = (file + rank) % 2 == 0
            fill = BOARD_LIGHT if light else BOARD_DARK
            if (file, rank) in (last_from, last_to):
                fill = _blend(fill, ARROW, 0.38)
            x0, y0, x1, y1 = cell(file, rank)
            draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=fill)

    x1, y1 = center(*last_from)
    x2, y2 = center(*last_to)
    draw.line((x1, y1, x2, y2), fill=ARROW, width=5)
    ang = math.atan2(y2 - y1, x2 - x1)
    ah = 13
    draw.polygon(
        [
            (x2, y2),
            (x2 - ah * math.cos(ang - 0.5), y2 - ah * math.sin(ang - 0.5)),
            (x2 - ah * math.cos(ang + 0.5), y2 - ah * math.sin(ang + 0.5)),
        ],
        fill=ARROW,
    )

    for (file, rank), (kind, is_white) in POSITION.items():
        x0, y0, x1, y1 = cell(file, rank)
        piece = sprites[(kind, is_white)].resize((x1 - x0, y1 - y0), Image.Resampling.LANCZOS)
        img.paste(piece, (x0, y0), piece)
    return img


def draw_feature_graphic() -> Image.Image:
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), BG)
    draw = ImageDraw.Draw(img)

    board = draw_board(h)
    img.paste(board, (0, 0))

    text_left = board.width + 48
    text_right = w - 56
    col_w = text_right - text_left

    title_font = font("seguisb.ttf", 42)
    line2_font = font("seguisb.ttf", 42)
    tag_font = font("segoeui.ttf", 22)
    small_font = font("segoeui.ttf", 18)

    lines = [
        ("Blindfold", title_font, ON_BG),
        ("Chess Trainer", line2_font, ON_BG),
    ]

    def text_size(text: str, fnt: ImageFont.FreeTypeFont) -> tuple[int, int]:
        bbox = draw.textbbox((0, 0), text, font=fnt)
        return bbox[2] - bbox[0], bbox[3] - bbox[1]

    block_h = 0
    measured: list[tuple[str, ImageFont.FreeTypeFont, tuple[int, int, int], int, int]] = []
    for text, fnt, color in lines:
        tw, th = text_size(text, fnt)
        measured.append((text, fnt, color, tw, th))
        block_h += th + 6
    block_h += 18 + 8  # accent bar + gap
    tag = "Train chess from memory."
    tag_w, tag_h = text_size(tag, tag_font)
    small = "Squares  ·  paths  ·  classics  ·  offline bot"
    small_w, small_h = text_size(small, small_font)
    block_h += 16 + tag_h + 10 + small_h

    y = (h - block_h) // 2
    for text, fnt, color, tw, th in measured:
        draw.text((text_left, y), text, font=fnt, fill=color)
        y += th + 6

    y += 10
    bar_w = min(96, col_w)
    draw.rounded_rectangle((text_left, y, text_left + bar_w, y + 5), radius=2, fill=ACCENT)
    y += 22
    draw.text((text_left, y), tag, font=tag_font, fill=ACCENT)
    y += tag_h + 12
    draw.text((text_left, y), small, font=small_font, fill=MUTED)
    return img


def process_screenshot(src: Path, dst: Path, extra_top: int = 0) -> tuple[int, int]:
    """Phone screenshot → 1080×1920 (9:16), 24-bit PNG, no alpha."""
    im = Image.open(src).convert("RGB")
    w, h = im.size
    # 1080x2340 Samsung: status icons occupy ~36–72, 3-button nav starts ~2208.
    status = 76 if h >= 2300 else max(36, int(h * 0.034))
    nav = 132 if h >= 2300 else max(56, int(h * 0.055))
    top = min(status + extra_top, h - nav - 2)
    im = im.crop((0, top, w, h - nav))
    cw, ch = im.size
    target_h = round(cw * 16 / 9)
    if ch > target_h:
        im = im.crop((0, 0, cw, target_h))
    elif ch < target_h:
        padded = Image.new("RGB", (cw, target_h), BG)
        padded.paste(im, (0, 0))
        im = padded
    im = im.resize((1080, 1920), Image.Resampling.LANCZOS)
    im.save(dst, "PNG", optimize=True)
    return im.size


def main() -> None:
    shots_dir = ROOT / "screenshots"
    shots_dir.mkdir(parents=True, exist_ok=True)

    icon = draw_icon()
    icon_path = ROOT / "icon-512.png"
    icon.save(icon_path, "PNG")

    feature = draw_feature_graphic()
    feature_path = ROOT / "feature-graphic.png"
    feature.save(feature_path, "PNG")

    mapping = [
        ("Screenshot_20260911_202821_Blindfold Chess.jpg", "01-home.png", 0),
        ("Screenshot_20260911_203429_Blindfold Chess.jpg", "02-board.png", 0),
        ("Screenshot_20260911_203025_Blindfold Chess.jpg", "03-play-bot-setup.png", 0),
        ("Screenshot_20260911_203411_Blindfold Chess.jpg", "04-play-bot-voice.png", 0),
        # Mid-scroll shot: skip the truncated Find the Square card under Show board.
        ("Screenshot_20260911_203939_Blindfold Chess.jpg", "05-drills.png", 284),
    ]
    for src_name, dst_name, extra_top in mapping:
        src = SCREENSHOT_DIR / src_name
        dst = shots_dir / dst_name
        if not src.exists():
            print(f"skip screenshot {src_name} (source missing)")
            continue
        size = process_screenshot(src, dst, extra_top=extra_top)
        print(f"screenshot {dst_name} {size[0]}x{size[1]}")

    print(f"icon {icon.size[0]}x{icon.size[1]} {icon.mode} -> {icon_path}")
    print(f"feature {feature.size[0]}x{feature.size[1]} {feature.mode} -> {feature_path}")


if __name__ == "__main__":
    main()
