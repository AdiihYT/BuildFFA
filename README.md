# BuildFFA
Implements a BuildFFA/KnockbackFFA gamemode.

## Current features
- Disappearing blocks (auto-regen, block states: green, yellow, orange, red)
- Default kit (with pre-saveable inventory)
- Map cycle system (every X mins the map changes) - Implemented with [ASWM](https://github.com/Paul19988/Advanced-Slime-World-Manager)
- Default game rules for each map (eg. autorespawn, no falldamage, no death messages etc.)
- Setup worlds as game maps (display name, spawnpoint, voidkill height)
- Killstreak counter
- PlaceholderAPI soft-depend support
- Heal-on-kill
- Instant void kill at certain Y pos
- Kill commands
- **Special items:**
- > Trampoline - boosts you up 20-30 blocks on right click
- > Soon more to be implemented...

## TO-DO features
- Implement bonuses (current ideas: ENDER_PEARL_SAVER, STRENGTH, POISON_HIT, SPEED)

## Admin commands
Base command: **/buildffa** or **/bffa**

**Subcommands:**
- /bffa give (player) (item) (amount)
- /bffa reload
- /bffa setmap (map)
- /bffa skipmap
- /bffa activatebonus (bonus enum) (activator player) *(not implemented yet)*
- /bffa resetbonuses *(not implemented yet)*

## Setup commands
Base command: **/buildffa setup** or **/bffa setup**

**Subcommands:**
- /bffa setup create (world)
- /bffa setup setdisplayname (map) (displayname)
- /bffa setup setspawn (map)
- /bffa setup setvoidkillheight (map) (y)
