# Bedwars Map Setup: Streamlined Guide

## 🚀 Quick Start
```
/mapsetup create <mapId>
```
- Example: `/mapsetup create arena1`
- You'll be teleported to a new world with setup tools automatically provided

## 📋 Guided Setup Process

The new setup system provides **visual progress tracking** and **step-by-step guidance**:

### 🎯 Your Setup Tools
- **Golden Axe (Setup Wand)**: Context-aware tool that changes based on your current step
- **Compass (Setup Menu)**: Right-click to open the progress dashboard anytime

### 📊 Step 1: Map Region Setup
**Goal**: Define the map boundaries that will reset between games

- **Right-click with wand**: Set Position 1 (first corner)
- **Left-click with wand**: Set Position 2 (opposite corner)
- ✅ **Auto-advance**: Once both positions are set, you'll automatically move to team setup

### 👥 Step 2: Team Configuration  
**Goal**: Set up at least 2 teams (up to 4 supported)

1. **Open Team Menu**: Use compass or `/mapsetup teams`
2. **Select a Team**: Click on Red, Blue, Green, or Yellow team
3. **Configure Team Locations**:
   - **Right-click with wand**: Set team spawn point
   - **Left-click with wand**: Set team bed location
   - **Command**: `/mapsetup shopkeeper` to set shopkeeper location
4. **Repeat**: Configure at least 2 teams to continue

**Visual Indicators**:
- 🟢 **Emerald Block**: Fully configured team
- 🟡 **Gold Block**: Currently selected team
- ⚪ **Colored Wool**: Unconfigured team

### ⚙️ Step 3: Map Settings
**Goal**: Finalize map details

- **Map Name**: Click name tag in GUI or `/mapsetup name <newName>`
- **Player Limits**: 
  - Min players: `/mapsetup minplayers <number>` (2-16)
  - Max players: `/mapsetup maxplayers <number>` (2-16)

### 💾 Step 4: Save Your Map
- **GUI**: Click the "Save Map" button when ready
- **Command**: `/mapsetup save`

## 🔧 Additional Commands

### Management Commands
```bash
/mapsetup edit <mapId>     # Edit an existing map
/mapsetup list             # List all maps  
/mapsetup delete <mapId>   # Delete a map
/mapsetup cancel           # Cancel current setup
```

### Quick Setup Commands (Alternative to GUI)
```bash
/mapsetup pos1             # Set region position 1
/mapsetup pos2             # Set region position 2
/mapsetup spawn            # Set team spawn (team must be selected)
/mapsetup bed              # Set team bed (team must be selected)
/mapsetup shopkeeper       # Set shopkeeper location
```

## 💡 Pro Tips

### 🎯 **Efficient Workflow**
1. Use the **compass** to check your progress anytime
2. The **wand automatically updates** to show what you need to do next
3. **Visual progress bar** shows completion percentage
4. **Auto-advancement** moves you to the next step when ready

### 🔄 **Smart Navigation**
- **Progress Dashboard**: Right-click compass for overview
- **Team Menu**: Shows completion status with color coding
- **Context-Aware Wand**: Instructions change based on current step
- **Back Navigation**: Easy return to previous menus

### ⚡ **Quick Setup**
- **Minimum Requirements**: 2 teams configured + region set
- **Recommended**: 4 teams for full gameplay experience
- **Auto-Save Prompts**: System suggests when you're ready to save

---

## 🆘 Troubleshooting

**Wand not working?**
- Ensure you've selected a team for team setup phase
- Check you're in the correct setup step

**Can't continue?**
- Verify you have at least 2 teams fully configured
- Ensure both Pos1 and Pos2 are set for the region

**Lost your tools?**
- Right-click the compass to reopen the setup menu
- Use `/mapsetup cancel` and start over if needed

**Need help?**
- The progress dashboard shows exactly what step you're on
- Each step provides clear hints for what to do next 