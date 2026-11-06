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
    assert "FontMixin" in json.loads(jar.read("fonttweaks.mixins.json"))["client"]
print(f"Verified {jars[0].name}: no default override; original font bytes and notices intact")
