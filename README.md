# Backpack in Inventory

A NeoForge 1.21.1 add-on for Sophisticated Backpacks. Opening the survival
inventory displays the selected backpack beside the complete vanilla player
inventory, so items can be moved between both without opening a separate screen.

## Features

- Keeps Sophisticated Backpacks storage, search, sorting, transfer controls,
  upgrades, settings, nesting controls, slot limits, and scrolling.
- Keeps the vanilla player model, armor, offhand, 2x2 crafting grid, recipe book,
  main inventory, and hotbar.
- Selects between accessible equipped and carried top-level backpacks. Hover the
  selector icon to see whether the current backpack is equipped or carried.
- Preserves Curios access from the player inventory when Curios is installed.
- Remembers the selected backpack on the server and validates ownership again
  whenever it is opened or switched.
- Leaves creative inventory, spectator behavior, placed backpacks, and the
  existing Sophisticated Backpacks keybind unchanged.
- Includes optional JEI transfer support for the integrated 2x2 crafting grid.

## Requirements

Install the mod on both the client and server. A client-only or server-only setup
cannot safely provide a synchronized custom inventory menu.

- Minecraft 1.21.1
- NeoForge 21.1.243 or newer in the 21.1 line
- Sophisticated Backpacks 3.25.78 through 3.25.x
- Sophisticated Core 1.4.90 through 1.4.x
- Curios and JEI are optional

## Client and server version policy

The client and server do not need matching patch versions while the shared menu
and network protocol remain unchanged. The current protocol version is `1`, and
the current minimum server release is `0.1.6`.

Every release will be labelled as one of the following:

- **Client-only update:** UI layout, textures, tooltips, or other rendering-only
  changes. Update the client jar only; the server can retain its existing jar.
- **Server update required:** menu slot ordering, payloads/codecs, validation,
  persistent data, dependencies, or other shared/server behaviour changed.
  Update both client and server before reconnecting.

Release compatibility so far:

| Client | Minimum server | Update type |
| --- | --- | --- |
| 0.1.12 | 0.1.6 | Client-only |
| 0.1.11 | 0.1.6 | Client-only |
| 0.1.10 | 0.1.6 | Client-only |
| 0.1.9 | 0.1.6 | Client-only |
| 0.1.8 | 0.1.6 | Client-only |
| 0.1.7 | 0.1.6 | Client-only |
| 0.1.6 | 0.1.6 | Server baseline |

The project will only increment the network protocol version when an incompatible
shared change is unavoidable. A protocol bump will always be marked **Server
update required**.

## Building

Java 21 is required. The included wrapper downloads Gradle 8.9:

```bash
./gradlew build
```

The distributable jar is written to `build/libs/`.

## Moving the active backpack

The backpack whose contents are displayed is locked while the combined menu is
open. Click that backpack item to open the normal inventory management screen,
then move it there. A backpack equipped in Curios opens Curios management first;
the next normal inventory opening allows moving the unequipped backpack.

## Project status

Version 0.1.12 is the current in-game-tested client baseline. The automated tests
cover selection fallback and saved selection data; they are not a comprehensive
inventory-interaction or multiplayer test suite. See [ROADMAP.md](ROADMAP.md) for
proposed quality-of-life improvements and additional verification work.

## License

All Rights Reserved, as declared in the mod metadata. No open-source license is
granted. Upstream dependencies retain their respective licenses.
