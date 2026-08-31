#!/usr/bin/env python3
"""Generate placeholder 16x16 / 64x64 pixel-art textures for Apex Ballistics."""
from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path("/workspace/src/main/resources")


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    height = len(pixels)
    width = len(pixels[0])
    raw = b"".join(
        b"\x00" + bytes(channel for pixel in row for channel in pixel)
        for row in pixels
    )

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def canvas(size: int, color: tuple[int, int, int, int] = (0, 0, 0, 0)) -> list[list[tuple[int, int, int, int]]]:
    return [[color for _ in range(size)] for _ in range(size)]


def setp(img, x, y, color) -> None:
    if 0 <= y < len(img) and 0 <= x < len(img[0]):
        img[y][x] = color


def fill(img, x0, y0, x1, y1, color) -> None:
    for y in range(y0, y1):
        for x in range(x0, x1):
            setp(img, x, y, color)


def rect(img, x0, y0, x1, y1, color) -> None:
    for x in range(x0, x1):
        setp(img, x, y0, color)
        setp(img, x, y1 - 1, color)
    for y in range(y0, y1):
        setp(img, x0, y, color)
        setp(img, x1 - 1, y, color)


IRON = (118, 118, 124, 255)
IRON_L = (168, 168, 176, 255)
IRON_D = (62, 62, 70, 255)
BLACK = (18, 18, 22, 255)
ORANGE = (214, 112, 28, 255)
ORANGE_L = (240, 168, 56, 255)
RED = (188, 36, 28, 255)
RED_L = (230, 78, 48, 255)
YELLOW = (228, 196, 48, 255)
GREEN = (46, 158, 70, 255)
GREEN_L = (96, 214, 110, 255)
WHITE = (232, 232, 236, 255)
BLUE = (48, 92, 168, 255)
COPPER = (188, 98, 52, 255)
SMOKE = (40, 36, 36, 255)
EXHAUST = (255, 140, 32, 255)


def draw_missile_item(accent, accent2, stripe: bool = False, diamond: bool = False, heavy: bool = False):
    img = canvas(16)
    # vertical missile, nose at top
    fill(img, 6, 1, 10, 4, accent)          # nose
    fill(img, 7, 0, 9, 1, accent2)
    fill(img, 5, 4, 11, 13, IRON if not heavy else IRON_D)
    fill(img, 6, 4, 10, 13, IRON_L if not heavy else IRON)
    fill(img, 5, 13, 11, 15, SMOKE)         # exhaust housing
    fill(img, 6, 15, 10, 16, EXHAUST)
    fill(img, 4, 11, 5, 14, IRON_D)         # fins
    fill(img, 11, 11, 12, 14, IRON_D)
    if stripe:
        fill(img, 5, 7, 11, 9, accent)
    if diamond:
        fill(img, 7, 6, 9, 10, accent)
        fill(img, 6, 7, 10, 9, accent2)
    # highlight
    setp(img, 6, 5, WHITE)
    setp(img, 6, 6, WHITE)
    return img


def draw_launcher():
    img = canvas(16)
    fill(img, 2, 6, 14, 10, IRON_D)         # barrel
    fill(img, 3, 7, 13, 9, IRON)
    fill(img, 12, 6, 15, 10, ORANGE)        # muzzle
    fill(img, 13, 7, 15, 9, ORANGE_L)
    fill(img, 4, 10, 8, 14, IRON_D)         # grip
    fill(img, 5, 10, 7, 13, IRON)
    fill(img, 7, 9, 10, 11, BLACK)          # trigger box
    setp(img, 8, 11, WHITE)
    fill(img, 2, 5, 6, 6, GREEN)            # sight
    return img


def draw_designator():
    img = canvas(16)
    fill(img, 2, 5, 14, 11, IRON_D)
    fill(img, 3, 6, 7, 10, BLUE)
    fill(img, 9, 6, 13, 10, BLUE)
    fill(img, 7, 7, 9, 9, IRON)
    fill(img, 4, 7, 6, 9, GREEN_L)
    fill(img, 10, 7, 12, 9, GREEN_L)
    fill(img, 6, 11, 10, 14, IRON)
    fill(img, 7, 4, 9, 6, RED)              # laser
    setp(img, 8, 3, RED_L)
    return img


