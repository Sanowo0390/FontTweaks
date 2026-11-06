"""Check the actual distributable, including exact upstream font bytes."""
import hashlib
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[1]
jars = [p for p in (root / "build/libs").glob("fonttweaks-*.jar")
        if not any(tag in p.name for tag in ("sources", "dev"))]
assert len(jars) == 1, jars
vendor = root / "build/vendor/MisakiGothic2forMC_26_3.zip"
assert hashlib.sha256(vendor.read_bytes()).hexdigest() == (
    "a09b2f0ea1e911fe4dd83fac39953a447a69c42fecc544d9af723a84954eee8d")
with zipfile.ZipFile(jars[0]) as jar, zipfile.ZipFile(vendor) as upstream:
    assert "assets/minecraft/font/default.json" not in jar.namelist()
    assert not any(p.startswith("assets/minecraft/font/") for p in jar.namelist())
    assert jar.read("assets/fonttweaks/font/misaki.json") == upstream.read(
        "assets/minecraft/font/default.json")
    for path in upstream.namelist():
        if path.startswith("assets/misakigothic2/") and not path.endswith("/"):
            assert jar.read(path) == upstream.read(path), path
    assert jar.read("META-INF/THIRD_PARTY_NOTICES.md") == (
        root / "THIRD_PARTY_NOTICES.md").read_bytes()
    mixins = json.loads(jar.read("fonttweaks.mixins.json"))["client"]
    assert "FontMixin" in mixins
    assert "UnihexBitmapMixin" in mixins
    assert "UnihexProviderMixin" not in mixins
    assert not any("GlyphProcessingContext" in path for path in jar.namelist())
    pixel_path = "assets/fonttweaks/font/pixelmplus12_regular.ttf"
    assert hashlib.sha256(jar.read(pixel_path)).hexdigest() == (
        "02f19467ea7cc235cc06c570b7f6c3b0a12a6f682bc8d74c43f2d323d97bcd12")
    pixel_definition = json.loads(jar.read("assets/fonttweaks/font/pixelmplus12.json"))
    assert pixel_definition["providers"][0]["file"] == "fonttweaks:pixelmplus12_regular.ttf"
    assert pixel_definition["providers"][-1] == {"type": "reference", "id": "minecraft:default"}
    assert b"Copyright (C) 2013 itouhiro" in jar.read("META-INF/THIRD_PARTY_NOTICES.md")
print(f"Verified {jars[0].name}: no default override; original font bytes and notices intact")

