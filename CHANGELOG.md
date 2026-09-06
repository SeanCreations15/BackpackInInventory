# Changelog

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
