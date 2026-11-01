---
name: Nexus Expressive Material
colors:
  surface: '#0f141c'
  surface-dim: '#0f141c'
  surface-bright: '#343942'
  surface-container-lowest: '#090e16'
  surface-container-low: '#171c24'
  surface-container: '#1b2028'
  surface-container-high: '#252a33'
  surface-container-highest: '#30353e'
  on-surface: '#dee2ee'
  on-surface-variant: '#bac9cc'
  inverse-surface: '#dee2ee'
  inverse-on-surface: '#2c3139'
  outline: '#849396'
  outline-variant: '#3b494c'
  surface-tint: '#00daf3'
  primary: '#c3f5ff'
  on-primary: '#00363d'
  primary-container: '#00e5ff'
  on-primary-container: '#00626e'
  inverse-primary: '#006875'
  secondary: '#44ddc1'
  on-secondary: '#00382f'
  secondary-container: '#00bea4'
  on-secondary-container: '#00463b'
  tertiary: '#b1ffbf'
  on-tertiary: '#003918'
  tertiary-container: '#22ef7e'
  on-tertiary-container: '#006731'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#9cf0ff'
  primary-fixed-dim: '#00daf3'
  on-primary-fixed: '#001f24'
  on-primary-fixed-variant: '#004f58'
  secondary-fixed: '#68fadd'
  secondary-fixed-dim: '#44ddc1'
  on-secondary-fixed: '#00201a'
  on-secondary-fixed-variant: '#005145'
  tertiary-fixed: '#62ff96'
  tertiary-fixed-dim: '#00e475'
  on-tertiary-fixed: '#00210b'
  on-tertiary-fixed-variant: '#005226'
  background: '#0f141c'
  on-background: '#dee2ee'
  surface-variant: '#30353e'
typography:
  display-lg:
    fontFamily: Roboto Flex
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  display-md:
    fontFamily: Roboto Flex
    fontSize: 44px
    fontWeight: '600'
    lineHeight: 52px
    letterSpacing: 0px
  headline-lg:
    fontFamily: Roboto Flex
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: 0px
  headline-lg-mobile:
    fontFamily: Roboto Flex
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: 0px
  headline-md:
    fontFamily: Roboto Flex
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: 0px
  headline-sm:
    fontFamily: Roboto Flex
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 26px
    letterSpacing: 0.15px
  title-lg:
    fontFamily: Roboto Flex
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-md:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 22px
    letterSpacing: 0.15px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.25px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.5px
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.25rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system translates the principles of Material Design 3 (Material You) into an expressive, utility-focused file management environment. It blends modern system-level integration with the precision demands of advanced storage manipulation, file analytics, and device housekeeping.

### Visual Aesthetic & Philosophy
The aesthetic combines **Modern Material / Corporate Expressive** with tactile, squircle-driven geometry. It balances technical credibility with vibrant dynamism, relying on:
- High-contrast visual hierarchy tuned for rapid visual scanning across dense lists and visual media grids.
- Soft squircle surfaces and deep organic containers that nest seamlessly within Android system paradigms.
- Vibrant, energetic accents to communicate storage health, file taxonomy, and operational states without overwhelming functional clarity.
- Fluid transitions and surface-level depth rather than skeuomorphic clutter.

### Target Persona & Emotional Response
The target audience consists of power users, system administrators, creative professionals transferring high-volume assets, and modern Android enthusiasts who demand granular device control. The interface instills a sense of surgical precision, fluid speed, zero data-loss anxiety, and effortless organizational control.

## Colors

The system uses an expressive 5-tier tonal architecture anchored by three high-energy accents: **Electric Cyan** (`#00E5FF`) for primary operations and focus states, **Material Teal** (`#00BFA5`) for secondary classification and directory routing, and **Emerald** (`#00E676`) for actionable states, health metrics, and completed transfers.

