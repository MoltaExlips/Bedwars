# Bedwars Map Setup: Quick Guide

## 1. Create a New Map
```
/mapsetup create <mapId>
```
- Example: `/mapsetup create arena1`
- You will be teleported to a new void world and given a setup wand.

## 2. Set Map Region
- Use the setup wand **without selecting a team**:
  - **Right-click** a block: Set **Pos1** (first corner)
  - **Left-click** a block: Set **Pos2** (opposite corner)

## 3. Configure Teams
```
/mapsetup teams
```
- Click a team color to select it.
- With a team selected, use the wand:
  - **Right-click**: Set team **spawn point**
  - **Left-click**: Set team **bed location**
- Use `/mapsetup shopkeeper` to set the shopkeeper location for the selected team.

## 4. Map Settings
```
/mapsetup settings
```
- Change map name, min/max players, etc.

## 5. Save the Map
```
/mapsetup save
```
- Your map is now ready to use!

## 6. (Optional) Cancel Setup
```
/mapsetup cancel
```
- Exit setup mode and discard changes.

---
**Tip:**
- Use `/mapsetup edit <mapId>` to edit an existing map.
- Use `/mapsetup list` to see all maps. 