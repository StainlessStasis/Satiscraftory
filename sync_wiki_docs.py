#!/usr/bin/env python3
"""
DISCLAIMER: This tool was made using generative AI (Claude).
I have reviewed it to double check that it isn't doing anything stupid.
However, I have not touched Python in years so my knowledge of it is very limited.
You shouldn't need to ever use this, as it only exists to save me time for making the wiki - but if you do use it, use it at your own risk.

--------------------------------------------------

Syncs Satiscraftory/Manifold datagen output into a format suitable for the wiki

What it does:
  1. Copies lang files
  2. Copies recipes
  3. Converts custom machine_recipes/*.json into the wiki's custom recipe format
  4. Copies WikiDataExporter's rendered item images + item properties into main/wiki
  5. Generates content/*.mdx pages for any item/block that doesn't already have one
     (only for the primary modid - see PRIMARY_MODID note below)
  6. Keeps content/<modid>/_meta.json (page display names) and the
     top-level content/_meta.json (category names) in sync with whatever pages exist on disk
"""

import json
import re
import shutil
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent  # run from repo root, or edit this
DOCS_ROOT = REPO_ROOT / "main" / "wiki"

# Where WikiDataExporter writes its output by default (run dir = main/run).
# Adjust if you set output_path / wiki_exporter.output.path differently.
EXPORTER_OUTPUT_DIR = REPO_ROOT / "main" / "run" / "wiki_exporter" / "output"

# Each entry: (gradle module dir, resource namespace/modid)
MODULES = [
    ("main", "satiscraftory"),
    ("engine", "manifold"),
]

# A sinytra-wiki.json project only has one modid, and content page IDs must
# belong to it - pages for other namespaces (e.g. manifold, the engine lib)
# fail publishing with a namespace mismatch. Only this modid gets content
# pages generated; other modules still get their lang/recipes synced so
# their items resolve correctly inside satiscraftory recipes.
PRIMARY_MODID = json.loads((DOCS_ROOT / "sinytra-wiki.json").read_text(encoding="utf-8"))["modid"]

# Recipe "type" values the wiki can import verbatim (see Game Data docs)
SUPPORTED_VANILLA_TYPES = {
    "minecraft:blasting",
    "minecraft:campfire_cooking",
    "minecraft:crafting_shaped",
    "minecraft:crafting_shapeless",
    "minecraft:smelting",
    "minecraft:smithing_transform",
    "minecraft:smoking",
    "minecraft:stonecutting",
}


def module_paths(module: str, modid: str):
    src = REPO_ROOT / module / "src"
    return {
        "lang_main": src / "main" / "resources" / "assets" / modid / "lang" / "en_us.json",
        "recipe_dir": src / "generated" / "resources" / "data" / modid / "recipe",
        "machine_recipe_dir": src / "generated" / "resources" / "data" / modid / "machine_recipes",
    }


def sync_lang(modid: str, lang_file: Path):
    if not lang_file.exists():
        return
    dest = DOCS_ROOT / "assets" / modid / "lang" / "en_us.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(lang_file.read_text(encoding="utf-8"), encoding="utf-8")
    print(f"[lang]   {modid}: en_us.json -> {dest.relative_to(DOCS_ROOT)}")


