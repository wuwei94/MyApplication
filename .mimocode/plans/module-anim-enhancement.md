# Plan: Comprehensive Enhancement of module_anim

## Overview

Add 4 new Activities and enhance 1 existing Activity to make module_anim a comprehensive Android animation tech stack showcase. The module currently covers Property Animation, Scene Transitions, and Blur effects. Missing are: View (Tween) Animation, Spring Animation, Circular Reveal, and MotionLayout.

## Dependency Changes

**File: `gradle/libs.versions.toml`** — Add DynamicAnimation:
```toml
[versions]
dynamicanimation = "1.0.0-alpha03"

[libraries]
androidx-dynamicanimation = { group = "androidx.dynamicanimation", name = "dynamicanimation", version.ref = "dynamicanimation" }
```

**File: `modules/module_anim/build.gradle.kts`** — Add dependency:
```kotlin
implementation(libs.androidx.dynamicanimation)
```

No other dependency changes needed — ConstraintLayout 2.2.1 (which includes MotionLayout) is already auto-included via the convention plugin.

---

## New Activities (4 total)

### 1. ViewAnimationActivity — Tween Animation (HIGH PRIORITY)

| Field | Value |
|-------|-------|
| **Name** | `ViewAnimationActivity` |
| **Route** | `RouterPath.Anim.ViewAnimation` = `"/Anim/ViewAnimation"` |
| **Base class** | `BaseVBActivity<AnimActivityViewAnimationBinding>` |
| **Layout** | `anim_activity_view_animation.xml` |
| **Package** | `com.example.william.my.module.anim.activity` |

**Purpose**: Demonstrate the legacy `android.view.animation` system (distinct from Property Animation). This is the animation system pre-API 11, still widely used for simple effects.

**UI Layout**:
- Top area: A target `View` (colored box, ~120dp) to animate
- Bottom area: 6 `Button` elements in a grid (2 columns, 3 rows):
  - Alpha Animation
  - Rotate Animation
  - Scale Animation
  - Translate Animation
  - AnimationSet (combines all 4)
  - Interpolator Showcase (cycle through 6+ interpolators)

**Key Implementation Details**:
- `AlphaAnimation(1f, 0f)` — fade from visible to invisible
- `RotateAnimation(0f, 360f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)` — rotate around center
- `ScaleAnimation(1f, 0.5f, 1f, 0.5f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)` — scale down to half
- `TranslateAnimation(0f, 300f, 0f, 0f)` — move right by 300px
- `AnimationSet(true)` — apply all 4 together with `setDuration(1000)`
- Interpolator showcase: `CycleInterpolator`, `BounceInterpolator`, `OvershootInterpolator`, `AnticipateInterpolator`, `LinearInterpolator`, `AccelerateDecelerateInterpolator` — each applied to a RotateAnimation to visually demonstrate the difference
- All animations use `setDuration(1000)` and `setRepeatCount(0)` for clarity
- **Key distinction to comment in code**: View Animation only changes the **drawing** (visual position), NOT the actual View properties. `getTranslationX()` remains 0 after a translate animation. This is the fundamental difference from Property Animation.

---

### 2. SpringAnimationActivity — DynamicAnimation / Spring (HIGH PRIORITY)

| Field | Value |
|-------|-------|
| **Name** | `SpringAnimationActivity` |
| **Route** | `RouterPath.Anim.SpringAnimation` = `"/Anim/SpringAnimation"` |
| **Base class** | `BaseVBActivity<AnimActivitySpringAnimationBinding>` |
| **Layout** | `anim_activity_spring_animation.xml` |
| **Package** | `com.example.william.my.module.anim.activity` |

**Purpose**: Demonstrate AndroidX DynamicAnimation API — physics-based animations using spring dynamics and fling gestures.

**UI Layout**:
- Top section: A draggable `View` (colored circle/box, ~80dp) that snaps back to center via spring
- Middle: 3 Buttons:
  - "Spring X" — spring the view horizontally to a target position
  - "Spring Y" — spring the view vertically to a target position
  - "Fling" — fling the view in a direction based on current velocity
- Bottom: 3 `SeekBar` controls for:
  - Stiffness (100–1000)
  - Damping ratio (0.1–1.0)
  - Start velocity (-2000 to 2000)

