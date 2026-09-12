# AntiCheat QA

**Fabric 1.21.11 client-side Anti-Cheat QA / Developer Client**

Observes, visualizes, logs and simulates. **No cheats. No bypasses.**

Designed for anti-cheat developers, QA testers and server staff who need clear client-side instrumentation while testing detection systems.

## Features

### Visuals
| Module | Description |
|--------|-------------|
| **Block ESP** | 3D outlines for ores (diamond, emerald, gold, iron, coal, ancient debris) and monster spawners |
| **Chest ESP** | Highlights chests / containers |
| **Entity ESP** | Glowing boxes around entities |
| **Tracers** | Screen-space markers toward players/mobs |
| **Nametags** | Forced nameplates + health & distance |
| **Fullbright** | Adjustable gamma |
| **FreeCam** | Detach camera from player body (body stays frozen) |

### Monitors
| Module | Description |
|--------|-------------|
| **CPS Monitor** | Real left/right click rate |
| **FPS Monitor** | Current client FPS |
| **Movement Monitor** | XYZ, horizontal/vertical speed, on-ground, sprinting |
| **Reach Monitor** | Distance to crosshair-targeted entity |
| **Rotation Monitor** | Yaw / pitch / delta |
| **Velocity Monitor** | Current velocity vector |
| **Ping Monitor** | Latency (tab-list / server entry) |
| **Tick Monitor** | Rough client TPS estimate |

### QA Tools
| Module | Description |
|--------|-------------|
| **Dashboard** | Live counters: detections / warnings / tests |
| **Test Simulator** | Safely injects simulated detection events for GUI & logging tests |

## Controls
- **Right Shift** — Open / close ClickGUI
- Modules can be bound to any key (set inside ClickGUI → module settings)

## Requirements
- Minecraft **1.21.11**
- Fabric Loader ≥ 0.16
- Fabric API
- Java 21

## Building
```bash
# If you have the Gradle wrapper:
./gradlew build

# Or with a system Gradle 9.x:
gradle build
```
The built jar appears in `build/libs/anticheat-qa-*.jar`.

A GitHub Actions workflow (`.github/workflows/build.yml`) builds the mod on every push to `main`.

## License
MIT — see [LICENSE](LICENSE)
