# Customi Client

A client-side **Fabric** utility mod for Minecraft Java Edition **1.21.11**, built to be used from a phone launcher
(Zalith / Pojav) as well as on PC. Everything is controlled from one clean, tap-friendly menu, with soft falling
petals and little flowers floating over it.

> Version for the newest Minecraft (26.x) is a separate project, because Minecraft switched to unobfuscated
> names and a new Fabric setup after 1.21.11.

## Menu

Open the menu in a world with the **Open Customi Client menu** key (default: Right Shift; rebind it in
Options > Controls > Key Binds under "Customi Client", or map an on-screen button to it in your launcher).

- Left column: categories (Combat, Movement, Render, Misc)
- Middle: the modules in that category. Tap a name to open its settings, tap the switch to turn it on or off.
- Right: the module's settings. Switches toggle, **-** / **+** change numbers, tapping a mode cycles it.

## Keybinds

Every module has its own toggle key, unbound by default. Set them in
**Options > Controls > Key Binds > Customi Client** (look for "Toggle <module name>").

## Modules

| Module | Category | What it does | Settings |
| --- | --- | --- | --- |
| Aim Assist | Combat | Turns your camera toward the closest target near your crosshair. **Lock on** keeps looking at one target until it dies or leaves range. | Players, Hostile mobs, Passive mobs, Lock on, Aim at (Body / Head), Range, FOV, Smoothness |
| Auto Attack | Combat | Attacks like pressing the attack button. | Must look at target, Crits (Normal / Only crits), Players only, Range, Min charge |
| Speed | Movement | Move faster. | Multiplier, Only on ground |
| Storage ESP | Render | Boxes chests, barrels, shulker boxes, ender chests (and optionally furnaces/hoppers) through walls. | Chests, Barrels, Shulker boxes, Ender chests, Furnaces / hoppers, Range, Labels, Glow |
| Ore ESP | Render | X-ray style: boxes ores through walls with bright colours. | One switch per ore, Range, Labels, Glow |
| Entity ESP | Render | Boxes players and mobs through walls. | Players, Hostile mobs, Passive mobs, Names, Glow, Range |
| Schematic Builder | Misc | Builds a `.litematic` for you, turning the camera with eased, mouse-like movement. | Schematic, Turn speed, Delay (ticks), Reach |

The ESP modules draw 2D boxes on your screen, so they show through walls. This is an overlay, not true see-through
terrain. Coming next: Auto Librarian, Auto Trap, and more.

### Schematic Builder notes

1. Put `.litematic` files in a `schematics` folder inside your game folder (next to `mods`).
2. Put the needed blocks in your **hotbar**.
3. Pick the file in the module's settings and switch it on. It starts 2 blocks in front of you and builds toward
   +X / +Z, bottom to top.
4. It does not walk for you. If the next block is out of reach, move closer.
5. Version 1 places the right block *type* but ignores facing, so stairs, logs and similar may be rotated wrong.

You do **not** need the Litematica mod, only the `.litematic` file.

### Limits of the ESP

ESP only shows what your game already knows. The server only sends chunks inside your render distance, so nothing
can be shown in chunks that were never loaded on your client.

## Installing

1. Install Fabric Loader for 1.21.11 and **Fabric API**.
2. Put the Customi Client jar in your `mods` folder.

## Building (no PC needed)

The project includes a GitHub Actions workflow. Upload the project to a GitHub repository, open the **Actions** tab,
and download the `clientmod-jar` artifact when the build finishes. Make sure `gradle.properties` has the real version
numbers for 1.21.11 from <https://fabricmc.net/develop>.

## Please read

- This is a client-side mod. Many multiplayer servers ban combat and movement modules, and server staff can see a lot
  that a client does. Customi Client does not hide itself and makes no promise of being undetectable. Use it in
  singleplayer, on your own server, or where the rules allow it.
- Not affiliated with Mojang or Microsoft.