**Key Implementation Details**:
- `SpringAnimation(view, DynamicAnimation.TRANSLATION_X, targetValue)` with `SpringForce(targetValue).setStiffness(stiffness).setDampingRatio(dampingRatio)`
- `FlingAnimation(view, DynamicAnimation.TRANSLATION_X)` with `.setStartVelocity(velocity).setMinValue(-500f).setMaxValue(500f)`
- Touch handling: On `ACTION_DOWN`, cancel running animation, record start position. On `ACTION_MOVE`, update view translation. On `ACTION_UP`, start a `FlingAnimation` based on velocity.
- `SpringAnimation` callbacks: `addEndListener` to log final values, `addUpdateListener` for real-time position display
- Show current translation values in a `TextView` updated via `addUpdateListener`

---

### 3. CircularRevealActivity — Circular Reveal (HIGH PRIORITY)

| Field | Value |
|-------|-------|
| **Name** | `CircularRevealActivity` |
| **Route** | `RouterPath.Anim.CircularReveal` = `"/Anim/CircularReveal"` |
| **Base class** | `BaseVBActivity<AnimActivityCircularRevealBinding>` |
| **Layout** | `anim_activity_circular_reveal.xml` |
| **Package** | `com.example.william.my.module.anim.activity` |

**Purpose**: Demonstrate `ViewAnimationUtils.createCircularReveal()` — the Material Design reveal/hide animation (API 21+).

**UI Layout**:
- A large `FrameLayout` container (~300dp x 300dp) with a colored background
- 4 Buttons at the bottom:
  - "Reveal from Center" — reveal the container from center
  - "Reveal from Corner" — reveal from top-left corner
  - "Hide to Center" — hide the container back to center
  - "Hide to Corner" — hide to top-left corner

**Key Implementation Details**:
- `ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius)` returns an `Animator`
- Reveal: `startRadius = 0f`, `endRadius = maxRadius` (where `maxRadius = sqrt(centerX² + centerY²)`)
- Hide: `startRadius = maxRadius`, `endRadius = 0f`
- `maxRadius` calculation: `Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()`
- Set initial visibility: `View.INVISIBLE` before reveal, `View.VISIBLE` before hide
- Duration: 500ms with `AccelerateDecelerateInterpolator`
- Handle API check: `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)` with fallback message

---

### 4. MotionLayoutActivity — MotionLayout (HIGH PRIORITY)

| Field | Value |
|-------|-------|
| **Name** | `MotionLayoutActivity` |
| **Route** | `RouterPath.Anim.MotionLayout` = `"/Anim/MotionLayout"` |
| **Base class** | `BaseVBActivity<AnimActivityMotionLayoutBinding>` |
| **Layout** | `anim_activity_motion_layout.xml` |
| **MotionScene** | `anim_motion_scene.xml` (in `res/xml/`) |
| **Package** | `com.example.william.my.module.anim.activity` |

**Purpose**: Demonstrate MotionLayout — the declarative, XML-driven motion system built on ConstraintLayout.

**UI Layout**:
- A `MotionLayout` wrapping a simple scene:
  - A round `View` (ball) positioned at top-left
  - A rectangular `View` (target) positioned at bottom-right
- 2 Buttons below:
  - "Transition to End" — programmatically transition to end state
  - "Transition to Start" — transition back to start
- A `SeekBar` for manual scrubbing of the transition progress

**Key Implementation Details**:
- **MotionScene XML** (`res/xml/anim_motion_scene.xml`):
  - `<Transition>` with `<start>` and `<End>` constraint sets
  - Start: ball at top-left, small scale
  - End: ball at bottom-right, large scale, color change
  - `<KeyPosition>` for path-based motion (ball follows a curved path, not diagonal)
  - `<KeyAttribute>` for intermediate property changes (e.g., rotation at 50%)
- **Layout XML** uses `<MotionLayout>` as root with `app:layoutDescription="@xml/anim_motion_scene"`
- **Programmatic control**: `motionLayout.setTransition(R.id.start, R.id.end)` + `motionLayout.transitionToEnd()` / `motionLayout.transitionToStart()`
- **Scrubbing**: `SeekBar.OnSeekBarChangeListener` calls `motionLayout.progress = progress`
- **Touch control**: MotionLayout handles touch drag by default — the ball can be dragged between states

---

## Enhancement to Existing Activity

### AnimatorActivity — Add Advanced Property Animation Demos

