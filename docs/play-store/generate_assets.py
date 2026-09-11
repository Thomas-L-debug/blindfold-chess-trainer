"""Generate Play Store hi-res icon (512) and feature graphic (1024x500)."""

from __future__ import annotations

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
PIECE_WHITE = (0xFF, 0xFF, 0xFF)
PIECE_BLACK = (0x14, 0x18, 0x1F)
ARROW = (0xE3, 0xA8, 0x7C)

WHITE = {
    "K": "♔",
    "Q": "♕",
    "R": "♖",
    "B": "♗",
    "N": "♘",
    "P": "♙",
}
BLACK = {
    "K": "♚",
    "Q": "♛",
    "R": "♜",
    "B": "♝",
    "N": "♞",
    "P": "♟",
}

# Position after 1.e4 d5 2.exd5 Qxd5 3.Nc3 Qe5+ 4.Qe2 Nc6 5.Nf3 Qxe2+ 6.Bxe2 Nb4
# rank 8 -> 1, file a -> h
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

FONTS = Path(r"C:\Windows\Fonts")


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONTS / name), size)


def rounded_paste(canvas: Image.Image, src: Image.Image, xy: tuple[int, int], radius: int) -> None:
    mask = Image.new("L", src.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, src.width, src.height), radius=radius, fill=255)
    canvas.paste(src, xy, mask)


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


def draw_board(board_px: int) -> Image.Image:
    notation = 28
    square = (board_px - notation) // 8
    inner = square * 8
    board_px = inner + notation
    img = Image.new("RGB", (board_px, board_px), BG)
    draw = ImageDraw.Draw(img)
    origin = notation
    piece_font = font("seguisym.ttf", max(30, int(square * 0.82)))
    coord_font = font("segoeui.ttf", 14)

    for file in range(8):
        for rank in range(8):
            light = (file + rank) % 2 == 0
            x0 = origin + file * square
            y0 = origin + rank * square
            draw.rectangle(
                (x0, y0, x0 + square - 1, y0 + square - 1),
                fill=BOARD_LIGHT if light else BOARD_DARK,
            )
            piece = POSITION.get((file, rank))
            if not piece:
                continue
            kind, is_white = piece
            glyph = WHITE[kind] if is_white else BLACK[kind]
            fill = PIECE_WHITE if is_white else PIECE_BLACK
            # Opposite halo so outline pieces stay readable on both square colors.
            stroke = (0x1A, 0x1E, 0x24) if is_white else (0xE6, 0xEEE, 0xE8)
            bbox = draw.textbbox((0, 0), glyph, font=piece_font)
            tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
            tx = x0 + (square - tw) / 2 - bbox[0]
            ty = y0 + (square - th) / 2 - bbox[1] - square * 0.03
            draw.text(
                (tx, ty),
                glyph,
                font=piece_font,
                fill=fill,
                stroke_width=2,
                stroke_fill=stroke,
            )

    def center(file: int, rank: int) -> tuple[float, float]:
        return (origin + (file + 0.5) * square, origin + (rank + 0.5) * square)

    x1, y1 = center(2, 3)  # c5
    x2, y2 = center(1, 4)  # b4
    draw.line((x1, y1, x2, y2), fill=ARROW, width=5)
    draw.polygon(
        [(x2 - 1, y2 + 12), (x2 + 11, y2 - 3), (x2 - 12, y2 - 1)],
        fill=ARROW,
    )
    bx, by = center(1, 4)
    r = square * 0.40
    draw.ellipse((bx - r, by - r, bx + r, by + r), outline=ARROW, width=3)

    for i in range(8):
        ch = str(8 - i)
        bbox = draw.textbbox((0, 0), ch, font=coord_font)
        tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
        draw.text(
            ((notation - tw) / 2, origin + i * square + (square - th) / 2),
            ch,
            font=coord_font,
            fill=MUTED,
        )
    return img


def draw_feature_graphic() -> Image.Image:
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), BG)
    draw = ImageDraw.Draw(img)

    board = draw_board(400)
    board_x, board_y = 48, (h - board.height) // 2
    rounded_paste(img, board, (board_x, board_y), radius=18)
    # quiet outline around the board, like the app cards
    draw.rounded_rectangle(
        (board_x, board_y, board_x + board.width - 1, board_y + board.height - 1),
        radius=18,
        outline=(0x3A, 0x42, 0x4C),
        width=1,
    )

    text_left = board_x + board.width + 48
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
    im = Image.open(src).convert("RGB")
    w, h = im.size
    # 1080x2340 Samsung: status icons occupy ~36–72, 3-button nav starts ~2208.
    status = 76 if h >= 2300 else max(36, int(h * 0.034))
    nav = 132 if h >= 2300 else max(56, int(h * 0.055))
    top = status + extra_top
    im = im.crop((0, top, w, h - nav))
    cw, ch = im.size
    if ch > cw * 2:
        extra = ch - cw * 2
        im = im.crop((0, extra // 2, cw, ch - extra // 2 - extra % 2))
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
        size = process_screenshot(src, dst, extra_top=extra_top)
        print(f"screenshot {dst_name} {size[0]}x{size[1]}")

    print(f"icon {icon.size[0]}x{icon.size[1]} {icon.mode} -> {icon_path}")
    print(f"feature {feature.size[0]}x{feature.size[1]} {feature.mode} -> {feature_path}")


if __name__ == "__main__":
    main()
