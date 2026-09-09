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
- Shows the selected backpack at its full native width beside opened vanilla
  and modded containers, including barrels, crafting tables, anvils, furnaces,
  and Sophisticated Storage chests. Native container controls remain available.
- Routes shift-clicks from the player or backpack into the opened container.
  Items leaving the container target the player first and only overflow into
  the backpack when the player inventory is full.
- Adds a visible Manage Backpack action, a locked-backpack explanation, a
  remembered Curios page, and safer Curios/upgrade-panel spacing.
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
- Curios, JEI, TrashSlot, and Sophisticated Storage are optional

Sophisticated Storage integration currently supports versions 1.5.91 through
1.5.x. Install Sophisticated Storage on both sides when using that feature.

## Client preferences

After the first launch, edit `config/backpackininventory-client.toml` to choose:

- `ANY_BACKPACK` — open the combined inventory for any accessible top-level backpack.
- `EQUIPPED_ONLY` — only use backpacks equipped in armor or Curios slots.
- `VANILLA` — leave the normal inventory and container screens unchanged.
- `chestIntegration` — legacy config-key name that independently enables or
  disables the backpack side panel for all opened containers.

## Client and server version policy

The client and server do not need matching patch versions while the shared menu
and network protocol remain unchanged. Versions 0.3.0 through 0.3.2 use
protocol `3`; servers should use 0.3.2 for the latest Sophisticated Storage
menu hotfixes.

Every release will be labelled as one of the following:

- **Client-only update:** UI layout, textures, tooltips, or other rendering-only
  changes. Update the client jar only; the server can retain its existing jar.
- **Server update required:** menu slot ordering, payloads/codecs, validation,
  persistent data, dependencies, or other shared/server behaviour changed.
  Update both client and server before reconnecting.

Release compatibility so far:

| Client | Minimum server | Update type |
| --- | --- | --- |
| 0.3.2 | 0.3.2 | Server hotfix; protocol-compatible with 0.3.x clients |
| 0.3.1 | 0.3.1 | Server hotfix; protocol-compatible with 0.3.0 clients |
| 0.3.0 | 0.3.0 | Server update required |
| 0.2.2 | 0.2.0 | Client-only |
| 0.2.1 | 0.2.0 | Client-only |
| 0.2.0 | 0.2.0 | Server update required |
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

Version 0.1.12 remains the prior in-game-tested baseline. Version 0.3.0 adds the
general container companion and deterministic transfer routing described above
and requires an updated server. Automated tests cover selection data and fallback
logic; they are not a substitute for multiplayer inventory-interaction testing.
See [ROADMAP.md](ROADMAP.md) for the remaining verification work.

## License

All Rights Reserved, as declared in the mod metadata. No open-source license is
granted. Upstream dependencies retain their respective licenses.
