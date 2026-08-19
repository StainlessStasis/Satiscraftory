#!/usr/bin/env python3
"""
DISCLAIMER: This tool was made using generative AI (Claude).
I have reviewed it to double check that it isn't doing anything stupid.
However, I have not touched Python in years so my knowledge of it is very limited.
You shouldn't need to ever use this, as it only exists to save me time for making the wiki - but if you do use it, use it at your own risk.

--------------------------------------------------

Syncs Satiscraftory/Manifold datagen output into a format suitable for the wiki

What it does:
  1. Copies lang files (folding manifold's entries into satiscraftory - see FOLD_MODIDS)
  2. Copies recipes (same folding applies)
  3. Converts custom machine_recipes/*.json into the wiki's custom recipe format
  4. Copies WikiDataExporter's rendered item images + item properties into main/wiki,
     folding folded namespaces straight into the primary modid and sorting images
     into item/block subfolders based on the lang file
  5. Generates content/<modid>/item/*.mdx and content/<modid>/block/*.mdx pages
     for anything that doesn't already have one
  6. Injects a "Machine Recipe" section directly onto each output item's own
     content page (after whatever else is on it), listing every machine
     recipe that produces that item - icon, amount and items/min for each
     input/output, plus the recipe's duration in ticks. Safe to re-run: the
     section lives between HTML comment markers and gets replaced in place.
  7. Keeps every _meta.json (page/folder display names) in sync with what
     actually exists on disk
"""

import json
import re
from pathlib import Path
import shutil

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
# belong to it. Manifold is just the engine, not a separate product, so its
# content gets folded into the primary modid everywhere: content page ids,
# lang keys, recipe/ingredient references, images, and icons all get
# rewritten from "manifold:x" to "satiscraftory:x" as they're copied in.
PRIMARY_MODID = json.loads((DOCS_ROOT / "sinytra-wiki.json").read_text(encoding="utf-8"))["modid"]
FOLD_MODIDS = {"manifold"}

# Source-namespaced ids that should never get a content page (folded or not).
EXCLUDED_IDS = {"manifold:producer", "manifold:power_producer"}

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

# MDX doesn't support HTML comments (`<!-- -->`) - only JS-style `{/* */}`
# ones - so the markers have to use that syntax or MDX compilation fails.
MACHINE_RECIPE_MARKER_START = "{/* MACHINE_RECIPES:START */}"
MACHINE_RECIPE_MARKER_END = "{/* MACHINE_RECIPES:END */}"

# Maps the singular kind used in lang keys/frontmatter (item.<modid>.<path>,
# type: item) to the plural top-level content folder name.
KIND_FOLDER = {"item": "items", "block": "blocks"}


def target_modid(source_modid: str) -> str:
    return PRIMARY_MODID if source_modid in FOLD_MODIDS else source_modid


def fold_id(resource_id: str) -> str:
    """Rewrites a 'manifold:x' resource id to the primary modid's
    namespace, regardless of which module's file it came from - cross-
    module references (e.g. main's recipes citing manifold's shared
    machine type) need this just as much as manifold's own files do."""
    namespace, sep, rest = resource_id.partition(":")
    if sep and namespace in FOLD_MODIDS:
        return f"{PRIMARY_MODID}:{rest}"
    return resource_id


def fold_json(obj):
    """Recursively applies fold_id to every dict key and string value in a
    parsed JSON structure."""
    if isinstance(obj, str):
        return fold_id(obj)
    if isinstance(obj, list):
        return [fold_json(v) for v in obj]
    if isinstance(obj, dict):
        return {fold_id(k): fold_json(v) for k, v in obj.items()}
    return obj


def module_paths(module: str, modid: str):
    src = REPO_ROOT / module / "src"
    return {
        "lang_main": src / "main" / "resources" / "assets" / modid / "lang" / "en_us.json",
        "recipe_dir": src / "generated" / "resources" / "data" / modid / "recipe",
        "machine_recipe_dir": src / "generated" / "resources" / "data" / modid / "machine_recipes",
    }


