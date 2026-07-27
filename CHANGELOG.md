# 0.3.0-alpha 
## New Features
Added resource nodes and miners!<br><br>
**Resource Nodes:** Patches of renewable resources which spawn around the world and can be extracted by miners. This update adds two nodes: iron, and copper. Nodes have a purity of impure, normal, or pure which affect the miner's production rate.<br><br>
**Miners:** Can only be placed on a resource node; extracts renewable resources from the node. Extraction rate is based on the resource node's purity. There is also a placement preview to show where/if a miner can be placed.

These are both steps toward creating a survival-friendly gameplay loop, but there are still no recipes for creating miners or any other factory components. Maybe some basic crafting recipes and more machine recipes to come in 0.3.1 perchance?

## Changes & Fixes
- Changed producers to buffer a whole stack of their production item, instead of holding only one pending item

# 0.2.0-alpha (Jul 21, 2026)
## New Features
Added splitters and mergers!
## Changes & Fixes
- Fixed belts being twice as fast as intended (e.g. belt mk. 1 being 120/min instead of 60/min)
- Fixed producers and machines still producing items after being broken
- Fixed containers transferring all of their items within a single tick. They are now rate-limited to 1 item per tick
- 
# 0.1.0-alpha (Jul 20, 2026)
Initial release. Built the foundation for the mod with barebones features and placeholders.
