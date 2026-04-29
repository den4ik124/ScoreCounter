# Team Setup

The first screen players see when launching the app.

## Layout

- A circular **volleyball glyph** icon at the top (gradient Ocean → Sunset palette)
- App title **"Scoreboard"** and tagline **"Tap to play · Swipe to score"**
- Two labelled input fields — TEAM A (blue dot) and TEAM B (orange dot)
- A gradient **Continue →** button pinned to the bottom

## What it does

Lets players enter custom names for Team A and Team B before starting a match. Names are optional — leaving a field blank defaults to "Team A" / "Team B".

## How to use

- Tap the **Team A** field and type a name (e.g. "Sharks")
- Press **Next** on the keyboard to jump to the Team B field
- Type a name for Team B (e.g. "Eagles")
- Press **Done** or tap **Continue →** to proceed

## Details

- Names are trimmed and capped: blank input falls back to the default label
- Names appear throughout the game: on the scoreboard panels, in the winner overlay, and in TTS announcements
- The gradient "Continue →" button and the keyboard Done action both trigger the same transition to Game Mode selection

## See also

- [Game Modes](game-modes.md) — next screen; team names are carried forward
- [Text-to-Speech](text-to-speech.md) — team names appear in every score announcement
- [Winner Overlay](winner-overlay.md) — team name is displayed on the win card