**Current state**: 6 demo modes (Alpha, Rotation, Scale, Translation, AnimatorSet, Interpolators). Already well-structured.

**Enhancement**: Add 3 new demo modes to the existing click-cycle:

| New Mode | What It Demonstrates |
|----------|---------------------|
| **PropertyValuesHolder** | Using `PropertyValuesHolder` to animate multiple properties simultaneously with a single `ObjectAnimator` — more concise than `AnimatorSet` |
| **Keyframe Animation** | Using `Keyframe.ofFloat()` + `PropertyValuesHolder` to define non-linear, multi-point animation paths (e.g., bounce at 50%, settle at end) |
| **AnimatorListenerAdapter** | `ObjectAnimator.addListener(AnimatorListenerAdapter {...})` — show `onAnimationStart`, `onAnimationEnd`, `onAnimationRepeat` callbacks via Toast/log |

**Implementation approach**:
- Add cases 7, 8, 9 to the existing `when (index)` switch in `onImageClick()`
- Increment the `index` modulo 9 (was 6)
- Add explanatory comments for each new technique

---

## Registration Checklist (for each new Activity)

Each new Activity requires these 5 touchpoints:

| # | File | Change |
|---|------|--------|
| 1 | `RouterPath.kt` | Add constant in `object Anim` |
| 2 | `AndroidManifest.xml` | Add `<activity android:name=".activity.XxxActivity" />` |
| 3 | `AnimActivity.kt` | Add `RouterItem` in `buildRouter()` |
| 4 | New Activity `.kt` | Create file with `@Route` annotation |
| 5 | New Layout `.xml` | Create layout file(s) |

Plus documentation updates:
| # | File | Change |
|---|------|--------|
| 6 | `docs/modules.md` | Add rows to module_anim table |
| 7 | `README.md` | Update if module_anim section exists |

---

## Implementation Order

| Phase | Activity | Rationale |
|-------|----------|-----------|
| **Phase 1** | `ViewAnimationActivity` | Simplest — no new dependencies, well-understood API, establishes pattern for custom-layout Activities |
| **Phase 2** | `CircularRevealActivity` | Also simple, single API call, no new dependencies, demonstrates API-level gating |
| **Phase 3** | `SpringAnimationActivity` | Requires adding DynamicAnimation dependency, more complex touch handling |
| **Phase 4** | `MotionLayoutActivity` | Requires MotionScene XML, most complex (XML + programmatic), but ConstraintLayout already available |
| **Phase 5** | Enhance `AnimatorActivity` | Low-risk addition to existing file, no new dependencies |
| **Phase 6** | Update docs + AnimActivity router list + manifest | Final registration and documentation pass |

---

## Files to Create/Modify

### New Files (8)
| File | Description |
|------|-------------|
| `modules/module_anim/src/main/java/.../activity/ViewAnimationActivity.kt` | Tween animation demo |
| `modules/module_anim/src/main/java/.../activity/SpringAnimationActivity.kt` | Spring/DynamicAnimation demo |
| `modules/module_anim/src/main/java/.../activity/CircularRevealActivity.kt` | Circular reveal demo |
| `modules/module_anim/src/main/java/.../activity/MotionLayoutActivity.kt` | MotionLayout demo |
| `modules/module_anim/src/main/res/layout/anim_activity_view_animation.xml` | Layout for ViewAnimation |
| `modules/module_anim/src/main/res/layout/anim_activity_spring_animation.xml` | Layout for SpringAnimation |
| `modules/module_anim/src/main/res/layout/anim_activity_circular_reveal.xml` | Layout for CircularReveal |
| `modules/module_anim/src/main/res/layout/anim_activity_motion_layout.xml` | Layout for MotionLayout |
| `modules/module_anim/src/main/res/xml/anim_motion_scene.xml` | MotionScene for MotionLayout |

### Modified Files (7)
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add dynamicanimation version + library |
| `modules/module_anim/build.gradle.kts` | Add dynamicanimation dependency |
| `modules/module_anim/src/main/java/.../activity/AnimatorActivity.kt` | Add 3 new demo modes |
| `modules/module_anim/src/main/AndroidManifest.xml` | Register 4 new Activities |
| `modules/module_anim/src/main/java/.../AnimActivity.kt` | Add 4 new router items |
| `basic/basic_module/src/main/java/.../RouterPath.kt` | Add 4 route constants |
| `docs/modules.md` | Add 4 new rows to anim table |
