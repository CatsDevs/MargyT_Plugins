# Color Studio

Customizable colour palette plugin for MargyT (API 4).

## Settings

Open **MargyT → Plugins → Color Studio** and tap the plugin to open its settings. Configure:
- Enable/disable the theme
- Background HEX
- Cards/dividers HEX
- Text HEX
- Reset to defaults

Colours are stored in the active account's private preferences; guest settings are separate when signed out.

## Important limitation

MargyT's public `onColour(int)` hook does not identify a colour's role. Color Studio therefore transforms only near-neutral colours, mapping brightness onto the selected background → surface → text palette. Saturated colours are left unchanged to avoid altering video content and most brand/accent colours. Some interface elements may not change, and some neutral text or icons may be affected along with backgrounds.

## Build and install

The GitHub Actions workflow builds the `.mtp` artifact. Open **Actions → Build MargyT plugins**, then download the `MargyT-Plugins-mtp` artifact from a successful run. Install the `.mtp` file via **TikTok → Settings and privacy → MargyT → Plugins → Install a plugin**.
