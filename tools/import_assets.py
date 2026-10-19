#!/usr/bin/env python3
"""
Jokino Jewels — art import pipeline.

Takes the original art folder (as delivered by the artist) and produces every
drawable / mipmap the app references through `ui/Assets.kt`:

    python3 tools/import_assets.py "/path/to/Jokino Jewels art"

What it does
  * trims transparent margins, resizes to sensible on-device sizes,
  * quantizes PNGs with libimagequant (pip install imagequant) when available,
  * builds btn_menu (ornate plate) and btn_plate (same plate without the crest),
  * builds adaptive launcher layers (stage background + logo), legacy icons,
    a themed-icon monochrome layer, the splash logo and the 512 px store icon.

Source → app file mapping is in SOURCES below; change it to swap art.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

try:
    import imagequant  # libimagequant bindings — high quality palette PNGs with alpha
except ImportError:  # pragma: no cover
    imagequant = None

SRC = sys.argv[1] if len(sys.argv) > 1 else "art"
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RES = os.path.join(ROOT, "app", "src", "main", "res")
NODPI = os.path.join(RES, "drawable-nodpi")
STORE = os.path.join(ROOT, "store")
os.makedirs(NODPI, exist_ok=True)
os.makedirs(STORE, exist_ok=True)

SOURCES = {
    "background": "background.jpg",
    "icon": "icon.jpg",
    "logo": "logo.png",
    "plate": "Сапфировая титульная табличка в золоте-1.png",
    "frame": "Золотая рамка с сапфировой вставкой-4.png",
    "round": "Пустая сапфировая кнопка с золотым ободком-5.png",
    "back": "Сапфировая кнопка назад в золотой оправе-3.png",
    "pause": "Золотая кнопка паузы с сапфировой эмалью-2.png",
    # six jester poses
    "mascot_a": "Шут с рубином на прозрачном фоне-9.png",            # presenting, ruby in hand
    "mascot_b": "Золотой шут с парящим рубином-7.png",               # raising a floating ruby
    "mascot_c": "Золотой сапфировый шут с рубиновыми акцентами-6.png",  # welcoming gesture
    "mascot_d": "Изолированный шут с рубином-11.png",                # dancing with a ruby
    "mascot_e": "Роскошный шут в театральном поклоне-8.png",         # theatrical bow (win)
    "mascot_f": "Шут на прозрачном фоне-10.png",                     # standing, ruby up
    # 12 match icons
    "item_01": "Королевская корона с рубиновым акцентом-1.png",
    "item_02": "Рубиново-сапфировая театральная полумаска-2.png",
    "item_03": "Рубиновая шкатулка-сердце в золотой оправе-9.png",
    "item_04": "Золотой сундук с рубинами и сапфирами-4.png",
    "item_05": "Сапфировый подарок с рубиновой лентой-8.png",
    "item_06": "Золотой кубок с рубиновым кристаллом-7.png",
    "item_07": "Золотой ключ с сапфирово-рубиновой головкой-5.png",
    "item_08": "Золотые песочные часы с рубинами-6.png",
    "item_09": "Церемониальный золотой колокольчик с сапфиром и рубином-4.png",
    "item_10": "Золотая арфа с сапфирами и рубинами-2.png",
    "item_11": "Ювелирный скипетр шута с рубином и сапфиром-3.png",
    "item_12": "Золотая лира с рубиновым орнаментом-6.png",
    # decorations
    "deco_cap": "Рубиново-сапфировый колпак шута-1.png",
    "deco_fan": "Рубиново-сапфировый маскарадный веер-3.png",
    "deco_pendant": "Рубиновый кулон с сапфирами и колокольчиком-9.png",
    "deco_medal": "Сапфировая медаль с золотой звездой-7.png",
    "deco_feather": "Сапфировое перо с рубиновым кончиком-5.png",
    "deco_ticket": "Сапфировый театральный билет с рубиновым ромбом-8.png",
}


def src(key):
    return Image.open(os.path.join(SRC, SOURCES[key]))


def trim(im, threshold=12, pad=4):
    im = im.convert("RGBA")
    alpha = im.split()[3].point(lambda v: 255 if v > threshold else 0)
    box = alpha.getbbox() or (0, 0, im.width, im.height)
    x0, y0, x1, y1 = box
    return im.crop((max(0, x0 - pad), max(0, y0 - pad), min(im.width, x1 + pad), min(im.height, y1 + pad)))


def fit(im, max_w=None, max_h=None):
    w, h = im.size
    s = 1.0
    if max_w:
        s = min(s, max_w / w)
    if max_h:
        s = min(s, max_h / h)
    if s < 1.0:
        im = im.resize((max(1, round(w * s)), max(1, round(h * s))), Image.LANCZOS)
    return im


def square(im, size, fill_ratio=0.94):
    """Centre an image in a transparent square canvas (keeps aspect)."""
    im = trim(im)
    inner = int(size * fill_ratio)
    scale = min(inner / im.width, inner / im.height)
    im = im.resize((max(1, round(im.width * scale)), max(1, round(im.height * scale))), Image.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(im, ((size - im.width) // 2, (size - im.height) // 2))
    return out


def save_png(im, path, quantize=True):
    im = im.convert("RGBA")
    if quantize and imagequant is not None:
        try:
            im = imagequant.quantize_pil_image(im, dithering_level=1.0, max_colors=256, min_quality=0, max_quality=95)
        except Exception:
            pass
    im.save(path, optimize=True)


def out(name):
    return os.path.join(NODPI, name)


# ------------------------------------------------------------------ backgrounds & logo
def build_background():
    bg = src("background").convert("RGB")
    bg = bg.resize((1080, 1920), Image.LANCZOS)
    bg.save(out("bg_main.jpg"), quality=85, optimize=True, progressive=True)


def build_logo():
    logo = fit(trim(src("logo")), max_w=900)
    save_png(logo, out("logo.png"))
    return logo


# ------------------------------------------------------------------ plates & buttons
def build_plates():
    plate = src("plate").convert("RGBA")
    # Ornate plate: crest + bar (label sits on the bar, ~70 % down — see Assets.BTN_MENU_LABEL_Y).
    menu = trim(plate.crop((0, 0, plate.width, 700)))
    menu = fit(menu, max_w=1000)
    save_png(menu, out("btn_menu.png"))

    # Simple plate: only the bar. Rebuilt as left cap + a stretched uniform middle column
    # + right cap, which removes the crest's centre diamond without visible seams.
    bar = plate.crop((0, 292, plate.width, 700))
    cap_w = int(bar.width * 0.16)
    left = bar.crop((0, 0, cap_w, bar.height))
    right = bar.crop((bar.width - cap_w, 0, bar.width, bar.height))
    mid_src = bar.crop((int(bar.width * 0.30), 0, int(bar.width * 0.30) + 8, bar.height))
    mid = mid_src.resize((bar.width, bar.height), Image.BILINEAR)
    rebuilt = mid.copy()
    fade = int(cap_w * 0.45)

    def cap_mask(flip):
        m = Image.new("L", (cap_w, bar.height), 255)
        d = ImageDraw.Draw(m)
        for i in range(fade):
            v = int(255 * (1 - (i + 1) / fade))
            x = cap_w - fade + i
            d.line([(cap_w - 1 - x if flip else x, 0), (cap_w - 1 - x if flip else x, bar.height)], fill=v)
        return m

    # Caps keep their own alpha where opaque (gems stick out) and crossfade into the middle.
    for img, x, flip in ((left, 0, False), (right, bar.width - cap_w, True)):
        region = rebuilt.crop((x, 0, x + cap_w, bar.height))
        blended = Image.composite(img, region, cap_mask(flip))
        rebuilt.paste(blended, (x, 0))
    bar = rebuilt
    bar = trim(bar)
    bar = fit(bar, max_w=1000)
    save_png(bar, out("btn_plate.png"))

    frame = fit(trim(src("frame")), max_w=1000)
    save_png(frame, out("frame_question.png"))

    save_png(square(src("round"), 360, 1.0), out("plate_round.png"))
    save_png(square(src("back"), 240, 1.0), out("icon_back.png"))
    save_png(square(src("pause"), 240, 1.0), out("icon_pause.png"))
    return menu.size, bar.size, frame.size


def build_mascots():
    sizes = {}
    for key in ("mascot_a", "mascot_b", "mascot_c", "mascot_d", "mascot_e", "mascot_f"):
        im = fit(trim(src(key)), max_h=900)
        save_png(im, out(f"{key}.png"))
        sizes[key] = im.size
    return sizes


def build_items():
    for i in range(1, 13):
        key = f"item_{i:02d}"
        save_png(square(src(key), 384), out(f"{key}.png"))
    sizes = {}
    for key in ("deco_cap", "deco_fan", "deco_pendant", "deco_medal", "deco_feather", "deco_ticket"):
        im = fit(trim(src(key)), max_w=480, max_h=480)
        save_png(im, out(f"{key}.png"))
        sizes[key] = im.size
    return sizes


# ------------------------------------------------------------------ launcher, splash, store
def build_launcher(logo):
    densities = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}

    bg_full = src("background").convert("RGB")
    side = bg_full.width
    top = (bg_full.height - side) // 2 - int(side * 0.05)
    stage = bg_full.crop((0, top, side, top + side))

    icon = src("icon").convert("RGB")

    for dens, s in densities.items():
        folder = os.path.join(RES, f"mipmap-{dens}")
        os.makedirs(folder, exist_ok=True)
        layer = round(108 * s)

        # Adaptive background: the blue stage.
        stage.resize((layer, layer), Image.LANCZOS).save(os.path.join(folder, "ic_launcher_background.png"), optimize=True)

        # Adaptive foreground: the logo inside the 66dp safe zone (+ a soft glow for legibility).
        fg = Image.new("RGBA", (layer, layer), (0, 0, 0, 0))
        target = int(layer * 74 / 108)
        lg = logo.copy()
        scale = min(target / lg.width, target / lg.height)
        lg = lg.resize((max(1, round(lg.width * scale)), max(1, round(lg.height * scale))), Image.LANCZOS)
        glow = Image.new("RGBA", (layer, layer), (0, 0, 0, 0))
        glow_alpha = Image.new("L", (layer, layer), 0)
        glow_alpha.paste(lg.split()[3], ((layer - lg.width) // 2, (layer - lg.height) // 2))
        glow_alpha = glow_alpha.filter(ImageFilter.GaussianBlur(layer * 0.03)).point(lambda v: v * 0.55)
        glow.putalpha(glow_alpha)
        fg.alpha_composite(glow)
        fg.alpha_composite(lg, ((layer - lg.width) // 2, (layer - lg.height) // 2))
        save_png(fg, os.path.join(folder, "ic_launcher_foreground.png"))

        # Themed icon (Android 13+): white silhouette of the logo.
        mono = Image.new("RGBA", (layer, layer), (255, 255, 255, 0))
        mono_alpha = Image.new("L", (layer, layer), 0)
        mono_alpha.paste(lg.split()[3], ((layer - lg.width) // 2, (layer - lg.height) // 2))
        mono.putalpha(mono_alpha)
        save_png(mono, os.path.join(folder, "ic_launcher_monochrome.png"))

        # Legacy (API 24–25) icons: the designed square icon.
        px = round(48 * s)
        big = icon.resize((px * 4, px * 4), Image.LANCZOS)
        for name, shape in (("ic_launcher.png", "rr"), ("ic_launcher_round.png", "circle")):
            m = Image.new("L", big.size, 0)
            d = ImageDraw.Draw(m)
            if shape == "rr":
                d.rounded_rectangle((0, 0, big.width - 1, big.height - 1), radius=int(big.width * 0.2), fill=255)
            else:
                d.ellipse((0, 0, big.width - 1, big.height - 1), fill=255)
            res = Image.new("RGBA", big.size, (0, 0, 0, 0))
            res.paste(big, (0, 0), m)
            save_png(res.resize((px, px), Image.LANCZOS), os.path.join(folder, name))

    icon.resize((512, 512), Image.LANCZOS).save(os.path.join(STORE, "ic_launcher_512.png"), optimize=True)

    # Splash icon (Android 12 splash API): logo inside the central 2/3 circle of a square canvas.
    canvas = 576
    sp = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    target = int(canvas * 0.62)
    lg = logo.copy()
    scale = min(target / lg.width, target / lg.height)
    lg = lg.resize((round(lg.width * scale), round(lg.height * scale)), Image.LANCZOS)
    sp.alpha_composite(lg, ((canvas - lg.width) // 2, (canvas - lg.height) // 2))
    save_png(sp, out("splash_icon.png"))


if __name__ == "__main__":
    build_background()
    logo = build_logo()
    plate_sizes = build_plates()
    mascot_sizes = build_mascots()
    deco_sizes = build_items()
    build_launcher(logo)
    print("logo", logo.size)
    print("btn_menu / btn_plate / frame", plate_sizes)
    print("mascots", mascot_sizes)
    print("deco", deco_sizes)
