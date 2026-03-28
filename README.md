# 🎮 Blokus – Strategic Board Game

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
  <img src="https://img.shields.io/badge/BoardGameWork-Framework-4CAF50?style=for-the-badge" alt="BGW"/>
  <img src="https://img.shields.io/badge/Multiplayer-Network-FF6B6B?style=for-the-badge" alt="Multiplayer"/>
</p>

<p align="center">
  <strong>Eine Kotlin-Implementierung des klassischen Strategiespiels Blokus mit KI-Bots, Netzwerk-Multiplayer und modernem UI.</strong>
</p>

<p align="center">
  <a href="https://github.com/Mohammad-El-Zein/Blokus-Game/releases/latest">⬇️ Download</a> •
  <a href="https://github.com/Mohammad-El-Zein/Blokus-Game/wiki">📚 Wiki</a>
</p>

---

## 🚀 Schnellstart

<table>
<tr>
<td>

### ⬇️ [Download Blokus Game v1.0](https://github.com/Mohammad-El-Zein/Blokus-Game/releases/latest)

</td>
</tr>
</table>

1. **Download:** Klicke auf den Link oben und lade `Blokus-Game-v1.0.zip` herunter
2. **Entpacken:** Entpacke die ZIP-Datei
3. **Starten:** Gehe zu `bin/` und doppelklicke auf:
   - **Windows:** `Projekt2.bat`
   - **Mac/Linux:** `Projekt2`
4. **Spielen!** 🎮

> 💡 *Keine Installation nötig – einfach entpacken und spielen!*

---

## 📋 Über das Projekt

**Blokus** ist ein abstraktes Strategiespiel, bei dem Spieler versuchen, so viele Teile wie möglich auf das Brett zu legen und die meiste Fläche zu bedecken. Dieses Projekt ist eine vollständige digitale Umsetzung mit KI-Gegnern und Netzwerk-Multiplayer.

> 🎓 *Entwickelt im Rahmen des Softwarepraktikums (SoPra) an der TU Dortmund*

---

## ✨ Features

### 🎯 Spielmodi
| Modus | Beschreibung |
|-------|-------------|
| 👥 **Hotseat** | 2-4 Spieler am gleichen Bildschirm |
| 🌐 **Netzwerk** | Online-Multiplayer über BGW-Net |
| 🤖 **vs. Bots** | Spiele gegen KI-Gegner |
| 🎭 **Bot vs. Bot** | Beobachte KI-Duelle |

### 🎮 Spielvarianten
| Variante | Spieler | Spielfeld |
|----------|---------|-----------|
| Classic | 4 Spieler | 20×20 |
| Trio | 3 Spieler | 20×20 |
| Duo | 2 Spieler (je 2 Farben) | 20×20 |
| Mini | 2 Spieler | 14×14 |

### 🛠️ Funktionen
| Feature | Beschreibung |
|---------|-------------|
| ↩️ **Undo/Redo** | Züge zurücknehmen und wiederholen |
| 💾 **Speichern/Laden** | Spiel unterbrechen und später fortsetzen |
| 🎨 **Drag & Drop** | Intuitive Steuerung der Spielsteine |
| 🔄 **Rotation/Spiegeln** | Steine drehen und spiegeln |
| 📊 **Punkteberechnung** | Basic & Advanced Scoring |
| ⚡ **Bot-Geschwindigkeit** | Einstellbare Simulationsgeschwindigkeit |
| 🌍 **Mehrsprachig** | Deutsch & Englisch |

---

## 🤖 KI-System

Das Spiel enthält zwei verschiedene Bot-Implementierungen:

| Bot | Strategie |
|-----|-----------|
| **Random Bot** | Wählt zufällig einen gültigen Zug |
| **Smart Bot** | Optimierte Strategie für das Bot-Turnier |

Der Smart Bot analysiert:
- Maximale Flächenabdeckung
- Blockierung gegnerischer Züge
- Strategische Eckpositionierung
- Priorisierung großer Steine

