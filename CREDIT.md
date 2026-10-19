# Adapted code

The following files contain code adapted from [NoellesRoles](https://github.com/agmass/NoellesRoles), by agmass, licensed under LGPL-3.0. These adaptations remain licensed under LGPL-3.0. Modifications include package and mod ID changes, removal of unused branches, renamed identifiers and adjustments to BTT mechanics. Paths are relative to `dev/hae/btt/`; `client/` refers to the client source tree.

- `component/AbilityState.java`, `component/DisguiseState.java`, `component/ScavengerState.java`.
- `component/CurseState.java`, from `voodoo/VoodooPlayerComponent`; `component/ExecutionerState.java`, from `executioner/ExecutionerPlayerComponent`; `component/CorpseState.java`, from `coroner/BodyDeathReasonComponent`.
- `client/mixin/game/StowawayHidePsychosisItems.java`.
- `client/mixin/game/ExecutionerHudMixin.java`.
- `client/spirit/SpiritCamera.java` and `client/renderer/DoorHighlightRenderer.java`.
- `mixin/game/FakeRevolverMixin.java`, from `mixin/jester/FakeRevolverMixin`.
