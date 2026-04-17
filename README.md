# ShieldSync

ShieldSync is a Minecraft server mod designed to maximize shield combat fairness by neutralizing latency (ping) and fixing critical vanilla shield desync issues.

## Features

- **Ping-Compensated Blocking**: Dynamically reduces the 250ms (5 tick) shield activation delay based on the player's network latency. High-ping players can block fair and square.
- **Axe-Hit Desync Fix**: Prevents axes from disabling a shield if the hit was not actually blocked (fixing the famous "Phantom Axe" exploit).
- **Angle Leniency**: Expands the server-side shield block angle slightly (from 180° to ~210°) to account for rapid turning desync.
- **Auto-Update System**: `/shieldsync update` fetches the latest release directly from GitHub and prepares it for the next server boot.

## Commands

All commands require OP level 2 or the `shieldsync.admin` permission node.

- `/shieldsync ping [player]`: Returns real ping, simulated jitter/spikes, and current tick compensation.
- `/shieldsync status [player]`: Displays global and per-player enablement status.
- `/shieldsync update`: Checks for newer versions on GitHub and downloads them automatically.

## Configuration

Settings are stored in `config/shieldsync.properties`.
- `enabled`: Global toggle for all features.
- `axe_fix_enabled`: Toggles the axe-disable fix.
- `detection_fix_enabled`: Toggles the rotation angle leniency.
- `max_ping_compensation_ms`: The maximum latency (in ms) to compensate for (Default: 250).

---

## Build & Release (For Developers)

This project is configured with an automated release pipeline.

### Preparation
1. Ensure the `gradlew` wrapper is used.
2. Build the project locally:
   ```bash
   ./gradlew build
   ```

### Push and Release
The mod automatically releases to GitHub only when the **version number increases**.
1. To bump the version:
   ```bash
   ./gradlew bumpVersion
   ```
2. Commit and push:
   ```bash
   git add .
   git commit -m "Bump version to X.Y.Z"
   git push
   ```
The GitHub Action will detect the version bump, compile the mod, and create a new Release with the JAR attached.

## License
Apache License 2.0. See [LICENSE](LICENSE) for details.