### Theme Modes & Tone Roles
- **Default Dark / Midnight Slate:** Background anchored at deep blue-gray neutral (`#0F141C`). Surfaces step upward in tone through container tiers (`#171E29`, `#1E2634`, `#263042`, `#303C52`).
- **AMOLED Pitch Black:** Background anchored at absolute black (`#000000`) with tonal container steps (`#0B0E14`, `#121720`, `#1A212D`) to conserve battery on OLED hardware while maintaining clear containment bounds.
- **Cool Slate / Midnight Indigo:** Employs deeper cobalt-tinged neutrals (`#0A0E17`) with cyan-tinted elevation steps.
- **Light Theme:** Grounded in a clinical, crisp white-slate neutral (`#F6F8FC`), using surface containers (`#EEF2F8`, `#E2E8F0`, `#CBD5E1`) and high-contrast, darkened teal/cyan anchors (`#006B7D`, `#00695C`).

### Semantic Storage Indicators
- **System / Critical Storage (>90% full):** Flame Coral (`#FF5252`).
- **Media & Binary Archives:** Radiant Amber (`#FFD600`).
- **Documents & Code Bases:** Electric Cyan (`#00E5FF`).
- **Active Operations / Success:** Emerald (`#00E676`).
- **On-Color Typography:** All primary containers feature `on-primary` dark surfaces (`#00363D`) ensuring strict WCAG 2.1 AAA contrast.

## Typography

The typographic system leverages a two-font pairing designed for technical readability and expressive hierarchy:

- **Roboto Flex (Headlines & Display):** Handles structural headers, analytics displays, category groupings, and navigation headers. Its flexible weight distribution delivers commanding clarity at scale while preserving Android ecosystem familiarity.
- **Inter (Body, Metadata, & Labels):** Applied across directory paths, tabular file sizes, file extensions, and timestamps. With tall x-height and distinct glyph disambiguation (e.g., distinguishing `1`, `l`, and `I`), Inter ensures zero misreading during destructive file commands.

### Scaling & Rules
- For screens under `600px` (mobile viewport), `headline-lg-mobile` replaces `headline-lg` to prevent line wrapping inside file operation toolbars and bottom sheets.
- Numerical metadata (file sizes, disk allocation metrics, permissions strings like `rwxr-xr-x`) must always apply tabular numerals (`tnum`) to maintain structural vertical alignment in list views.

## Layout & Spacing

The layout is built upon an adaptive 8pt fluid-grid architecture aligned to standard Android window size classes (Compact, Medium, Expanded).

### Breakpoints & Adaptive Layout Grid
- **Compact (<600dp - Mobile):** 4-column layout, `margin` = `1rem` (16dp), `gutter` = `1rem` (16dp). UI structures adopt a single-pane vertical view with sticky navigation and floating bottom app actions. Top bars pad explicitly for the Android status bar (`top: env(safe-area-inset-top)`).
- **Medium (600dp to 839dp - Foldables & Small Tablets):** 8-column layout, `margin-tablet` = `1.5rem` (24dp), `gutter-tablet` = `1.25rem` (20dp). Dual-pane presentation: tree navigation persists on the left pane while content operates within a fluid canvas on the right pane.
- **Expanded (>=840dp - Desktop & Large Tablets):** 12-column layout, `margin-desktop` = `2rem` (32dp), `gutter-desktop` = `1.5rem` (24dp). Tri-pane architecture: Navigation Rail + File Grid/List + Detail/Inspection Sidebar.

### Spacing Application
- Component internal padding uses strict tokens: `space-xs` (4dp) for micro badge gutters, `space-sm` (8dp) for list item compact vertical paddings and chip gaps, `space-md` (16dp) for card bodies, and `space-lg` (24dp) for dialog and sheet internals.

## Elevation & Depth

In alignment with Material 3, the design system eliminates hard drop-shadows and artificial light reflections in favor of **Tonal Surface Layers** paired with subtle ambient diffusion.

### Tonal Container Hierarchy
Elevation is communicated via color luminance steps:
- **Surface Level 0 (Canvas):** Pure background canvas (`#0F141C` dark / `#000000` AMOLED).
- **Surface Level 1 (Default Containers):** Base storage cards, non-selected list items, pinned quick-access rows (`#171E29` dark / `#0B0E14` AMOLED).
- **Surface Level 2 (Interactive Floating Containers):** Hovered cards, search bars, filter toolbars (`#1E2634` dark / `#121720` AMOLED).
- **Surface Level 3 (Drawers & Dialogs):** Modal bottom sheets, action sheets, context menus (`#263042` dark / `#1A212D` AMOLED).
- **Surface Level 4 (Transient Overlays):** Floating Action Buttons (FAB), toast messages, snackbars (`#303C52` dark / `#222B3A` AMOLED).

