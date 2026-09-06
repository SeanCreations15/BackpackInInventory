# Roadmap

Version 0.2.0 implements the original quality-of-life batch: explicit backpack
management, locked-slot guidance, remembered Curios pagination, safer middle
drawer layout, opening-mode preferences, translated tooltips, CI, and optional
Sophisticated Storage chest integration.

## Remaining verification

- Exercise normal, copper, iron, gold, diamond, and netherite Sophisticated
  Storage chests with different row/column upgrades and both full/empty backpacks.
- Verify chest/backpack drag, split, double-click, hotbar swaps, shift-click,
  upgrades, settings transitions, paging, block break, and distance closure on a
  two-player dedicated server.
- Expand automated coverage around live menu partitions and quick-move behavior
  if upstream provides a practical headless container-test harness.
- Validate narrow-window behavior with JEI, Curios, TrashSlot, and open upgrade
  controls together.

## Release discipline

- Label every release Client-only update or Server update required, with the
  compatible server baseline and a downloadable JAR checksum.
- Distinguish automated test coverage, startup checks and actual in-game testing.
- Continue publishing release type, protocol baseline, artifact checksum, and a
  clear distinction between automated, startup, and in-game verification.
