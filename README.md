# Madoku Craft Stacks

Madoku Craft Stacks is an item stacking system that can be tinkered with.
This is perfect for users that love to customize the game to their needs.
This can be done through the MOD's config JSON file.

## Dependencies

This MOD requires Fabric API and Madoku Craft API in order to function properly.
Madoku Craft API provides this MOD's the JSON, DATA, death system.

## Implementation

This MOD by default increases item stacks to 128.
The number can be increased up to 999 items per stack.
This MOD also controls how many items a player drops when they die.
By default, a player drops 50% of their inventory upon death.
When the player dies, the items they drop will despawn after 15 minutes instead of five minutes.
This only affects items dropped by player deaths and can be changed up to 60 minutes.

## Disclaimer

This MOD affects items in player inventories and containers.
If removed from a world, item loss can be expected since Vanilla Minecraft only allows item stacks of 64.
Use with caution and make sure to back up your worlds before using this MOD.