def draw_fuel():
    img = canvas(16)
    fill(img, 5, 2, 11, 14, IRON_D)
    fill(img, 6, 3, 10, 13, ORANGE)
    fill(img, 6, 3, 10, 7, ORANGE_L)
    fill(img, 7, 1, 9, 3, IRON)
    fill(img, 6, 14, 10, 15, IRON)
    setp(img, 8, 5, WHITE)
    setp(img, 7, 8, RED)
    return img


def draw_launch_pad():
    img = canvas(16)
    fill(img, 0, 0, 16, 16, IRON_D)
    fill(img, 1, 1, 15, 15, IRON)
    fill(img, 2, 2, 14, 14, IRON_L)
    rect(img, 3, 3, 13, 13, IRON_D)
    fill(img, 5, 5, 11, 11, BLACK)
    rect(img, 6, 6, 10, 10, ORANGE)
    fill(img, 7, 7, 9, 9, ORANGE_L)
    # rivets
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        setp(img, x, y, WHITE)
    return img


def draw_entity(accent, accent2):
    img = canvas(64, IRON_D)
    # body net around (0,0) ~ 40x20
    fill(img, 0, 0, 40, 20, IRON)
    fill(img, 0, 0, 40, 4, IRON_L)
    fill(img, 0, 16, 40, 20, IRON_D)
    # colored nose band
    fill(img, 0, 8, 40, 12, accent)
    fill(img, 16, 8, 24, 12, accent2)
    # panel lines
    for x in range(0, 40, 4):
        fill(img, x, 0, x + 1, 20, IRON_D)
    # nose cuboid at (0, 20) 2x2x3 ~ 10x5
    fill(img, 0, 20, 12, 28, accent)
    fill(img, 2, 21, 10, 27, accent2)
    # fins at (24,0) and (24,5)
    fill(img, 24, 0, 50, 8, IRON_D)
    fill(img, 28, 1, 46, 7, IRON)
    fill(img, 24, 8, 40, 22, IRON_D)
    fill(img, 26, 10, 38, 20, IRON)
    # exhaust glow
    fill(img, 48, 24, 64, 40, SMOKE)
    fill(img, 52, 28, 60, 36, EXHAUST)
    fill(img, 54, 30, 58, 34, WHITE)
    return img


def scale_nn(img, factor: int):
    h = len(img)
    w = len(img[0])
    out = canvas(w * factor)
    for y in range(h):
        for x in range(w):
            c = img[y][x]
            for dy in range(factor):
                for dx in range(factor):
                    out[y * factor + dy][x * factor + dx] = c
    return out


def draw_logo():
    base = canvas(16, (12, 14, 20, 255))
    fill(base, 0, 0, 16, 16, (16, 18, 26, 255))
    # missile
    m = draw_missile_item(ORANGE, ORANGE_L, stripe=True)
    for y in range(16):
        for x in range(16):
            if m[y][x][3] > 0:
                setp(base, x, y, m[y][x])
    return scale_nn(base, 8)


def main() -> None:
    items = ROOT / "assets/apexballistics/textures/item"
    blocks = ROOT / "assets/apexballistics/textures/block"
    entities = ROOT / "assets/apexballistics/textures/entity"

    write_png(items / "he_missile.png", draw_missile_item(ORANGE, ORANGE_L, stripe=True))
    write_png(items / "incendiary_missile.png", draw_missile_item(RED, RED_L, stripe=True))
    write_png(items / "cluster_missile.png", draw_missile_item(YELLOW, ORANGE_L, stripe=True))
    write_png(items / "homing_missile.png", draw_missile_item(GREEN, GREEN_L, diamond=True))
    write_png(items / "bunker_missile.png", draw_missile_item(WHITE, IRON_L, heavy=True, stripe=True))
    write_png(items / "missile_launcher.png", draw_launcher())
    write_png(items / "target_designator.png", draw_designator())
    write_png(items / "rocket_fuel.png", draw_fuel())
    write_png(blocks / "launch_pad.png", draw_launch_pad())

    write_png(entities / "he_missile.png", draw_entity(ORANGE, ORANGE_L))
    write_png(entities / "incendiary_missile.png", draw_entity(RED, RED_L))
    write_png(entities / "cluster_missile.png", draw_entity(YELLOW, ORANGE_L))
    write_png(entities / "homing_missile.png", draw_entity(GREEN, GREEN_L))
    write_png(entities / "bunker_missile.png", draw_entity(WHITE, IRON_L))
    write_png(entities / "bomblet.png", draw_entity(ORANGE, YELLOW))

    write_png(ROOT / "logo.png", draw_logo())
    print("textures written")


if __name__ == "__main__":
    main()
