## Madoku Craft: Items

Madoku Craft: Items is a configurable Item system.
It allows users to customize Items to their specific needs.
This system splits Items into different Categories.
You can add and remove Items from the system.

## Dependencies

- Fabric API
- Madoku Craft API

## Implementation

This MOD uses the same category-driven item model as the main Madoku Craft item system.
Items can be classified as fuel, misc, farming, tool, or armor.
Tool and armor items can inherit properties from their category defaults, while fuel and misc items can override stack behavior.
Secondary categories are supported for cross-system tagging such as farming and composter rules.
This MOD also contains a Rarity system, which applies to tool and armor items.
Higher rarity increases an item's stats, and tools get a rarity indicator in the UI.
