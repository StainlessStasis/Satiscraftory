# READ THIS PLEASE
This mod is currently a very early **alpha/technical demo**. Do not expect a feature-complete or survival-friendly experience. Expect bugs, crashes, save instability (between mod versions), and general lack of polish. Things **will** break.

# What is Satiscraftory?
Satiscraftory, as you might have guessed, is a factory automation mod inspired by the game Satisfactory. It is built from the ground up to support massive, world-spanning factories, allowing you to harvest and process infinitely renewable resources by solving logistical challenges. 
## A True Factory Mod
Like in any actual factory game, every factory component always ticks globally across the world and continues to produce resources, even when the chunks they are in are completely unloaded. Blocks/BlockEntities are only an abstraction for rendering and to provide interactibility, and the simulation of the factory runs entirely separate.
## ​⚠️ Technical Tradeoffs ⚠️
This mod operates entirely on its own simulation layer so that it is not reliant on chunkloading. What this means is:
- **​Limited vanilla interoperability** - With the exception of Containers, factory components cannot interact with redstone, item entities, hoppers, mobs, etc. Containers function like vanilla chests and can be used as a bridge between Satiscraftory and vanilla/other mods.
- ​**No integration with other tech mods** - Since the actual simulation of factories is self-contained, Satiscraftory cannot connect to pipes, cables, or machines from any other tech mods. Also, as mentioned above, you *could* use Containers as a bridge, but obviously this would be dependent on chunk loading. Using Containers in this way somewhat defeats the whole intent of the mod, but do whatever you want.