### Ambient Edge Treatment
To guarantee definition between stacked layers in low-contrast AMOLED scenarios, containers utilize a 1px inner border (`surface-variant-outline`) set at 8% opacity white for dark mode and 6% black for light mode.

### Scrim & Status Bar Treatments
- Android status bars render completely transparent, with icon glyphs reacting dynamically to the underlying surface tone.
- Modal backdrops apply a 45% black wash with an 8px hardware-accelerated background blur (`backdrop-filter: blur(8px)`).

## Shapes

The shape system adopts smooth, organic squircle corners designed to soften dense technical interfaces and reflect the rounded hardware curves of modern mobile devices.

### Corner Radii Mappings
- **Full Pill (`9999px`):** Used exclusively for filter chips, tags, input search capsules, and primary floating action buttons.
- **Large Squircle (`rounded-3xl` / 28px - 32px):** Applied to Modal Bottom Sheets, primary storage overview cards, and contextual alert dialogs.
- **Medium Squircle (`rounded-2xl` / 16px - 20px):** Applied to standard file grid tiles, folder containers, multi-selection banners, and popover panels.
- **Small Squircle (`rounded-lg` / 8px - 12px):** Applied to thumbnail media masks, progress bar tracks, and inner list row highlight states.

## Components

### Buttons & Floating Action Buttons
- **Primary Buttons:** High-emphasis pill shape (`rounded-full`), filled with Electric Cyan (`#00E5FF`), text set in dark contrasting ink (`#00363D`, `label-lg`). Pressed state triggers a tonal ripple overlay with 12% opacity.
- **Secondary / Outlined Buttons:** Pill shape, 1px cyan-tinted container outline, filled with Surface Level 1.
- **Extended Floating Action Button (FAB):** Rounded squircle (`rounded-2xl`), elevated with Surface Level 4, prominent Cyan fill for the primary batch action (e.g., "Add / New Transfer").

### Chips & Taxonomy Filters
- **Filter & Assist Chips:** Exact pill silhouette (`height: 32px`). Unselected state uses Surface Level 2 with low-contrast borders. Selected state fills with Material Teal (`#00BFA5`) or Electric Cyan at 20% opacity with a vibrant solid accent border and a leading checkmark icon.

### File List Items & Grid Tiles
- **List Rows:** 56px default row height (dense mode: 48px). Left-aligned 40px squircle container (`rounded-xl`) housing file type glyphs or thumbnail previews. Typography is stacked: primary file title (`title-md`) on top, metadata string (`body-sm` tabular) below.
- **Grid Tiles:** Squircle card (`rounded-2xl`), aspect ratio 1:1, incorporating full-bleed thumbnail rendering with a lower gradient scrim for text legibility.
- **Selection State:** Active items highlight via a soft 12% Electric Cyan surface wash and display an Emerald checkmark pill badge in the top-right corner.

### Inputs & Search Bars
- **Search Capsule:** Docked pill search bar (`height: 48px`), Surface Level 2 fill, leading folder-search icon, trailing voice / filter action buttons. No harsh exterior border; relies on ambient surface elevation contrast.

### Checkboxes, Radios, & Toggles
- **Checkboxes:** Smooth rounded-md squares (6px corner radius). Checked state transitions to a solid Emerald or Cyan fill with a crisp white tick mark.
- **Switches:** Pill track with an offset, floating circular thumb that scales dynamically during touch press (`h-6 w-6` expanding to `h-6 w-7`).

### Cards & Storage Status Indicators
- **Storage Overview Card:** Elevated Surface Level 1 container with `rounded-3xl` corners and `space-md` internal padding.
- **Storage Progress Bar:** Segmented or unified 12px thick pill track (`rounded-full`). Filled segments map to category hues: Electric Cyan for System, Material Teal for User Media, and Emerald for Available Space. Real-time disk readouts utilize `display-md` or `headline-md` tabular numbers.

### Android Contextual Bottom Sheets
- Anchored to the viewport bottom with `rounded-t-3xl` geometry. Features a top-centered drag handle (`32px x 4px`, rounded-full, 20% opacity white) and persistent dismiss gestures.