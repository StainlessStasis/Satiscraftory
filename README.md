# IMPORTANT
This mod is currently a very early alpha/technical demo. Do not expect a feature-complete or survival-friendly experience (currently accessible only in creative). Expect bugs, crashes, save instability between versions, and general lack of polish. Things **will** break.

# What is Satiscraftory?
Satiscraftory is a factory automation mod inspired by the game Satisfactory. It is built from the ground up to support massive, world-spanning factories, allowing you to harvest and process infinite resources by solving logistical challenges. 
## Chunk-independent Simulation
Every factory component always ticks globally and continues to produce resources, even if the chunks they are in are completely unloaded - and this includes other dimensions.
## ​⚠️ Technical Tradeoffs
​To achieve chunk-independent simulation, this mod operates entirely on its own closed backend. What this means is:
- ​No vanilla interoperability: Factory components cannot interact with redstone, vanilla item entities, hoppers, mobs, etc.
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