# READ THIS PLEASE
This mod is currently a very early **alpha/technical demo**. Do not expect a feature-complete or survival-friendly experience. Expect bugs, crashes, save instability (between mod versions), and general lack of polish. Things **will** break.

# What is Satiscraftory?
Satiscraftory, as you might have guessed, is a factory automation mod inspired by the game Satisfactory. It is built from the ground up to support massive, world-spanning factories, allowing you to harvest and process infinitely renewable resources by solving logistical challenges. 
## Chunk-independent Simulation
Every factory component always ticks globally and continues to produce resources, even if the chunks they are in are completely unloaded - and this includes other dimensions. This sounds like it would be horribly laggy, but the mod is built with this fact in mind, and optimized accordingly. See the performance notes at the bottom of the page for more info.
## ​⚠️ Technical Tradeoffs ⚠️
​To achieve chunk-independent simulation, this mod operates entirely on its own closed backend. What this means is:
- **​Limited vanilla interoperability** - With the exception of containers, factory components cannot interact with redstone, item entities, hoppers, mobs, etc. Containers function like vanilla chests and can be used as a bridge between Satiscraftory and vanilla/other mods.
- ​**No integration with other tech mods** - Because block entities are used solely for rendering, Satiscraftory cannot connect to pipes, cables, or machines from any other tech mods. Think of it as a standalone factory game. Also, as mentioned above, you *could* use containers as a bridge, but obviously this would be dependent on chunk loading. Using containers in this way somewhat defeats the whole intent of the mod, but do whatever you want.

​<img width="1919" height="1012" alt="image" src="https://github.com/user-attachments/assets/bd4fb7d9-81f4-4de7-82a0-b31f437e1b5f" />

# Getting Started
Find the creative tab on the second page labeled Factory Components, and grab one of each item. There are 3 speeds of belts - choose whichever you want, or use them all together. Consumers are optional if you're using containers.
## Factory Components
*Note: the bulleted dev tools only work in creative mode.*

**Producer:** The start of your factory. Infinitely generates a single item type.
- Right click with an item in hand to produce that item.
- Right click with an empty hand to increment the production interval. Hold sneak to decrement it.

**Belt:** Transports items between every other factory component. Has 3 shapes: straight, corner, and slope. Belts only have one output, but can take an input from any direction, effectively allowing for mergers (will be officially added later).
- Right click to reverse the belt's direction.

**Machine:** Converts item(s) into other item(s) determined by the machine's recipe. Defaults to converting raw iron into iron ingots. Recipes can be added as datapacks under `data/<your_namespace>/machine_recipes`.
- Use `/factory setrecipe <recipe_id>` while looking at a machine to change its recipe. There is another built in recipe to turn iron ingots into iron plates which you can use to test this.

**Container:** Effectively functions as a chest, storing 27 slots of any items. Can also output items to act as a storage buffer. When in loaded chunks, containers function the same as a vanilla chest, accepting and pushing items through hoppers. This functionality *can* be used if you wish to integrate Satiscraftory with vanilla/other mods, but obviously it is dependent on the chunks being loaded

**Consumer:** Destroys any item. That's it.
## Recommended Mods
- [Sodium](https://modrinth.com/mod/sodium): Massively increases FPS with large amounts of factory components/belt items on screen. In some extreme cases, FPS can be more than doubled. In my testing with 5000 belt items (2500 belts), FPS went from ~22 to ~48
- [Spark](https://modrinth.com/mod/spark): Immensely useful for finding performance bottlenecks and viewing system resource usage. Also greatly helps in the process of reporting performance issues when you provide a Spark report
- [Entity Culling](https://modrinth.com/mod/entityculling): Helps a bit with block entity culling (most notably for belts)
- [FerriteCore](https://modrinth.com/mod/ferrite-core) & [ModernFix](https://modrinth.com/mod/modernfix): Memory optimizations, among other things
- [WorldEdit](https://modrinth.com/plugin/worldedit): Copy/Paste factories to create stress tests or make building repetitive factories faster. There will eventually (probably) be official features in Satiscraftory for these sorts of things later on

# Additional Performance Notes
## Tick Lag (CPU & Networking)
TPS slows to a crawl when a massive amount of belts are in loaded chunks near a player. This is not necessarily a critical issue, but something that should be kept in mind when stress testing. This is due to the network overhead of sending so many sync packets, and while the packets *are* batched in some capacity (150 belt lines, which is 2400 belts), having around 100K belts is still enough to break it. This shouldn't realistically be anywhere close to normal use case, but may still be further optimized eventually. 

When factories are spread apart, as you would normally do in a regular playthrough, performance shouldn't be much of a concern. This is a test in a completely empty superflat world with no mob spawning or anything else: https://spark.lucko.me/IAj24rSlrH
## FPS
Rendering is easily the largest bottleneck of the mod right now. As mentioned above in the recommended mods section, 5000 moving items (2500 belts) on screen dropped to less than 50 FPS, even with Sodium.

While 5000 on-screen items is still pushing the upper bounds of what's normally expected in a playthrough, there is still massive room for improvement, especially considering that this is *without* having a ton of animated machines on screen. This will be fixed in the near future once [Flywheel](https://github.com/Engine-Room/Flywheel)'s 26.1.2 port is finalized. Flywheel will allow Satiscraftory to take advantage of instanced rendering and hopefully push those FPS numbers up a decent bit.
# Commands
`/factory count [optional: belts/consumers/containers/machines/produces]` - Outputs the global amount of factory components. Includes unloaded chunks and other dimensions<br>
`/factory loaded [optional: same as above]` - Outputs the amount of factory components in loaded chunks<br>
`/factory rendered [optional: same as above]` - Outputs the amount of factory components in chunks loaded by your client<br>
`/factory setrecipe <recipe> [at/force]` - Sets a machine's recipe, optionally at the specified block coordinate (in the same dimension). The `force` option will force the machine to clear its buffers and stop crafting so that it can set the new recipe immediately<br>
`/factory [freeze/unfreeze]` - Freezes/unfreezes the ticking of all factories, separately from the server tick
# Known Issues
- Merging/side-loading a belt puts the items onto the belt at the start of that belt lane, instead of the exact block 
- Occasional minor visual glitches with belt items, most often when handing off from one belt line to the next
- Factory block models clip into each other and cause z-fighting. Size of the models is intended, but they are still placeholders. Will eventually disallow placing them too close to each other, and some will become multiblocks
# Feedback
Having other people stress test their system, report bugs, and any other feedback is incredibly valuable! Please feel free to share **any** thoughts - whether it's an issue, feature request, something you like or don't like, whatever. This mod is still in its early stages so I'm open to pretty much anything

## Reporting Issues
When reporting logical (CPU, RAM) performance issues, include:
- Exactly how many total factory components exist, and how many are in loaded chunks. Use the `/factory count` and `/factory loaded` commands
- Link to results of a Spark profiler and run it for at least 30+ seconds. If Spark is unavailable in your case, provide an average TPS and players on server as a bare minimum. More info is always helpful
- (If available) CPU specs and RAM dedicated to the Minecraft instance

When reporting rendering performance issues, include:
- Approximately how many factory components are actively being rendered, and how many are in client-loaded chunks. Use the `/factory rendered` command
- Average FPS when the factory components are on screen, and average FPS you would have if you were normally going about your world with no factories on screen
- Any notable rendering mods installed, such as Iris. If using shaders, name the shader pack
- GPU specs
