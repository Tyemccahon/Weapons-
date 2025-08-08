# ⚔️ Class Weapons Plugin

**THIS IS A BETA RELEASE — everything might not function correctly at the moment.  
All features are subject to change and update.**

A Minecraft Paper plugin adding **5 RPG class-themed weapons** — each with **unique abilities**, custom crafting recipes, and optional resource pack support for custom models.

---

## 📥 Installation

1. **Download the latest `.jar` from the [Releases](../../releases) page**.
2. Place the `.jar` into your server’s `plugins/` folder.
3. (Optional) Add the included resource pack to your server or distribute it to players.
4. Restart your server.

---

## 📜 Classes & Weapons

### 🪄 Mage – Arcane Staff
- **3 attack modes** (Sneak + Right-Click to cycle):
  1. **Firebolt** – Shoots a flaming projectile.
  2. **Frost Orb** – Snowball that explodes into a lingering **Slowness II** cloud (5s).
  3. **Chain Lightning** – Strikes up to 4 chained targets with damage and **Weakness**.
- **Cooldown:** 5 seconds shared across all modes.
- **Custom Model Data:** `2001`

**Recipe:**
```
E E E
E S E
  S
```
`E = Ender Pearl`, `S = Stick`

**Command:** `/givearcane`

---

### 🛡️ Tank – Aegis Mace
- **Passive buffs while held:**
  - **Resistance II**
  - **Slowness I**
- **Custom Model Data:** `2002`

**Recipe:**
```
I I I
I M I
  S
```
`I = Iron Block`, `M = Mace`, `S = Stick`

**Command:** `/giveguardian`

---

### 🪓 Berserker – Bloodaxe
- **Passive buffs while held:**
  - **Strength II**
  - **Mining Fatigue I**
- **Custom Model Data:** `2003`

**Recipe:**
```
B I B
B S B
  S
```
`B = Block of Redstone`, `I = Iron Ingot`, `S = Stick`

**Command:** `/giveberserker`

---

### 🏹 Archer – Wind Bow
- **Two firing modes** (Sneak + Right-Click to toggle):
  - **Straight Shot** – Vanilla bow mechanics.
  - **Homing Shot** – Arrows curve toward the nearest target.
- **Buff:** Grants **Speed II** for 5s after shooting.
- **Custom Model Data:** `2004`

**Recipe:**
```
S F S
F B F
S F S
```
`S = String`, `F = Feather`, `B = Bow`

**Command:** `/givearcher`

---

### ✨ Healer – Holy Staff
- **Ability:** Right-click to heal nearby allies and give **Regeneration**.
- **Cooldown:** 10 seconds.
- **Custom Model Data:** `2005`

**Recipe:**
```
G H G
  S
  S
```
`G = Gold Ingot`, `H = Golden Apple`, `S = Stick`

**Command:** `/givehealer`

---

## 📦 Resource Pack Support
The plugin assigns **CustomModelData** values for all weapons, so you can use a custom resource pack to replace their models:
- Mage – 2001
- Tank – 2002
- Berserker – 2003
- Archer – 2004
- Healer – 2005

A starter resource pack with model overrides is included in the repo.

---

## 🖥️ Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `classweapons.givearcane` | Allows `/givearcane` | op |
| `classweapons.giveguardian` | Allows `/giveguardian` | op |
| `classweapons.giveberserker` | Allows `/giveberserker` | op |
| `classweapons.givearcher` | Allows `/givearcher` | op |
| `classweapons.givehealer` | Allows `/givehealer` | op |

---

## 🗺️ Commands
- `/givearcane` – Gives the Mage staff.
- `/giveguardian` – Gives the Tank weapon.
- `/giveberserker` – Gives the Berserker weapon.
- `/givearcher` – Gives the Archer weapon.
- `/givehealer` – Gives the Healer staff.

---

## 🎯 Roadmap
- More classes & skills
- Configurable recipes & abilities
- Particle animation customization
