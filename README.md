# IMPORTANT
This mod is currently a very early alpha/technical demo. Do not expect a feature-complete or survival-friendly experience (currently accessible only in creative). Expect bugs, crashes, save instability between versions, and general lack of polish. Things **will** break.

# What is Satiscraftory?
Satiscraftory is a factory automation mod inspired by the game Satisfactory. It is built from the ground up to support massive, world-spanning factories, allowing you to harvest and process infinite resources by solving logistical challenges. 
## Chunk-independent Simulation
Every factory component always ticks globally and continues to produce resources, even if the chunks they are in are completely unloaded - and this includes other dimensions.
## ​⚠️ Technical Tradeoffs
​To achieve chunk-independent simulation, this mod operates entirely on its own closed backend. What this means is:
- ​Limited vanilla interoperability: With the exception of containers, factory components cannot interact with redstone, item entities, hoppers, mobs, etc. Containers function like vanilla chests and can be used as a bridge.
- ​No tech mod compatibility: Because this mod completely bypasses standard block entity ticking for factory simulation, it cannot connect to pipes, cables, or machines from any other tech mods.

​Think of it as a standalone factory game running inside Minecraft.
# Getting Started
Find the creative tab, and grab one of each item (choose whichever belt you want, and Consumers are optional)
## Factory Components
**Producer:** Infinitely generates a single item type (currently only raw iron). Has a single output

**Belt:** Transports items between every other factory component. Has 3 shapes: straight, corner, and slope. Can also have its direction reversed by right clicking. Has only one output, but can take an input from any direction, effectively allowing for mergers

**Machine:** Converts item(s) into other item(s) determined by the machine's recipe (currently only raw iron -> iron ingot). Has one input and one output

**Container:** Effectively functions as a chest, storing 27 slots of any items. Has one input and one output

**Consumer:** Destroys any item. Has one input
## Recommended Mods
- [Sodium](https://modrinth.com/mod/sodium): Massively increases FPS with large amounts of factory components/belt items on screen. In some extreme cases, FPS can be more than doubled (e.g. when 5,000 moving items are on screen)
- [Spark](https://modrinth.com/mod/spark): Immensely useful for finding performance bottlenecks and viewing system resource usage. Also greatly helps in the process of reporting performance issues when you provide a Spark report
- [Entity Culling](https://modrinth.com/mod/entityculling): Helps a bit with block entity culling (most notably for belts)
- [FerriteCore](https://modrinth.com/mod/ferrite-core) & [ModernFix](https://modrinth.com/mod/modernfix): Memory optimizations, among other things
- [WorldEdit](https://modrinth.com/plugin/worldedit): Copy/Paste factories to create stress tests or make building repetitive factories faster. There will eventually (probably) be official features in Satiscraftory for these sorts of things later on

# Additional Performance Notes
TPS slows to a crawl when a massive amount of belts are in loaded chunks near a player. This is not necessarily a critical issue, but something that should be kept in mind when stress testing. This is due to the network overhead of sending so many sync packets, and while the packets *are* batched in some capacity (150 belt lines, which is 2400 belts), having around 100K belts is still enough to break it. This shouldn't realistically be anywhere close to normal use case, but may still be further optimized eventually. 

When factories are spread apart, as you would normally do in a regular playthrough, performance shouldn't be much of a concern. This is a test in a completely empty superflat world with no mob spawning or anything else: https://spark.lucko.me/IAj24rSlrH
# Known Issues
- Occasional minor visual glitches with belt items, most often when handing off from one belt line to the next
# Feedback
Having other people stress test their system, report bugs and any other feedback is incredibly valuable! Please feel free to share **any** thoughts - whether it's an issue, feature request, something you like or don't like, whatever. 

When reporting LOGISTICAL performance issues, include:
- Exactly how many total factory components exist, and how many are in loaded chunks. Use the `/factory count` and `/factory loaded` commands
- Link to results of a Spark profiler, run for at least 30+ seconds. If Spark is unavailable in your case, provide an average TPS and players on server as a bare minimum
- CPU specs (RAM shouldn't be as much of an issue)

When reporting RENDERING performance issues, include:
- Approximately how many factory components are actively being rendered, and how many are in client chunks. Use the `/factory rendered` command
- Average FPS when the factory components are on screen, and average FPS you would have if you were normally going about your world with no factories on screen
- Any other notable rendering mods installed, such as Iris. If using shaders, name the shader pack
