# 0.4.1-alpha (Aug 3, 2026)
## New Features
Added new items!<br>
**Copper Sheet:** 2x copper ingot -> 1x copper sheet; 6 sec (10/min)<br>
**Wire:** 1x copper ingot -> 2x wire; 4 sec (30/min)<br>
**Cable:** 2x wire -> 1x cable; 2 sec (30/min)

## Coming Up
0.5.0 is on the way, and it's bringing **power grids**! I'd estimate it's about 70% done; backend stuff is done, but I need to model and implement power poles & biomass burners. This update will bring machines/miners requiring power, cables with placement preview and daisy-chaining, power poles, and biomass burners. Also, don't worry, there's a config setting to disable power requirements. A survival-friendly gameplay loop is starting to take shape! 

# 0.4.0-alpha (Jul 30, 2026)
## ⚠️ BREAKING CHANGES ⚠️
Any Machine that isn't using the default iron ingot recipe will stop working and will need to have its recipe reset. This is a necessary change to implement the new menu.

## New Features
Added a menu to Machines!
- Right click a Machine to open its menu. Here you can put in and take out items, and see the crafting progress and items per minute
- Added new `/factory presetrecipe <id>` command - any Machines you place will automatically have that recipe applied to them, so you don't have to do it manually for each one. This state is volatile and will reset when the game is restarted

# 0.3.1-alpha (Jul 28, 2026)
## New Features
Added new items!<br>
**Iron Rod:** 1x iron ingot -> 1x iron rod; 4 sec (15/min)<br>
**Screws:** 1x iron rod -> 4x screws; 6 sec (40/min)

## Changes & Fixes
- Fixed Machines not crafting when flushing their outputs, which caused noticeable discrepancies in the expected ratios of factories compared to what they actually produced. This fixes manifold designs not filling up the Machine's buffers, and they now work properly
- Added factory items (iron plate, iron rod, screws) to their own Factory Items creative tab

# 0.3.0-alpha (Jul 27, 2026)
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
