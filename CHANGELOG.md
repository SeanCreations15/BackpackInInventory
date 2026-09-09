# Changelog

## 0.3.4

Client-and-server compatibility fix.

- Support the Sophisticated Storage Create Integration mounted-storage screen,
  including mounted double diamond chests.
- Register companion backpack slots with Sophisticated Core so its integrity
  check accepts them and layout refreshes retain them.

## 0.3.3

Protocol-compatible server hotfix for 0.3.x clients.

- Rebuild the combined menu once when a joined/double Sophisticated Storage
  chest synchronizes a different authoritative slot count during opening.
- Log the complete slot counts if a rebuilt combined menu is still invalid.

## 0.3.2

Protocol-compatible server hotfix for 0.3.x clients.

- Keep large and double Sophisticated Storage chests open when their
  upgrade-column layout causes Sophisticated Core to rebuild the menu slots.
- Validate the stable combined layout directly instead of relying on Core's
  cumulative private extra-slot counter.

## 0.3.1

Protocol-compatible server hotfix for 0.3.0 clients.

- Register the companion backpack cells as Sophisticated Core extra slots so
  Sophisticated Storage's slot-integrity check accepts the combined menu instead
  of closing it on the first server tick.

## 0.3.0

Server update required. Network protocol 3; client and server must both run
0.3.0.

- Show the selected backpack beside vanilla and modded container screens,
  including barrels, crafting tables, anvils, furnaces, and Sophisticated
  Storage chests.
- Preserve the backpack's full 9- or 12-column layout instead of repacking it
  into pages when a large container is open.
- Make shift-click routing deterministic: player and backpack items target the
  opened container; container items target the player first and overflow into
  the backpack only when the player inventory cannot accept them.
- Keep Sophisticated Storage on its native menu and renderer while applying the
  same full-size companion layout.

## 0.2.2

Client-only update compatible with server 0.2.0.

- Prevent Recipe Essentials and similar recipe-book mixins from casting the
  combined backpack menu to Minecraft's `RecipeBookMenu` during screen ticks.

## 0.2.1

Client-only update compatible with server 0.2.0.

- Correct player-slot positioning and rendering in the Sophisticated Storage
  combined screen.
- Draw upgraded chest slot backgrounds at their actual positions so non-square,
  oversized layouts do not leave interactive blank cells.

## 0.2.0

Server update required. Network protocol 2; client and server must both run
0.2.0 or a later compatible protocol-2 release.

- Add an optional Sophisticated Storage integration that preserves the native
  chest screen and presents the selected backpack as a paged panel on its left.
- Add client opening modes for any backpack, equipped backpacks only, or vanilla
  inventory, plus an independent chest-integration switch.
- Add an explicit Manage Backpack button and explain why the open backpack item
  is locked.
- Remember and display the Curios page, clamp it after layout changes, and stop
  Curios from overlapping an expanded backpack upgrade/settings panel.
- Harden backing-item locks for hotbar swaps and quick-move actions.
- Add drawer-layout regression tests and a Java 21 continuous-integration build.

## 0.1.12

Client-only update compatible with server 0.1.6. Established the prior
in-game-tested combined-inventory layout baseline.
