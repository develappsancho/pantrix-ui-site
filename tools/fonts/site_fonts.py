"""Makes the site's two fallback fonts in core/designsystem's composeResources/font: the characters its pages write
that neither Plus Jakarta Sans nor the library's Pantrix Symbols has. In a browser, Compose draws such a character in a
Noto font it fetches from Google's servers; with these read beside the site's typeface, it fetches none.

    python3 tools/fonts/site_fonts.py NOTO_FOLDER

NOTO_FOLDER holds three fonts, from github.com/google/fonts (ofl/):
    NotoSansArabic[wdth,wght].ttf   ofl/notosansarabic
    NotoSansMath-Regular.ttf        ofl/notosansmath
    NotoSansSymbols2-Regular.ttf    ofl/notosanssymbols2

It needs fontTools (pip install fonttools). Their licence, the fonts' own SIL Open Font License 1.1, is beside them in
composeResources/files/licenses/.
"""

import io
import os
import sys

from fontTools.subset import Options, Subsetter
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

FONTS = os.path.join(os.path.dirname(__file__), "..", "..", "core", "designsystem", "src", "commonMain",
                     "composeResources", "font")

# The Arabic block, its marks of direction, and the spaces: the home page's "ع" and the guides on dates and numbers,
# which write the library's Arabic words and digits.
ARABIC = list(range(0x0600, 0x0700)) + [0x061C, 0x200C, 0x200D, 0x200E, 0x200F, 0x0020, 0x00A0]

# The site's own marks, by the font that has each: the home page's, the search's, the page for coding assistants' and
# the date guide's.
SYMBOLS = {
    "NotoSansMath-Regular.ttf": "↵∿▦◎✓⟳",
    "NotoSansSymbols2-Regular.ttf": "✗",
}

# Plus Jakarta Sans's ascent and descent: a line with a mark in it is as tall as one without.
ASCENT, DESCENT = 1038, -222


def cut(path, unicodes, keep_layout):
    """The font at path at weight 400, with the glyphs of unicodes alone, written and read again."""
    font = TTFont(path)
    if "fvar" in font:
        font = instantiateVariableFont(font, {axis.axisTag: axis.defaultValue for axis in font["fvar"].axes})
    options = Options()
    options.layout_features = ["*"] if keep_layout else []
    options.hinting = False
    options.name_IDs = ["*"]
    options.drop_tables += ["STAT", "DSIG"] + ([] if keep_layout else ["GDEF", "GPOS", "GSUB", "MATH"])
    subsetter = Subsetter(options)
    subsetter.populate(unicodes=unicodes)
    subsetter.subset(font)
    written = io.BytesIO()
    font.save(written)
    written.seek(0)
    return TTFont(written)


def arabic(folder):
    """Noto Sans Arabic's Arabic block, its shaping kept: the letters join as Arabic writes them."""
    font = cut(os.path.join(folder, "NotoSansArabic[wdth,wght].ttf"), ARABIC, keep_layout=True)
    font["name"].setName(
        "Version 2.0; a subset of Noto Sans Arabic for pantrixui.com: the Arabic block at weight 400", 5, 3, 1, 0x409
    )
    return font


def symbols(folder):
    """The site's marks, cut from Noto's fonts and put in one font, the others' glyphs moved into the first."""
    fonts = [cut(os.path.join(folder, name), [ord(c) for c in text], keep_layout=False) for name, text in SYMBOLS.items()]
    base = fonts[0]
    order = base.getGlyphOrder()
    for other in fonts[1:]:
        for code, name in sorted(other.getBestCmap().items()):
            glyph = other["glyf"][name]
            assert not glyph.isComposite() and name not in order, name
            order.append(name)
            base["glyf"].glyphs[name] = glyph
            base["hmtx"].metrics[name] = other["hmtx"].metrics[name]
            for table in base["cmap"].tables:
                if table.isUnicode():
                    table.cmap[code] = name
    base.setGlyphOrder(order)
    base["glyf"].glyphOrder = order
    base["hhea"].ascent, base["hhea"].descent, base["hhea"].lineGap = ASCENT, DESCENT, 0
    os2 = base["OS/2"]
    os2.sTypoAscender, os2.sTypoDescender, os2.sTypoLineGap = ASCENT, DESCENT, 0
    os2.usWinAscent, os2.usWinDescent = ASCENT, -DESCENT
    codes = sorted(base.getBestCmap())
    os2.usFirstCharIndex, os2.usLastCharIndex = codes[0], codes[-1]
    names = base["name"]
    names.names = [n for n in names.names if n.nameID not in (0, 1, 3, 4, 5, 6, 16, 17, 21, 22, 25)]
    for name_id, text in {
        0: "Copyright 2022 The Noto Project Authors (https://github.com/notofonts/math, https://github.com/notofonts/symbols)",
        1: "Pantrix Site Symbols",
        3: "1.000;PANTRIX;PantrixSiteSymbols-Regular",
        4: "Pantrix Site Symbols Regular",
        5: "Version 1.000; a subset of Noto Sans Math and Noto Sans Symbols 2 for pantrixui.com",
        6: "PantrixSiteSymbols-Regular",
    }.items():
        names.setName(text, name_id, 3, 1, 0x409)
    return base


def main(folder):
    for name, font in (("noto_sans_arabic.ttf", arabic(folder)), ("site_symbols.ttf", symbols(folder))):
        path = os.path.join(FONTS, name)
        font.save(path)
        print(f"Wrote {os.path.normpath(path)}: {len(TTFont(path).getBestCmap())} characters, {os.path.getsize(path)} bytes")


if __name__ == "__main__":
    main(sys.argv[1])