![Factory](https://private-user-images.githubusercontent.com/99687421/627061696-ba870e98-3533-4dd5-b9fa-008cf4897b24.gif?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3ODUxNDU1NzksIm5iZiI6MTc4NTE0NTI3OSwicGF0aCI6Ii85OTY4NzQyMS82MjcwNjE2OTYtYmE4NzBlOTgtMzUzMy00ZGQ1LWI5ZmEtMDA4Y2Y0ODk3YjI0LmdpZj9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNjA3MjclMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjYwNzI3VDA5NDExOVomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWVmYjdmYzYxODAyZDAwYjBmZmU5MWM2MzZhMWQ0OTk5ZmQ3ZWUxNTViNGQzZTJkNmY4OGU5ZDBjOTMyYjUyMGImWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnJlc3BvbnNlLWNvbnRlbnQtdHlwZT1pbWFnZSUyRmdpZiJ9.wokmhXYRLZVU5FnQqwcoHexGdB4GjQf4oePONZ2HOeM)

## Getting Started
1. **Locate a Resource Node:** Explore the world to find Iron or Copper resource nodes (or use `/findnode`).
2. **Place a Miner:** Build a Miner on top of the node. Miners output raw ore at a rate based on node purity.
3. **Build a Power Grid:**
   * Place a **Biomass Burner** and load it with organic fuel (leaves, wood, etc.).
   * Use **Cables** to connect your Miner to the Biomass Burner. Use **Cable Cutters** to cut any misplaced cables.
   * Place **Power Poles** to expand your power grid and link multiple factory components together.
4. **Build Your Factory:** Connect **Belts** from your Miner going to **Machines** to automate production lines. Use **Splitters** and **Mergers** for more complex logistics.

> For more detailed documentation, see the **[Satiscraftory Wiki](https://github.com/StainlessStasis/Satiscraftory/wiki/1.-Getting-Started)**.

## Recommended Mods
- [Sodium](https://modrinth.com/mod/sodium): Massively increases FPS with large amounts of factory components/belt items on screen. In some extreme cases, FPS can be more than doubled. In my testing with 5000 belt items (2500 belts), FPS went from ~22 to ~48
- [Spark](https://modrinth.com/mod/spark): Immensely useful for finding performance bottlenecks and viewing system resource usage. Also greatly helps in the process of reporting performance issues when you provide a Spark report
- [Entity Culling](https://modrinth.com/mod/entityculling): Helps a bit with block entity culling (most notably for belts)
- [FerriteCore](https://modrinth.com/mod/ferrite-core) & [ModernFix](https://modrinth.com/mod/modernfix): Memory optimizations, among other things
- [WorldEdit](https://modrinth.com/plugin/worldedit): Copy/Paste factories to create stress tests or make building repetitive factories faster. There will eventually (probably) be official features in Satiscraftory for these sorts of things later on. *Note: may not work as expected with things like power grid connections*

# Additional Performance Notes
**Note:** This section was written shortly after initial release - most of it should still apply the same, but things may have changed since. Feel free to do your own testing and report results
## Tick Lag (CPU & Networking)
TPS slows to a crawl when a massive amount of belts are in loaded chunks near a player. This is not necessarily a critical issue, but something that should be kept in mind when stress testing. This is due to the network overhead of sending so many sync packets, and while the packets *are* batched in some capacity (150 belt lines, which is 2400 belts), having around 100K belts is still enough to break it. This shouldn't realistically be anywhere close to normal use case, but may still be further optimized eventually. 

When factories are spread apart, as you would normally do in a regular playthrough, performance shouldn't be much of a concern. This is a test with 100K (unloaded) belts in a completely empty superflat world with no mob spawning or anything else: https://spark.lucko.me/IAj24rSlrH
## FPS
Rendering is easily the largest bottleneck of the mod right now. As mentioned above in the recommended mods section, 5000 moving items (2500 belts) on screen dropped to less than 50 FPS, even with Sodium.

While 5000 on-screen items is still pushing the upper bounds of what's normally expected in a playthrough, there is still massive room for improvement - especially considering that this is *without* having a ton of animated machines on screen, which will only worsen the FPS problem when that time comes. This will be fixed in the near future once [Flywheel](https://github.com/Engine-Room/Flywheel)'s 26.1.2 port is finalized. Flywheel will allow Satiscraftory to take advantage of instanced rendering and hopefully push those FPS numbers up a decent bit.
# Known Issues
- Side-loading a belt puts the items onto the belt at the start of that belt lane, instead of the exact block. This "feature" will likely be entirely removed in the future, which is why it isn't fixed yet - use the actual Merger block
- Occasional minor visual glitches with belt items, most often when handing off from one belt line to the next

# Roadmap
Listed approximately in order; things may change, and small patches may fall in between
- **0.5.1:** Recipes to obtain factory components in survival. Basic survival gameplay loop
- **0.6.0:** Smelters and Constructors to replace generic Machine
- **0.7.0:** Assemblers (the first multi-input machine) and more items that require the Assembler, such as reinforced iron plates
- **0.8.0:** Fluids, pipes, Water Extractor, and Coal Plant. Very large update, maybe even larger in scope than 0.5.0. Expect it to take a while
- **Other:** Expect more items or smaller features from Satisfactory (e.g. Blade Runners, AWESOME Sink). The goal is to fully flesh out tier 1 & 2 progression, and have some elements of tier 3, for the mod to be considered ready for a full release. Also Space Elevator to come eventually...
- **Future:** In the future, I would like to add some more Minecraft elements to the mod, and not just copy Satisfactory 1:1. For example, automating common vanilla building blocks, adding biome-specific and dimension-specific custom resources, and maybe reworking progression a bit to utilize more vanilla stuff. But that's far away, and for now the focus is making a functional factory game

# Get Involved
## Help Needed!
Currently, the bottleneck of the mod is art. I am not an artist by any means, but I do what I can to make things not just boring placeholders. However, trying to develop the mod while also handling all of the art is quite a large task which slows down development tremendously

So, if you are an artist, animator, or sound designer, and you want to help out, please join the [Discord server](https://discord.gg/A7nnazW8gX) or message me directly (stasis_the_shattered)
## Feedback
Having other people stress test their system, report bugs, and any other feedback is incredibly valuable! Please feel free to share **any** thoughts - whether it's an issue, feature request, something you like or don't like, whatever. This mod is still in its early stages so I'm open to pretty much anything

## Reporting Issues
When reporting logical (CPU, RAM) performance issues, include:
- Exactly how many total factory components exist, and how many are in loaded chunks. Use the `/factory count` and `/factory loaded` commands
- Link to results of a Spark profiler and run it for at least 30+ seconds. If Spark is unavailable in your case, provide an average TPS and players on server as a bare minimum. More info is always helpful
- (If available) CPU specs, RAM dedicated to the Minecraft instance, and total system RAM

When reporting rendering performance issues, include:
- Approximately how many factory components are actively being rendered, and how many are in client-loaded chunks. Use the `/factory rendered` command
- Average FPS when the factory components are on screen, and average FPS you would have if you were normally going about your world with no factories on screen
- Any notable rendering mods installed, such as Iris. If using shaders, name the shader pack
- GPU specs

## Credits
Some assets (mainly models and sounds) are derivatives of other works. For more information, see [ATTRIBUTIONS.md](https://github.com/StainlessStasis/Satiscraftory/blob/main/ATTRIBUTIONS.md)
