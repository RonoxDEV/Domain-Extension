# 🌌 Domain Expansion: Unlimited Void

[![Tier](https://img.shields.io/badge/Portfolio-Tier_4_--_CustomMod-purple?style=for-the-badge)](https://ronoxdev.github.io)
[![API](https://img.shields.io/badge/Minecraft-Forge%20%2F%20Fabric-orange?style=for-the-badge)](https://minecraft.net)
[![Graphics](https://img.shields.io/badge/Render-Native_Core_Shaders-red?style=for-the-badge)]()

A high-performance, dependency-free implementation of Gojo Satoru's **Unlimited Void** (Jujutsu Kaisen). This project serves as a technical demonstration of **Native Core Shaders** injection and procedural geometry within the Minecraft rendering pipeline.

---

## 🛠 Technical Overview

Unlike most visual mods that rely on heavy external APIs (like Veil or Iris), this project is built to be **lightweight and vanilla-compatible** by interacting directly with the game's low-level rendering system.

### 1. Native GLSL Core Shaders
The "Void" effect is achieved through custom **Fragment Shaders** injected into the native pipeline:
* **Zero Dependency**: Optimized for performance by avoiding external library overhead.
* **Complex Visuals**: Implements multiple noise layers (scrolling noise), distortion vectors, and a **Fresnel Edge Glow** to create a sense of infinite depth.
* **Dynamic Transparency**: Real-time alpha-blending management for the expansion/retraction phases.

### 2. Procedural UV Sphere Geometry
The domain is not a static 3D model. It uses a **procedural mesh generator**:
* **Math-based Mesh**: Generates vertices for a UV Sphere using stacks and slices directly in Java.
* **Dynamic Scaling**: The radius is updated per-tick, allowing the domain to expand smoothly without model clipping or texture stretching.

### 3. Client-Side Immersive Logic
To simulate the "infinite information" brain-overload effect:
* **Camera Shake**: Randomized pitch and yaw offsets applied directly to the player's view matrix.
* **Input Neutralization**: High-priority event listeners that block mouse clicks and specific keybinds for the target player.
* **State Machine**: A robust server-client synchronization system managing the lifecycle of the domain (`OPENING` -> `ACTIVE` -> `CLOSING`).

---

## 🚀 Usage & Implementation

1. **Activation**: Use the `domain_expansion:domain_activator` item.
2. **Targeting**: Right-click near a mob/player (range: 5-20 blocks).
3. **Mechanics**:
    * The system calculates the **geometric midpoint** between the caster and the target.
    * Spawns the `DomainEntity` which serves as the anchor for the renderer.
    * Applies **Slowness VI** and **Weakness VI** to all entities caught inside.

---

## 🔬 Project Architecture

* `net.domainexpansion.client.shader`: Handles the native GLSL injection.
* `net.domainexpansion.client.renderer`: Procedural mesh generation and buffer management.
* `net.domainexpansion.entity`: Core logic for the expansion state machine.

---

## 👨‍💻 Developed by
**RonoxDEV**
*CS Student & Cybersecurity Enthusiast*
*Focus: Low-level optimization, Game Engines, and Network Security.*
