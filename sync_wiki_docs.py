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
     then mirrors folded namespaces' images so folded content pages have icons
  5. Generates content/<modid>/item/*.mdx and content/<modid>/block/*.mdx pages
     for anything that doesn't already have one
  6. Generates content/<modid>/machine_recipes/*.mdx - one overview page per
     machine type, listing every recipe it performs
  7. Keeps every _meta.json (page/folder display names) in sync with what
     actually exists on disk
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
# belong to it. Manifold is just the engine, not a separate product, so its
# content gets folded into the primary modid everywhere: content page ids,
# lang keys, recipe/ingredient references, and icons all get rewritten from
# "manifold:x" to "satiscraftory:x" as they're copied in.
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

    Returns {machine_type: [recipe_record, ...]} so callers can build a
    combined "what does this machine craft" overview page."""
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


def sync_exporter_output():
    """Copies WikiDataExporter's render (item images) and metadata (item
    properties) output into main/wiki, in case the exporter/toolkit auto-
    wiring isn't placing them there for you.

    NOTE: this assumes the exporter writes to <output>/render/... and
    <output>/metadata/... - if nothing gets copied, check
    EXPORTER_OUTPUT_DIR and look at what's actually inside it, the exact
    layout isn't fully documented."""
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


def fold_assets():
    """Folded content pages use PRIMARY_MODID-based ids, and an id doubles
    as its icon's asset location - so mirror rendered images for any folded
    namespace into the primary project's asset folder too."""
    for source_modid in FOLD_MODIDS:
        src = DOCS_ROOT / "assets" / source_modid
        if not src.exists():
            continue
        dest = DOCS_ROOT / "assets" / PRIMARY_MODID
        dest.mkdir(parents=True, exist_ok=True)
        shutil.copytree(src, dest, dirs_exist_ok=True)
        print(f"[fold]   mirrored assets/{source_modid} -> assets/{PRIMARY_MODID}")


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

        content_dir = DOCS_ROOT / "content" / dest_modid / kind
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
        print(f"[page]   generated stub content/{dest_modid}/{kind}/{path}.mdx")


def update_content_meta():
    """Keeps every _meta.json in sync with what's actually on disk under
    content/<PRIMARY_MODID>/: item/_meta.json, block/_meta.json, the
    project's own _meta.json (naming the item/block/machine_recipes
    subfolders), and the top-level content/_meta.json. Only adds missing
    entries - never overwrites a name you've already customized."""
    lang_file = DOCS_ROOT / "assets" / PRIMARY_MODID / "lang" / "en_us.json"
    lang = json.loads(lang_file.read_text(encoding="utf-8")) if lang_file.exists() else {}
    pattern = re.compile(rf"^(item|block)\.{re.escape(PRIMARY_MODID)}\.(.+)$")
    name_by_kind_path = {}
    for key, name in lang.items():
        m = pattern.match(key)
        if m:
            name_by_kind_path[m.groups()] = name

    project_dir = DOCS_ROOT / "content" / PRIMARY_MODID
    project_meta_file = project_dir / "_meta.json"
    project_meta = json.loads(project_meta_file.read_text(encoding="utf-8")) if project_meta_file.exists() else {}
    project_changed = False

    for kind, folder_title in (("item", "Items"), ("block", "Blocks")):
        kind_dir = project_dir / kind
        if not kind_dir.exists():
            continue
        if kind not in project_meta:
            project_meta[kind] = folder_title
            project_changed = True

        meta_file = kind_dir / "_meta.json"
        meta = json.loads(meta_file.read_text(encoding="utf-8")) if meta_file.exists() else {}
        changed = False
        for page_file in sorted(kind_dir.glob("*.mdx")):
            if page_file.name not in meta:
                meta[page_file.name] = name_by_kind_path.get((kind, page_file.stem), page_file.stem)
                changed = True
        if changed:
            meta_file.write_text(json.dumps(meta, indent=2), encoding="utf-8")
            print(f"[meta]   updated content/{PRIMARY_MODID}/{kind}/_meta.json")

    if project_changed:
        project_meta_file.write_text(json.dumps(project_meta, indent=2), encoding="utf-8")
        print(f"[meta]   updated content/{PRIMARY_MODID}/_meta.json")

    root_meta_file = DOCS_ROOT / "content" / "_meta.json"
    root_meta = json.loads(root_meta_file.read_text(encoding="utf-8")) if root_meta_file.exists() else {}
    if PRIMARY_MODID not in root_meta:
        root_meta[PRIMARY_MODID] = PRIMARY_MODID.replace("_", " ").title()
        root_meta_file.write_text(json.dumps(root_meta, indent=2), encoding="utf-8")
        print(f"[meta]   added '{PRIMARY_MODID}' to content/_meta.json")


def generate_machine_recipe_pages(recipes_by_machine_type: dict):
    """Writes one overview page per machine type under
    content/<PRIMARY_MODID>/machine_recipes/, listing every recipe that
    machine performs. Items belonging to the current project get a
    <ContentLink/> to their own page; everything else (vanilla, other
    namespaces) is shown as plain text, since ContentLink only supports
    linking within the current project or to vanilla items."""
    if not recipes_by_machine_type:
        return

    section_dir = DOCS_ROOT / "content" / PRIMARY_MODID / "machine_recipes"
    section_dir.mkdir(parents=True, exist_ok=True)

    def link(item_id: str) -> str:
        namespace = item_id.split(":", 1)[0]
        if namespace in (PRIMARY_MODID, "minecraft"):
            return f'<ContentLink id="{item_id}"/>'
        return f"`{item_id}`"  # outside the project - can't be linked

    section_meta = {}
    for machine_type, recipes in recipes_by_machine_type.items():
        mt_namespace, mt_path = machine_type.split(":", 1)
        page_file = section_dir / f"{mt_path}.mdx"

        lines = [f"# {mt_path.replace('_', ' ').title()}\n"]
        for r in recipes:
            inputs = ", ".join(f"{count}x {link(i)}" for i, count in r["inputs"])
            outputs = ", ".join(f"{count}x {link(i)}" for i, count in r["outputs"])
            lines.append(f"### {r['id'].replace('_', ' ').title()}")
            lines.append(f"- **Input:** {inputs}")
            lines.append(f"- **Output:** {outputs}")
            if r["duration_ticks"]:
                lines.append(f"- **Duration:** {r['duration_ticks']} ticks")
            lines.append("")

        page_file.write_text("\n".join(lines), encoding="utf-8")
        section_meta[f"{mt_path}.mdx"] = mt_path.replace("_", " ").title()
        print(f"[machine-page] wrote content/{PRIMARY_MODID}/machine_recipes/{mt_path}.mdx")

    (section_dir / "_meta.json").write_text(json.dumps(section_meta, indent=2), encoding="utf-8")

    parent_meta_file = DOCS_ROOT / "content" / PRIMARY_MODID / "_meta.json"
    parent_meta = json.loads(parent_meta_file.read_text(encoding="utf-8")) if parent_meta_file.exists() else {}
    if "machine_recipes" not in parent_meta:
        parent_meta["machine_recipes"] = "Machine Recipes"
        parent_meta_file.write_text(json.dumps(parent_meta, indent=2), encoding="utf-8")


def main():
    # Fill this in as you learn which machine (block) handles which recipe type.
    # e.g. {"manifold:basic_processing": ["satiscraftory:constructor_mk1"]}
    KNOWN_WORKBENCHES = {}

    print(f"Primary modid (from sinytra-wiki.json): {PRIMARY_MODID}")
    print(f"Folding into primary: {', '.join(FOLD_MODIDS) or '(none)'}")
    all_machine_recipes = {}  # machine_type -> [recipe_record, ...], across all modules

    for module, source_modid in MODULES:
        dest_modid = target_modid(source_modid)
        paths = module_paths(module, source_modid)
        print(f"\n=== {module} ({source_modid} -> {dest_modid}) ===")

        sync_lang(source_modid, dest_modid, paths["lang_main"])
        sync_vanilla_recipes(source_modid, dest_modid, paths["recipe_dir"])
        recipes_by_type = convert_machine_recipes(source_modid, dest_modid, paths["machine_recipe_dir"])
        for machine_type, recipes in recipes_by_type.items():
            all_machine_recipes.setdefault(machine_type, []).extend(recipes)
        update_workbenches(source_modid, dest_modid, KNOWN_WORKBENCHES.get(source_modid, {}))

        if dest_modid == PRIMARY_MODID:
            generate_content_stubs(source_modid, dest_modid, paths["lang_main"])
        else:
            print(f"[page]   skipping content pages for '{source_modid}' (not the project's modid)")

    update_content_meta()

    print(f"\n=== machine recipe pages ===")
    generate_machine_recipe_pages(all_machine_recipes)

    print(f"\n=== exporter output ===")
    sync_exporter_output()
    fold_assets()

if __name__ == "__main__":
    main()