def sync_vanilla_recipes(modid: str, recipe_dir: Path):
    if not recipe_dir.exists():
        return
    dest_dir = DOCS_ROOT / "data" / modid / "recipe"
    dest_dir.mkdir(parents=True, exist_ok=True)
    for f in sorted(recipe_dir.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        rtype = data.get("type")
        if rtype in SUPPORTED_VANILLA_TYPES:
            (dest_dir / f.name).write_text(f.read_text(encoding="utf-8"), encoding="utf-8")
            print(f"[recipe] {modid}:{f.stem} ({rtype}) -> copied")
        else:
            print(f"[recipe] {modid}:{f.stem} has unsupported/missing type {rtype!r}, skipped")


def convert_machine_recipes(modid: str, machine_recipe_dir: Path):
    """Convert internal MachineRecipe jsons into the wiki's custom recipe
    format. Slot names are input_0, input_1... output_0, output_1...
    Grouped per machineType so you only need one recipe_type file per
    machine, not per recipe."""
    if not machine_recipe_dir.exists():
        return

    machine_types_seen = set()
    dest_recipe_dir = DOCS_ROOT / "data" / modid / "recipe"
    dest_recipe_dir.mkdir(parents=True, exist_ok=True)

    for f in sorted(machine_recipe_dir.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        machine_type = data["machineType"]  # e.g. "manifold:basic_machine"
        machine_types_seen.add(machine_type)
        mt_namespace, mt_path = machine_type.split(":", 1)

        wiki_recipe = {
            "type": machine_type,
            "input": {},
            "output": {},
        }
        for i, inp in enumerate(data.get("inputs", [])):
            wiki_recipe["input"][f"input_{i}"] = {
                "id": inp["itemId"],
                "count": inp.get("amount", 1),
            }
        for i, out in enumerate(data.get("outputs", [])):
            wiki_recipe["output"][f"output_{i}"] = {
                "id": out["itemId"],
                "count": out.get("amount", 1),
            }

        out_path = dest_recipe_dir / f"{mt_path}_{f.stem}.json"
        out_path.write_text(json.dumps(wiki_recipe, indent=2), encoding="utf-8")
        print(f"[machine] {modid}:{f.stem} ({machine_type}) -> {out_path.name}")

    # Write/refresh a recipe_type stub per machineType so `input_N`/`output_N`
    # slot names above have somewhere to resolve to. Coordinates are placeholders.
    rt_dir = DOCS_ROOT / "data" / modid / "recipe_type"
    rt_dir.mkdir(parents=True, exist_ok=True)
    for machine_type in machine_types_seen:
        mt_namespace, mt_path = machine_type.split(":", 1)
        if mt_namespace != modid:
            continue  # recipe_type file lives under its own namespace's data dir
        rt_file = rt_dir / f"{mt_path}.json"
        if rt_file.exists():
            continue  # don't clobber hand-tuned slot coordinates
        stub = {
            "background": f"{modid}:gui/{mt_path}",
            "input_slots": {"input_0": {"x": 16, "y": 16}},
            "output_slots": {"output_0": {"x": 200, "y": 52}},
        }
        rt_file.write_text(json.dumps(stub, indent=2), encoding="utf-8")
        print(f"[TODO]   wrote placeholder recipe_type {rt_file.relative_to(DOCS_ROOT)}")
        print(f"         -> add a GUI screenshot at assets/{modid}/gui/{mt_path}.png")
        print(f"         -> fix slot coords, and add extra input_N/output_N slots if needed")

    return machine_types_seen


def update_workbenches(modid: str, machine_type_to_blocks: dict):
    """machine_type_to_blocks: {"manifold:basic_processing": ["satiscraftory:constructor_mk1"]}
    Fill this in by hand below once you know which block(s) process which
    machineType - the datagen files don't currently record that mapping."""
    if not machine_type_to_blocks:
        return
    dest = DOCS_ROOT / "data" / modid / "workbenches.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    existing = json.loads(dest.read_text(encoding="utf-8")) if dest.exists() else {}
    existing.update(machine_type_to_blocks)
    dest.write_text(json.dumps(existing, indent=2), encoding="utf-8")
    print(f"[workbenches] updated {dest.relative_to(DOCS_ROOT)}")


def sync_exporter_output():
    """Copies WikiDataExporter's render (item images) and metadata (item
    properties) output into main/wiki

    NOTE: this assumes the exporter writes to <output>/render/... and
    <output>/metadata/... - if nothing gets copied, check
    EXPORTER_OUTPUT_DIR and look at what's actually inside it"""
    if not EXPORTER_OUTPUT_DIR.exists():
        print(f"[export] {EXPORTER_OUTPUT_DIR} not found - run the exportClient run config first")
        return

    render_dir = EXPORTER_OUTPUT_DIR / "render"
    if render_dir.exists():
        dest = DOCS_ROOT / "assets"
        dest.mkdir(parents=True, exist_ok=True)
        shutil.copytree(render_dir, dest, dirs_exist_ok=True)
        print(f"[export] copied rendered images: {render_dir} -> assets/")
    else:
        print(f"[export] no 'render' folder under {EXPORTER_OUTPUT_DIR}, skipped images")

    metadata_dir = EXPORTER_OUTPUT_DIR / "metadata"
    if metadata_dir.exists():
        dest = DOCS_ROOT / "data"
        dest.mkdir(parents=True, exist_ok=True)
        shutil.copytree(metadata_dir, dest, dirs_exist_ok=True)
        print(f"[export] copied item properties: {metadata_dir} -> data/")
    else:
        print(f"[export] no 'metadata' folder under {EXPORTER_OUTPUT_DIR}, skipped properties")


def generate_content_stubs(modid: str, lang_file: Path):
    """One stub content page per item.<modid>.<path> / block.<modid>.<path>
    lang key. Never overwrites an existing page."""
    if not lang_file.exists():
        return
    lang = json.loads(lang_file.read_text(encoding="utf-8"))
    content_dir = DOCS_ROOT / "content" / modid
    content_dir.mkdir(parents=True, exist_ok=True)

    pattern = re.compile(rf"^(item|block)\.{re.escape(modid)}\.(.+)$")
    for key, name in lang.items():
        m = pattern.match(key)
        if not m:
            continue
        kind, path = m.groups()
        page_file = content_dir / f"{path}.mdx"
        if page_file.exists():
            continue  # don't touch hand-authored pages
        page_file.write_text(
            f"---\n"
            f"id: {modid}:{path}\n"
            f"type: {kind}\n"
            f"---\n\n"
            f"# {name}\n\n"
            f"TODO: write a short description of {name}.\n\n"
            f"<PrefabObtaining/>\n\n"
            f"<PrefabUsage/>\n",
            encoding="utf-8",
        )
        print(f"[page]   generated stub content/{modid}/{path}.mdx")


def update_content_meta(modid: str, lang_file: Path):
    """Keeps content/<modid>/_meta.json (page -> display name) in sync, and
    ensures content/_meta.json (category folder -> display name) has an
    entry for this modid. Merges with existing entries rather than
    overwriting, so manual renames/reordering survive re-runs."""
    if not lang_file.exists():
        return
    lang = json.loads(lang_file.read_text(encoding="utf-8"))
    content_dir = DOCS_ROOT / "content" / modid
    content_dir.mkdir(parents=True, exist_ok=True)

    # Per-category _meta.json: one entry per page file that exists on disk
    meta_file = content_dir / "_meta.json"
    meta = json.loads(meta_file.read_text(encoding="utf-8")) if meta_file.exists() else {}

    pattern = re.compile(rf"^(item|block)\.{re.escape(modid)}\.(.+)$")
    name_by_path = {m.group(2): name for key, name in lang.items() if (m := pattern.match(key))}

    changed = False
    for page_file in sorted(content_dir.glob("*.mdx")):
        entry_key = page_file.name  # e.g. "iron_plate.mdx"
        if entry_key not in meta:
            display_name = name_by_path.get(page_file.stem, page_file.stem)
            meta[entry_key] = display_name
            changed = True
    if changed:
        meta_file.write_text(json.dumps(meta, indent=2), encoding="utf-8")
        print(f"[meta]   updated content/{modid}/_meta.json")

    # Top-level content/_meta.json: one entry per category (modid) folder
    root_meta_file = DOCS_ROOT / "content" / "_meta.json"
    root_meta = json.loads(root_meta_file.read_text(encoding="utf-8")) if root_meta_file.exists() else {}
    if modid not in root_meta:
        root_meta[modid] = modid.replace("_", " ").title()
        root_meta_file.write_text(json.dumps(root_meta, indent=2), encoding="utf-8")
        print(f"[meta]   added '{modid}' to content/_meta.json")


def main():
    # Fill this in as you learn which machine (block) handles which recipe type.
    # e.g. {"manifold:basic_processing": ["satiscraftory:constructor_mk1"]}
    KNOWN_WORKBENCHES = {}

    print(f"Primary modid (from sinytra-wiki.json): {PRIMARY_MODID}")

    for module, modid in MODULES:
        paths = module_paths(module, modid)
        print(f"\n=== {module} ({modid}) ===")
        sync_lang(modid, paths["lang_main"])
        sync_vanilla_recipes(modid, paths["recipe_dir"])
        convert_machine_recipes(modid, paths["machine_recipe_dir"])
        update_workbenches(modid, KNOWN_WORKBENCHES.get(modid, {}))

        if modid == PRIMARY_MODID:
            generate_content_stubs(modid, paths["lang_main"])
            update_content_meta(modid, paths["lang_main"])
        else:
            print(f"[page]   skipping content pages for '{modid}' (not the project's modid)")

    print(f"\n=== exporter output ===")
    sync_exporter_output()

    print("\nDone. Remaining manual work:")
    print(" - Add crafting-table workbench mapping if any recipes use crafting_shaped/shapeless:")
    print(f'     data/{PRIMARY_MODID}/workbenches.json -> {{"minecraft:crafting_shaped": ["minecraft:crafting_table"]}}')
    print(" - Draw real GUI background images + slot coords for each recipe_type stub")
    print(" - Fill in workbenches.json for custom machine types")
    print(" - Flesh out the TODO description in each generated content page")
    print(f" - Delete content/manifold/*.mdx if you don't intend to register it as its own wiki project")


if __name__ == "__main__":
    main()