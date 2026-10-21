# Music Island

A compact Dynamic-Island-style pill UI for MargyT.

## Current status

This is an **UI prototype**, not a working system media controller yet. It adds a rounded, dark floating pill to resumed TikTok activities and provides an expandable status message.

The plugin intentionally does not invent track titles or artists. Android's `MediaSessionManager.getActiveSessions()` requires a valid enabled `NotificationListenerService` component. The plugin API packages code as a DEX and does not let a plugin declare a new service in the host manifest, so a reliable cross-app media bridge must be added to MargyT itself (or exposed by a future host API).

## Planned integration

- Host-provided media-session bridge
- Current track title and artist
- Album artwork
- Playback state and progress
- Optional play/pause controls, if the host API supports session transport controls

## Build

The repository's **Build MargyT plugins** workflow creates the `.mtp` artifact.
