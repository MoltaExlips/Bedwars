# Bedwars Plugin

A Bedwars plugin for Minecraft servers with complete game mechanics, GUI interfaces, custom maps, kits, and shop system.

## Installation

### Prerequisites
- Minecraft Server (Spigot/Paper) 1.20.4+
- Java 17 or higher
- Maven (for building)

### Building the Plugin

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd Bedwars
   ```

2. **Build with Maven**:
   ```bash
   mvn clean package
   ```

3. **Install the plugin**:
   - Copy the generated JAR file from `target/bedwars-1.0.0.jar` to your server's `plugins` folder
   - Restart your server

### Configuration

The plugin will generate a `config.yml` file in the `plugins/Bedwars/` directory. You can customize:

- **Game Settings**: Player limits, countdown times, game duration
- **Kits**: Starting items for each kit
- **Maps**: Spawn points, bed locations, generator positions
- **Shop Items**: Prices and categories
- **Messages**: Customizable game messages

## Usage

### Player Commands
- `/bedwars` - Open the main menu
- `/bedwars join <map> <team> <kit>` - Join a specific game
- `/bedwars leave` - Leave current game
- `/bedwars stats` - View your statistics
- `/bedwars shop` - Open shop (in-game only)
- `/bedwars currency` - Check your currency (in-game only)

### Admin Commands
- `/bedwarsadmin reload` - Reload configuration
- `/bedwarsadmin stats` - View server statistics
- `/bedwarsadmin create <mapId>` - Create a new map
- `/bedwarsadmin delete <mapId>` - Delete a map
- `/bedwarsadmin setup <mapId>` - Setup mode for map configuration

### Permissions
- `bedwars.join` - Allow joining games (default: true)
- `bedwars.spectate` - Allow spectating games (default: true)
- `bedwars.admin` - Admin commands (default: op)

## Game Flow

1. **Joining a Game**:
   - Use `/bedwars` to open the main menu
   - Select "Join Game"
   - Choose a map from the available options
   - Select your team (Red, Blue, Green, Yellow)
   - Choose your kit (Default, Warrior, Builder)
   - Wait for the countdown to begin

2. **Gameplay**:
   - Protect your team's bed at all costs
   - Collect resources from generators
   - Purchase items from the shop using currency
   - Attack enemy teams and destroy their beds
   - Eliminate players whose beds have been destroyed

3. **Victory Conditions**:
   - Destroy all enemy beds
   - Eliminate all enemy players
   - Last team standing wins

## Map Configuration

Maps are configured in `config.yml` under the `maps` section:

```yaml
maps:
  lighthouse:
    name: "Lighthouse"
    description: "A classic 4-team map"
    min-players: 4
    max-players: 8
    teams: 4
    spawn-points:
      red: "100,64,100"
      blue: "-100,64,100"
      green: "100,64,-100"
      yellow: "-100,64,-100"
    bed-locations:
      red: "95,64,95"
      blue: "-95,64,95"
      green: "95,64,-95"
      yellow: "-95,64,-95"
    generators:
      iron:
        - "0,64,0"
        - "50,64,50"
      gold:
        - "0,64,0"
      diamond:
        - "25,64,25"
      emerald:
        - "0,64,0"
```

## Kit Configuration

Kits are configured in `config.yml` under the `kits` section:

```yaml
kits:
  default:
    name: "Default"
    description: "Basic kit with starting items"
    items:
      - material: WOODEN_SWORD
        amount: 1
      - material: WOOL
        amount: 16
      - material: STONE
        amount: 16
```

## Shop Configuration

Shop items are configured in `config.yml` under the `shop` section:

```yaml
shop:
  blocks:
    wool:
      cost: 4
      currency: IRON_INGOT
      material: WOOL
      amount: 16
  weapons:
    stone_sword:
      cost: 7
      currency: GOLD_INGOT
      material: STONE_SWORD
      amount: 1
```

## World Generation

Each map creates its own world with:
- **Flat World**: Clean slate for building
- **No Structures**: No villages, temples, or other structures
- **No Bedrock**: Completely blank world
- **Custom Settings**: Optimized for Bedwars gameplay

## Game States

1. **WAITING**: Players can join the game
2. **COUNTDOWN**: Game is about to start (30 seconds by default)
3. **PLAYING**: Active gameplay
4. **ENDING**: Game is finishing
5. **FINISHED**: Game has ended, map resetting

## Technical Features

- **Thread-safe**: Concurrent game management
- **Memory Efficient**: Proper cleanup and resource management
- **Scalable**: Support for multiple simultaneous games
- **Configurable**: Extensive configuration options
- **GUI Integration**: Full inventory-based user interface
- **Statistics Tracking**: Player performance metrics

## Troubleshooting

### Common Issues

1. **Plugin won't load**:
   - Ensure you're using Java 17+
   - Check server logs for error messages
   - Verify all dependencies are present

2. **Maps not loading**:
   - Check map configuration in `config.yml`
   - Ensure world names are unique
   - Verify spawn point coordinates

3. **Games not starting**:
   - Check minimum player count in configuration
   - Ensure teams are properly configured
   - Verify bed locations are valid

### Performance Tips

- Limit the number of simultaneous games
- Use appropriate world generation settings
- Monitor memory usage with large player counts
- Regularly restart the server to clear memory

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License
dont use