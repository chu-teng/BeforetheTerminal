# Before the Terminal

[简体中文](README.md) | **English**

Find allies, conceal your role, and survive a train ride where every passenger may be hiding something.

BTT is a multiplayer social deduction mod for Minecraft, providing an independent game mode built on Wathe and HarpyModLoader.

**Current version: 0.7.0-h1.4 · Alpha.** Builds, resource validation, client/server startup and server round lifecycle checks have passed. Extensive testing with human players is still needed. Reports with versions, logs and reproduction steps are welcome.

## Features

- **Nearly 100 roles**, spanning passengers, murderers and neutrals, with nine role categories.
- **Five modes**: Script, Draft, Duel, Debate and Reminiscence, plus encounters that change each round's rules.
- **Three traits per player**, including general, faction-specific and relationship traits.
- **Interconnected systems**: sanity, madness, needs, executions, wrongful kills, drunkenness and epilogues.
- **Round rewards** and **89 advancements across five tabs**.
- **Data-pack driven maps**, with map selection and voting.

Most modes support **6–24 players**; Debate and Reminiscence support **6–18**. Duel places everyone in the passenger or murderer faction; a lone neutral who meets their objective becomes permanently drunk and receives a gun. Reminiscence hides role information from opening and ability text, allowing players to guess their own role using letters.

## Installation and play

Requires Minecraft **1.21.1**, Java **21** and Fabric Loader **0.17.2+**. Install BTT and its required dependencies on both client and server, using matching versions.

| Dependency | Version |
| --- | --- |
| Fabric API | For 1.21.1; development baseline 0.116.7+1.21.1 |
| [Wathe](https://modrinth.com/mod/XoU7Lf7D) | 1.4.1-1.21.1 |
| [HarpyModLoader](https://modrinth.com/mod/harpymodloader) | 1.2.5-h1.4 |
| Ratatouille | 1.4.3-1.21.1 |
| YetAnotherConfigLib | 3.8.0+1.21.1-fabric or a compatible newer release |
| Simple Voice Chat | Optional; recommended for proximity voice |

**Maps are not included.** Supply a compatible Wathe map and its data pack separately. Custom maps need the control panel, lookout seat, chandelier and wire mechanisms, and other required gameplay facilities.

Right-click the compass in the lobby to select the mode, script and encounter. Use `/maps` to select a registered map. Pull the whistle to start, or use `/wathe:start btt:before_the_terminal <map_identifier>` as an administrator.

Default keys: **G** for abilities, **E** for inventory and available shops, **K** for the guide. In-round messages appear in the action bar. Administrators can use `/forceRole` and `/forceModifier` to configure the next round.

## Building from source

Requires JDK 21. The wrapper uses Gradle 9.2.0 with Loom 1.14.10.

Download these development dependencies from their official projects and place them in `libs/`, keeping the following filenames:

- `wathe-1.4.1-1.21.1.jar`
- `harpymodloader-1.2.5-h1.4.jar`
- `midnightlib-1.5.7-fabric.jar` (development runtime only; included inside Wathe in normal installations)

Gradle downloads the remaining dependencies. On Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Use `./gradlew` on Linux/macOS. Accept the Minecraft EULA before running a dedicated server. The mod is built as `build/libs/BeforetheTerminal-0.7.0-h1.4.jar`; the `-sources.jar` contains corresponding source code.

## Reporting issues

Use [GitHub Issues](https://github.com/chu-teng/BeforetheTerminal/issues). Include BTT, Wathe and HML versions, mode, roles, reproduction steps and relevant logs. Remove personal information before posting logs.

## Credits and licensing

BTT code and documentation are licensed under the [GNU LGPL version 3 or later](LICENSE.txt). Copyright © 2025–2026 hae-si and Before the Terminal contributors. Third-party content retains its own licensing.

Gameplay draws inspiration from Town of Salem, Blood on the Clocktower and Goose Goose Duck. BTT is an independent Wathe add-on, rather than a NoellesRoles fork.

| Project | Author | License and use |
| --- | --- | --- |
| Wathe | doctor4t | All Rights Reserved; base game, downloaded separately |
| HarpyModLoader | agmass | LGPL-3.0; role and trait APIs |
| Ratatouille | doctor4t | All Rights Reserved; Wathe dependency |
| YetAnotherConfigLib | isXander | LGPL-3.0-or-later; configuration |
| Fabric API | FabricMC | Apache-2.0; platform APIs |
| Cardinal Components API | Ladysnake | MIT; bundled by Wathe |
| Simple Voice Chat | maxhenkel | All Rights Reserved; optional proximity voice |
| Gradle Wrapper | Gradle contributors | Apache-2.0; build tooling, original license notices retained |

### Adapted code

Last reviewed: 2026-10-18. Ordinary comments were removed from the public source snapshot on that date; required license notices were retained.

The following files contain code adapted from [NoellesRoles](https://github.com/agmass/NoellesRoles), by agmass, licensed under LGPL-3.0. These adaptations remain licensed under LGPL-3.0. Modifications include package and mod ID changes, removal of unused branches, renamed identifiers and adjustments to BTT mechanics. Paths are relative to `dev/hae/btt/`; `client/` refers to the client source tree.

- `component/AbilityState.java` (owner synchronization updated 2026-10-09), `component/DisguiseState.java` (timed disguise updated 2026-10-11), `component/ScavengerState.java` (display threshold updated 2026-10-10).
- `component/CurseState.java`, from `voodoo/VoodooPlayerComponent`; `component/ExecutionerState.java`, from `executioner/ExecutionerPlayerComponent`; `component/CorpseState.java`, from `coroner/BodyDeathReasonComponent`. Updated 2026-10-12 for null targets, reset state, and initial/technical role fields.
- `client/mixin/game/StowawayHidePsychosisItems.java`.
- Parts of `client/mixin/game/ExecutionerHudMixin.java` (package references and historical comments updated 2026-10-14).
- `client/spirit/SpiritCamera.java` and `client/renderer/DoorHighlightRenderer.java`.
- `mixin/game/FakeRevolverMixin.java`, from `mixin/jester/FakeRevolverMixin` (adapted item check and documentation, 2026-09-29).

### Assets and distribution

Short sound effects use CC0-1.0. Project textures, including the cage and remote, were supplied by the project author. Four recolored Wathe mood UI textures are used with permission. The author has confirmed permission to use the short literary and song quotations. Those permissions do not extend to third-party mods, maps or full music recordings.

This repository and the mod jar do not include third-party mod jars, map saves or epilogue music. Models and vanilla sounds are referenced by resource identifier. Optional epilogue music requires a separate resource pack; it is not needed to play. Full license texts are packaged as `LICENSE_btt`, together with both README files.
