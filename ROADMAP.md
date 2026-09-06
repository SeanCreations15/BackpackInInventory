# Improvements to consider

The working baseline is client 0.1.12 with server 0.1.6 or a compatible newer
release. These are proposed changes, not features already implemented.

## Client quality of life

- Add an explicit Manage backpack button and a tooltip explaining why the active
  backpack is locked. Preserve the existing click-to-open-management behaviour.
- Show a Curios page counter and remember the drawer's page between openings.
  Clamp the page before positioning slots after a resize or GUI-scale change.
- Resolve competing space requirements when Curios and a backpack upgrade panel
  are both open. Cover narrow windows, single-column drawers and large backpacks.
- Offer a client preference for automatically opening equipped backpacks only,
  any accessible backpack, or vanilla inventory. Show which backpack is active.
- Move remaining hard-coded button tooltips into language resources.

## Reliability

- Add regression coverage for slot partitions, carried-backpack locks, quick-move
  routing and menu transitions with cursor items and crafting remainders.
- Exercise Curios page changes and resizing with different slot counts. Test
  optional integrations both present and absent.
- Verify a newer client against the supported older server in a dedicated-server
  session; an unchanged payload protocol string alone does not prove menu safety.
- On future incompatible menu or networking changes, bump the protocol and mark
  the release as requiring a server update. Review older protocol-1 releases
  separately: pre-0.1.6 menus do not have the current Curios slot layout.

## Release discipline

- Label every release Client-only update or Server update required, with the
  compatible server baseline and a downloadable JAR checksum.
- Distinguish automated test coverage, startup checks and actual in-game testing.
- Add continuous integration for Java 21 builds and tests.