def sync_lang(source_modid: str, dest_modid: str, lang_file: Path):
    if not lang_file.exists():
        return
    lang = json.loads(lang_file.read_text(encoding="utf-8"))

    # lang keys look like "item.manifold.cable_cutter" - fold the modid segment
    rewritten = {}
    for key, value in lang.items():
        parts = key.split(".", 2)
        if len(parts) == 3 and parts[1] in FOLD_MODIDS:
            parts[1] = PRIMARY_MODID
            key = ".".join(parts)
        rewritten[key] = value
    lang = rewritten

    dest = DOCS_ROOT / "assets" / dest_modid / "lang" / "en_us.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    existing = json.loads(dest.read_text(encoding="utf-8")) if dest.exists() else {}
    existing.update(lang)  # merge - folded modules share the primary's lang file
    dest.write_text(json.dumps(existing, indent=2), encoding="utf-8")
    print(f"[lang]   {source_modid} -> assets/{dest_modid}/lang/en_us.json ({len(lang)} keys)")


def sync_vanilla_recipes(source_modid: str, dest_modid: str, recipe_dir: Path):
    if not recipe_dir.exists():
        return
    dest_dir = DOCS_ROOT / "data" / dest_modid / "recipe"
    dest_dir.mkdir(parents=True, exist_ok=True)
    for f in sorted(recipe_dir.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        rtype = data.get("type")
        if rtype not in SUPPORTED_VANILLA_TYPES:
            print(f"[recipe] {source_modid}:{f.stem} has unsupported/missing type {rtype!r}, skipped")
            continue
        data = fold_json(data)  # no-op unless the recipe references a folded namespace
        (dest_dir / f.name).write_text(json.dumps(data, indent=2), encoding="utf-8")
        print(f"[recipe] {source_modid}:{f.stem} ({rtype}) -> data/{dest_modid}/recipe/{f.name}")


def convert_machine_recipes(source_modid: str, dest_modid: str, machine_recipe_dir: Path):
    """Convert internal MachineRecipe jsons into the wiki's custom recipe
    format. Slot names are input_0, input_1... output_0, output_1...
    Grouped per machineType so you only need one recipe_type file per
    machine, not per recipe.

    Returns {machine_type: [recipe_record, ...]} so callers can build the
    per-item "Machine Recipe" sections."""
    if not machine_recipe_dir.exists():
        return {}

    recipes_by_machine_type = {}
    dest_recipe_dir = DOCS_ROOT / "data" / dest_modid / "recipe"
    dest_recipe_dir.mkdir(parents=True, exist_ok=True)

    for f in sorted(machine_recipe_dir.glob("*.json")):
        data = json.loads(f.read_text(encoding="utf-8"))
        machine_type = fold_id(data["machineType"])  # e.g. "manifold:basic_machine" -> "satiscraftory:basic_machine"
        mt_namespace, mt_path = machine_type.split(":", 1)

        wiki_recipe = {"type": machine_type, "input": {}, "output": {}}
        for i, inp in enumerate(data.get("inputs", [])):
            wiki_recipe["input"][f"input_{i}"] = {"id": fold_id(inp["itemId"]), "count": inp.get("amount", 1)}
        for i, out in enumerate(data.get("outputs", [])):
            wiki_recipe["output"][f"output_{i}"] = {"id": fold_id(out["itemId"]), "count": out.get("amount", 1)}

        out_path = dest_recipe_dir / f"{mt_path}_{f.stem}.json"
        out_path.write_text(json.dumps(wiki_recipe, indent=2), encoding="utf-8")
        print(f"[machine] {source_modid}:{f.stem} ({machine_type}) -> data/{dest_modid}/recipe/{out_path.name}")

        recipes_by_machine_type.setdefault(machine_type, []).append({
            "id": f.stem,
            "inputs": [(fold_id(inp["itemId"]), inp.get("amount", 1)) for inp in data.get("inputs", [])],
            "outputs": [(fold_id(out["itemId"]), out.get("amount", 1)) for out in data.get("outputs", [])],
            "duration_ticks": data.get("durationTicks"),
        })

    # Write/refresh a recipe_type stub per machineType so `input_N`/`output_N`
    # slot names above have somewhere to resolve to. Coordinates are placeholders.
    rt_dir = DOCS_ROOT / "data" / dest_modid / "recipe_type"
    rt_dir.mkdir(parents=True, exist_ok=True)
    for machine_type in recipes_by_machine_type:
        mt_namespace, mt_path = machine_type.split(":", 1)
        if mt_namespace != dest_modid:
            continue  # recipe_type file lives under its own namespace's data dir
        rt_file = rt_dir / f"{mt_path}.json"
        if rt_file.exists():
            continue  # don't clobber hand-tuned slot coordinates
        stub = {
            "background": f"{dest_modid}:gui/{mt_path}",
            "input_slots": {"input_0": {"x": 16, "y": 16}},
            "output_slots": {"output_0": {"x": 200, "y": 52}},
        }
        rt_file.write_text(json.dumps(stub, indent=2), encoding="utf-8")
        print(f"[TODO]   wrote placeholder recipe_type {rt_file.relative_to(DOCS_ROOT)}")
        print(f"         -> add a GUI screenshot at assets/{dest_modid}/gui/{mt_path}.png")
        print(f"         -> fix slot coords, and add extra input_N/output_N slots if needed")

    return recipes_by_machine_type


def update_workbenches(source_modid: str, dest_modid: str, machine_type_to_blocks: dict):
    """machine_type_to_blocks: {"manifold:basic_processing": ["satiscraftory:constructor_mk1"]}
    Fill this in by hand below once you know which block(s) process which
    machineType - the datagen files don't currently record that mapping."""
    if not machine_type_to_blocks:
        return
    machine_type_to_blocks = fold_json(machine_type_to_blocks)
    dest = DOCS_ROOT / "data" / dest_modid / "workbenches.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    existing = json.loads(dest.read_text(encoding="utf-8")) if dest.exists() else {}
    existing.update(machine_type_to_blocks)
    dest.write_text(json.dumps(existing, indent=2), encoding="utf-8")
    print(f"[workbenches] updated {dest.relative_to(DOCS_ROOT)}")


def classify_paths_by_kind(modid: str) -> dict:
    """Maps a content path (e.g. 'conveyor_belt') to 'item' or 'block' using
    the lang file's item.<modid>.<path> / block.<modid>.<path> keys, so
    rendered images can be sorted into matching item/block subfolders."""
    lang_file = DOCS_ROOT / "assets" / modid / "lang" / "en_us.json"
    if not lang_file.exists():
        return {}
    lang = json.loads(lang_file.read_text(encoding="utf-8"))
    pattern = re.compile(rf"^(item|block)\.{re.escape(modid)}\.(.+)$")
    kind_by_path = {}
    for key in lang:
        m = pattern.match(key)
        if m:
            kind_by_path[m.group(2)] = m.group(1)
    return kind_by_path


def copy_namespaced_tree(src_root: Path, dest_root: Path, label: str, sort_by_kind: bool):
    """Copies each top-level namespace folder under src_root into
    dest_root/<dest_modid>, folding FOLD_MODIDS straight into the primary
    modid (so assets/manifold never gets recreated) and, when sort_by_kind
    is set, sorting files into item/block subfolders via the lang file."""
    if not src_root.exists():
        print(f"[export] no '{src_root.name}' folder under {EXPORTER_OUTPUT_DIR}, skipped {label}")
        return

    for ns_dir in src_root.iterdir():
        if not ns_dir.is_dir():
            continue
        source_modid = ns_dir.name
        dest_modid = target_modid(source_modid)
        kind_by_path = classify_paths_by_kind(dest_modid) if sort_by_kind else {}
        dest_ns_root = dest_root / dest_modid

        count = 0
        for f in ns_dir.rglob("*"):
            if f.is_dir():
                continue
            rel = f.relative_to(ns_dir)
            kind = kind_by_path.get(rel.with_suffix("").as_posix()) if sort_by_kind else None
            dest = (dest_ns_root / kind / rel) if kind else (dest_ns_root / rel)
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(f, dest)
            count += 1

        note = f"folded into {dest_modid}, " if dest_modid != source_modid else ""
        sorted_note = "sorted by item/block" if sort_by_kind else "merged"
        print(f"[export] copied {label}: {src_root.name}/{source_modid} -> {dest_root.name}/{dest_modid} ({note}{sorted_note}, {count} files)")

    # clean up any leftover unfolded namespace dir from older runs
    for fold_modid in FOLD_MODIDS:
        stray = dest_root / fold_modid
        if stray.exists():
            shutil.rmtree(stray)
            print(f"[export] removed stray {dest_root.name}/{fold_modid} (folded into {PRIMARY_MODID})")


def sync_exporter_output():
    """Copies WikiDataExporter's render (item/block images) and metadata
    (item properties) output into main/wiki.

    NOTE: assumes the exporter writes to <output>/render/<namespace>/... and
    <output>/metadata/<namespace>/... - if nothing gets copied, check
    EXPORTER_OUTPUT_DIR and look at what's actually inside it, the exact
    layout isn't fully documented."""
    if not EXPORTER_OUTPUT_DIR.exists():
        print(f"[export] {EXPORTER_OUTPUT_DIR} not found - run the exportClient run config first")
        return

    copy_namespaced_tree(EXPORTER_OUTPUT_DIR / "render", DOCS_ROOT / "assets", "rendered images", sort_by_kind=True)
    copy_namespaced_tree(EXPORTER_OUTPUT_DIR / "metadata", DOCS_ROOT / "data", "item properties", sort_by_kind=False)


def generate_content_stubs(source_modid: str, dest_modid: str, lang_file: Path):
    """One stub content page per item.<source_modid>.<path> / block.<source_modid>.<path>
    lang key, written to content/<dest_modid>/<kind>/<path>.mdx. Never
    overwrites an existing page."""
    if not lang_file.exists():
        return
    lang = json.loads(lang_file.read_text(encoding="utf-8"))
    pattern = re.compile(rf"^(item|block)\.{re.escape(source_modid)}\.(.+)$")

    for key, name in lang.items():
        m = pattern.match(key)
        if not m:
            continue
        kind, path = m.groups()
        if f"{source_modid}:{path}" in EXCLUDED_IDS:
            continue

        content_dir = DOCS_ROOT / "content" / KIND_FOLDER[kind]
        content_dir.mkdir(parents=True, exist_ok=True)
        page_file = content_dir / f"{path}.mdx"
        if page_file.exists():
            continue  # don't touch hand-authored pages
        page_file.write_text(
            f"---\n"
            f"id: {dest_modid}:{path}\n"
            f"type: {kind}\n"
            f"---\n\n"
            f"# {name}\n\n"
            f"TODO: write a short description of {name}.\n\n"
            f"<PrefabObtaining/>\n\n"
            f"<PrefabUsage/>\n",
            encoding="utf-8",
        )
        print(f"[page]   generated stub content/{KIND_FOLDER[kind]}/{path}.mdx")


def update_content_meta():
    """Keeps every _meta.json in sync with what's actually on disk under
    content/: items/_meta.json, blocks/_meta.json, and the top-level
    content/_meta.json (naming the items/blocks folders themselves). Only
    adds missing entries - never overwrites a name you've already
    customized."""
    lang_file = DOCS_ROOT / "assets" / PRIMARY_MODID / "lang" / "en_us.json"
    lang = json.loads(lang_file.read_text(encoding="utf-8")) if lang_file.exists() else {}
    pattern = re.compile(rf"^(item|block)\.{re.escape(PRIMARY_MODID)}\.(.+)$")
    name_by_kind_path = {}
    for key, name in lang.items():
        m = pattern.match(key)
        if m:
            name_by_kind_path[m.groups()] = name

    content_dir = DOCS_ROOT / "content"
    root_meta_file = content_dir / "_meta.json"
    root_meta = json.loads(root_meta_file.read_text(encoding="utf-8")) if root_meta_file.exists() else {}
    root_changed = False

    for kind, folder_name, folder_title in (("item", "items", "Items"), ("block", "blocks", "Blocks")):
        kind_dir = content_dir / folder_name
        if not kind_dir.exists():
            continue
        if folder_name not in root_meta:
            root_meta[folder_name] = folder_title
            root_changed = True

        meta_file = kind_dir / "_meta.json"
        meta = json.loads(meta_file.read_text(encoding="utf-8")) if meta_file.exists() else {}
        changed = False
        for page_file in sorted(kind_dir.glob("*.mdx")):
            if page_file.name not in meta:
                meta[page_file.name] = name_by_kind_path.get((kind, page_file.stem), page_file.stem)
                changed = True
        if changed:
            meta_file.write_text(json.dumps(meta, indent=2), encoding="utf-8")
            print(f"[meta]   updated content/{folder_name}/_meta.json")

    if root_changed:
        root_meta_file.write_text(json.dumps(root_meta, indent=2), encoding="utf-8")
        print(f"[meta]   updated content/_meta.json")


# --- Machine Recipe section injection ---------------------------------------

def find_content_page(item_id: str) -> Path | None:
    """Locates the content/{items,blocks}/<path>.mdx page for a given
    '<modid>:<path>' id. Returns None if the item's namespace isn't this
    project's, or no such page exists yet."""
    namespace, path = item_id.split(":", 1)
    if namespace != PRIMARY_MODID:
        return None
    for folder_name in ("items", "blocks"):
        candidate = DOCS_ROOT / "content" / folder_name / f"{path}.mdx"
        if candidate.exists():
            return candidate
    return None


def icon_ref(item_id: str, kind_by_path: dict) -> str:
    """Resource location of the item's rendered inventory icon, matching
    wherever copy_namespaced_tree() actually put the file. Project items use
    whichever of item/block the lang file says they are; everything else
    (vanilla, other mods) falls back to the universal 'item/' convention the
    wiki itself uses for auto-resolved icons."""
    namespace, path = item_id.split(":", 1)
    if namespace == PRIMARY_MODID:
        kind = kind_by_path.get(path, "item")
        return f"{namespace}:{kind}/{path}"
    return f"{namespace}:item/{path}"


def item_cell(item_id: str, kind_by_path: dict) -> str:
    """Icon + name, linking to the item's own page. Only items belonging to
    this project or vanilla have a rendered icon/resolvable link available -
    anything else (a different mod) is shown as plain text."""
    namespace, _path = item_id.split(":", 1)
    if namespace not in (PRIMARY_MODID, "minecraft"):
        return f"`{item_id}`"
    icon = icon_ref(item_id, kind_by_path)
    return f'![](@{icon}) <ContentLink id="{item_id}"/>'


def fmt_rate(value: float) -> str:
    return f"{value:.2f}".rstrip("0").rstrip(".")


def machine_type_title(machine_type: str, machine_type_to_blocks: dict) -> str:
    blocks = machine_type_to_blocks.get(machine_type)
    if blocks:
        return " / ".join(f'<ContentLink id="{b}"/>' for b in blocks)
    _, path = machine_type.split(":", 1)
    return path.replace("_", " ").title()


def render_recipe_block(recipe: dict, kind_by_path: dict, machine_type_to_blocks: dict) -> str:
    duration = recipe["duration_ticks"]
    lines = [f"#### {machine_type_title(recipe['machine_type'], machine_type_to_blocks)}", ""]
    lines.append("| | Item | Amount | Rate |")
    lines.append("|---|---|---|---|")
    for direction, items in (("Input", recipe["inputs"]), ("Output", recipe["outputs"])):
        for item_id, amount in items:
            rate = f"{fmt_rate(amount * 1200 / duration)}/min" if duration else "—"
            lines.append(f"| {direction} | {item_cell(item_id, kind_by_path)} | {amount} | {rate} |")
    lines.append("")
    if duration:
        lines.append(f"**Craft time:** {duration} ticks ({fmt_rate(duration / 20)}s)")
    lines.append("")
    return "\n".join(lines)


def inject_machine_recipes(all_machine_recipes: dict, machine_type_to_blocks: dict):
    """Adds/updates a 'Machine Recipe' section on each output item's own
    content page, after whatever else is already on it, showing every
    machine recipe that produces that item - icon, amount and items/min for
    each input/output, plus the recipe's duration in ticks.

    Safe to re-run: the section is wrapped in MACHINE_RECIPE_MARKER_START/END
    HTML comments and replaced in place, so hand-written content elsewhere on
    the page is left untouched."""
    if not all_machine_recipes:
        return

    kind_by_path = classify_paths_by_kind(PRIMARY_MODID)

    recipes_by_output_item: dict[str, list[dict]] = {}
    for machine_type, recipes in all_machine_recipes.items():
        for r in recipes:
            for item_id, _amount in r["outputs"]:
                if item_id.split(":", 1)[0] != PRIMARY_MODID:
                    continue  # only this project's own pages get a section
                recipes_by_output_item.setdefault(item_id, []).append({**r, "machine_type": machine_type})

    for item_id, recipes in recipes_by_output_item.items():
        page_file = find_content_page(item_id)
        if page_file is None:
            print(f"[recipe-inject] no content page found for {item_id}, skipped")
            continue

        blocks = [render_recipe_block(r, kind_by_path, machine_type_to_blocks) for r in recipes]
        heading = "## Machine Recipe" if len(blocks) == 1 else "## Machine Recipes"
        section = "\n".join([MACHINE_RECIPE_MARKER_START, heading, "", *blocks, MACHINE_RECIPE_MARKER_END])

        text = page_file.read_text(encoding="utf-8")
        if MACHINE_RECIPE_MARKER_START in text and MACHINE_RECIPE_MARKER_END in text:
            pattern = re.compile(re.escape(MACHINE_RECIPE_MARKER_START) + r".*?" + re.escape(MACHINE_RECIPE_MARKER_END), re.DOTALL)
            new_text = pattern.sub(section, text)
        else:
            new_text = text.rstrip("\n") + "\n\n" + section + "\n"

        if new_text != text:
            page_file.write_text(new_text, encoding="utf-8")
            print(f"[recipe-inject] updated {page_file.relative_to(DOCS_ROOT)} ({len(recipes)} recipe(s))")


def main():
    # Fill this in as you learn which machine (block) handles which recipe type.
    # e.g. {"manifold:basic_processing": ["satiscraftory:constructor_mk1"]}
    KNOWN_WORKBENCHES = {}

    print(f"Primary modid (from sinytra-wiki.json): {PRIMARY_MODID}")
    print(f"Folding into primary: {', '.join(FOLD_MODIDS) or '(none)'}")
    all_machine_recipes = {}  # machine_type -> [recipe_record, ...], across all modules
    all_workbenches = {}      # machine_type -> [block_id, ...], folded, across all modules

    for module, source_modid in MODULES:
        dest_modid = target_modid(source_modid)
        paths = module_paths(module, source_modid)
        print(f"\n=== {module} ({source_modid} -> {dest_modid}) ===")

        sync_lang(source_modid, dest_modid, paths["lang_main"])
        sync_vanilla_recipes(source_modid, dest_modid, paths["recipe_dir"])
        recipes_by_type = convert_machine_recipes(source_modid, dest_modid, paths["machine_recipe_dir"])
        for machine_type, recipes in recipes_by_type.items():
            all_machine_recipes.setdefault(machine_type, []).extend(recipes)

        module_workbenches = KNOWN_WORKBENCHES.get(source_modid, {})
        update_workbenches(source_modid, dest_modid, module_workbenches)
        all_workbenches.update(fold_json(module_workbenches))

        if dest_modid == PRIMARY_MODID:
            generate_content_stubs(source_modid, dest_modid, paths["lang_main"])
        else:
            print(f"[page]   skipping content pages for '{source_modid}' (not the project's modid)")

    update_content_meta()

    print(f"\n=== machine recipes ===")
    inject_machine_recipes(all_machine_recipes, all_workbenches)

    print(f"\n=== exporter output ===")
    sync_exporter_output()

if __name__ == "__main__":
    main()