---

## 📖 Spielregeln

### Grundregeln
1. Jeder Spieler beginnt in einer Ecke des Spielfelds
2. Neue Steine müssen **diagonal** an eigene Steine angrenzen
3. Steine dürfen **nicht** an den Kanten eigener Steine liegen
4. Wer nicht mehr legen kann, passt automatisch

### Punkteberechnung

**Basic Scoring:**
- Jedes nicht gelegte Quadrat = **-1 Punkt**

**Advanced Scoring (aktiviert):**
- Alle Steine gelegt = **+15 Bonuspunkte**
- Letzter Stein war der Einer = **+5 Extra-Bonuspunkte**

---

## 🛠️ Tech Stack

```
Kotlin 1.9  •  Gradle  •  BoardGameWork (BGW)  •  BGW-Net  •  JUnit 5
```

### Architektur

```
src/
├── main/kotlin/
│   ├── entity/          # Spielobjekte (Board, Player, Tile)
│   ├── service/         # Spiellogik & Netzwerk
│   └── view/            # UI-Komponenten (BGW)
└── test/kotlin/         # Unit Tests
```

---

## 📦 Installation (für Entwickler)

Falls du den Code selbst kompilieren möchtest:

### Voraussetzungen
- JDK 17+
- Gradle 8.x

### Bauen & Starten

```bash
# Repository klonen
git clone https://github.com/Mohammad-El-Zein/Blokus-Game.git
cd Blokus-Game

# Spiel starten
./gradlew run

# Oder Distribution erstellen
./gradlew installDist
```

### Tests ausführen

```bash
./gradlew test
```

---

## 📸 Screenshots

<details>
<summary>🖼️ Screenshots anzeigen</summary>

### Hauptmenü
![Main Menu](docs/screenshots/main-menu.png)

### Spielfeld
![Game Board](docs/screenshots/game-board.png)

### Spielende
![Game Over](docs/screenshots/game-over.png)

</details>

---

## 📊 UML Diagramme

Die vollständige Dokumentation findest du im [Wiki](https://github.com/Mohammad-El-Zein/Blokus-Game/wiki):

- [📐 Klassendiagramm](https://github.com/Mohammad-El-Zein/Blokus-Game/wiki/Design)
- [🔄 Sequenzdiagramme](https://github.com/Mohammad-El-Zein/Blokus-Game/wiki/Sequences)

---

## 🎯 Projektstruktur

```
Blokus-Game/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       ├── entity/        # Datenmodelle
│   │       ├── service/       # Geschäftslogik
│   │       └── view/          # Benutzeroberfläche
│   └── test/
│       └── kotlin/            # Unit Tests
├── docs/                      # UML Diagramme
├── HowToPlay.pdf              # Spielanleitung
├── build.gradle.kts           # Build-Konfiguration
└── README.md
```

---

## 📄 Spielanleitung

Eine detaillierte Anleitung zur Bedienung des Spiels findest du in der [HowToPlay.pdf](HowToPlay.pdf).

---

## 👨‍💻 Autor

<table>
  <tr>
    <td align="center">
      <strong>Mohammad El Zein</strong><br>
      Informatik Student @ TU Dortmund<br><br>
      <a href="https://github.com/Mohammad-El-Zein">
        <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"/>
      </a>
    </td>
  </tr>
</table>

> 🎓 *Dieses Projekt wurde im Rahmen des Softwarepraktikums (SoPra) an der TU Dortmund als Teamprojekt entwickelt.*

---

## 🙏 Danksagung

- **TU Dortmund** – Softwarepraktikum Team
- **BoardGameWork** – Framework für Brettspiel-Entwicklung
- **Blokus** – Originalspiel von Bernard Tavitian

---

## 📄 Lizenz

Dieses Projekt wurde für Bildungszwecke erstellt.

---

<p align="center">
  <strong>⭐ Wenn dir das Projekt gefällt, gib ihm einen Stern! ⭐</strong>
</p>
