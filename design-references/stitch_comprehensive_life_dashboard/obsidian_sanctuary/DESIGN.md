# Design System Specification: The Monochromatic Sanctuary

## 1. Overview & Creative North Star
**Creative North Star: "The Obsidian Monastery"**

This design system is built on the philosophy of "Silent Utility." We are moving away from the loud, cluttered patterns of traditional SaaS interfaces to create a digital sanctuary. The goal is to provide a high-end editorial experience where the user’s focus is protected, not demanded.

To achieve this, we reject "template" layouts. We favor **intentional asymmetry**, high-contrast typography scales (the tension between large `display` type and small `label` type), and a depth model based on light absorption rather than artificial drop shadows. The interface should feel like a physical object carved from dark stone—tactile, heavy, and quiet.

---

## 2. Colors & Tonal Depth
Our palette is a disciplined range of obsidian blacks and deep slates. Hierarchy is not achieved through color, but through the management of light and surface.

### The Palette (Material Convention)
*   **Background / Surface:** `#131313` (The base obsidian)
*   **Primary:** `#ffffff` (Pure light for essential actions)
*   **Surface Containers:**
    *   `surface_container_lowest`: `#0e0e0e` (Recessed areas)
    *   `surface_container_low`: `#1c1b1b` (Standard cards)
    *   `surface_container_highest`: `#353534` (Elevated focus)
*   **Functional:** `error` (`#ffb4ab`) and `outline_variant` (`#474747`).

### Core Visual Rules
*   **The "No-Line" Rule:** Prohibit the use of 1px solid borders to define sections. Boundaries must be defined solely through background shifts. For example, a `surface_container_low` card sits directly on a `surface` background. The change in hex value is the boundary.
*   **Surface Hierarchy & Nesting:** Use a "recessed" logic. To make an element feel important, do not always lift it "up." Sometimes, "sinking" the background (using `surface_container_lowest`) around a component makes it stand out more effectively. 
*   **The Glass & Gradient Rule:** For floating modals or navigation bars, use `surface_variant` at 60% opacity with a 20px backdrop-blur. This "Glassmorphism" ensures the UI feels integrated into the environment.
*   **Signature Textures:** Use a subtle linear gradient on primary CTAs—transitioning from `primary` (#ffffff) to `primary_container` (#d4d4d4) at a 45-degree angle. This adds a "satin" finish that feels premium and bespoke.

---

## 3. Typography
We utilize a pairing of **Manrope** for structural expression and **Inter** for functional clarity. 

*   **Display & Headlines (Manrope):** These are our "Editorial Moments." Use `display-lg` (3.5rem) with tight letter-spacing (-0.02em) to create an authoritative, architectural feel. Headlines should be used sparingly to break the grid and create a rhythmic flow.
*   **Body & Titles (Inter):** Inter handles the "Workhorse" duties. Use `body-md` (0.875rem) for most content to maintain a high-density, professional look.
*   **Label Scale:** `label-sm` (0.6875rem) in all-caps with increased letter-spacing (+0.05em) should be used for metadata. This creates a "technical" aesthetic that balances the organic feel of the headlines.

---

## 4. Elevation & Depth
In a monochromatic system, traditional shadows often look "dirty." We use **Tonal Layering** to convey depth.

*   **The Layering Principle:** Stacking is life. 
    *   *Level 0:* `surface` (#131313)
    *   *Level 1:* `surface_container_low` (#1c1b1b)
    *   *Level 2:* `surface_container_high` (#2a2a2a)
*   **Ambient Shadows:** When an element must float (e.g., a context menu), use a shadow with a 40px blur, 0px offset, and 8% opacity of the `on_surface` color. It should feel like a soft glow of dark light, not a hard drop shadow.
*   **The "Ghost Border" Fallback:** If accessibility requirements demand a border, use the `outline_variant` token at **15% opacity**. This creates a "Ghost Border" that is barely perceptible but provides the necessary edge definition for low-vision users.

---

## 5. Components

### Buttons
*   **Primary:** Solid `primary` (#ffffff) with `on_primary` (#1a1c1c) text. Use `DEFAULT` (0.25rem) rounding. No shadow.
*   **Secondary:** `surface_container_high` background. Text in `on_surface`.
*   **Tertiary:** No background. Text in `primary`. Underline on hover using the "Ghost Border" logic.

### Cards & Lists
*   **Card Construction:** Never use dividers. Separate content using the Spacing Scale (typically 2rem of vertical space) or by nesting a `surface_container_lowest` block inside a `surface_container_low` card.
*   **Interactive Lists:** Use a subtle background shift to `surface_bright` (#3a3939) on hover.

### Input Fields
*   **Text Inputs:** Use a "Minimalist Underline" style or a fully flooded `surface_container_lowest` block.
*   **State:** The active state should be indicated by a 1px transition of the `outline` token (#919191), never a bright color.

### Custom Component: The "Zen Loader"
In the context of a "Digital Sanctuary," use a slow-pulsing circular ring using the `primary_fixed` (#5d5f5f) to `primary` (#ffffff) gradient.

---

## 6. Do's and Don'ts

### Do:
*   **Embrace Negative Space:** Allow elements to breathe. If you think it needs more space, double it.
*   **Use Subtle Gradients:** Use gradients to mimic how light hits a matte surface.
*   **Focus on Micro-Typography:** Use `label-sm` for secondary information to keep the UI clean.

### Don't:
*   **Don't use pure black (#000000):** It kills the "obsidian" depth. Always use the `surface` (#131313) base.
*   **Don't use 100% opaque borders:** They break the "Sanctuary" feel and make the UI look like a generic wireframe.
*   **Don't over-rely on Icons:** Use precise typography instead. If an icon is needed, use a thin-stroke (1px or 1.5px) weight to match the Inter font-weight.

---

## 7. Spacing & Grid
This system thrives on a **12-column fluid grid** but encourages "Bleed Moments." Allow large `display` typography or hero images to break the container margins, creating an editorial, magazine-like layout that feels bespoke rather than generated. Use an 8px base spacing power scale (8, 16, 24, 32, 48, 64, 96).