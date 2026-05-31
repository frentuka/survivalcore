package site.ftka.survivalcore.services.worldboard.objects

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import java.util.concurrent.atomic.AtomicBoolean

class WorldBoardInstance(
    private val plugin: MClass,
    val id: String,
    private val spawnLocation: Location,
    @Volatile private var currentText: Component
) {
    private var entity: TextDisplay? = null
    private var subtitleEntity: TextDisplay? = null
    private val iconEntities = mutableListOf<org.bukkit.entity.ItemDisplay>()
    
    @Volatile var subtitle: Component? = null
    
    fun getLocation(): Location? = entity?.location
    
    var icons: List<WorldBoardIcon> = emptyList()
    
    // Atomic safety switch to prevent race conditions during async Folia spawning
    private val isRemoved = AtomicBoolean(false)
    private val teleportGeneration = java.util.concurrent.atomic.AtomicInteger(0)

    // Premium default aesthetics
    var billboardSetting: Display.Billboard = Display.Billboard.CENTER
    var backgroundColorARGB: Color = Color.fromARGB(140, 10, 10, 10)
    var textGlow: Boolean = true
    var textShadow: Boolean = true
    var lineWidth: Int = 350
    var viewRange: Float = 1.0f // Approx 64 blocks visible distance
    
    // Frame facility
    var frame: WorldBoardFrame? = null
    var frameGradient: String? = null
    
    // Base scale
    var scale: Vector3f = Vector3f(1.1f, 1.1f, 1.1f)
    
    // Animation facility
    var animation: WorldBoardAnimation = WorldBoardAnimation.POP

    /**
     * Spawns the display entity safely using Folia's region scheduler.
     */
    internal fun spawn() {
        if (isRemoved.get()) return

        val finalComponent = if (frame != null) {
            frame!!.wrap(currentText, frameGradient ?: frame!!.defaultGradient)
        } else {
            currentText
        }

        val world = spawnLocation.world
        plugin.server.regionScheduler.execute(plugin, spawnLocation) {
            // Double check in case it was removed while queued
            if (isRemoved.get()) return@execute

            val spawnedEntity = world.spawn(spawnLocation, TextDisplay::class.java) { textDisplay ->
                textDisplay.text(finalComponent)
                textDisplay.billboard = billboardSetting
                textDisplay.backgroundColor = backgroundColorARGB
                textDisplay.isShadowed = textShadow
                textDisplay.lineWidth = lineWidth
                textDisplay.viewRange = viewRange
                
                // Add the persistent data tag to identify the entity across reloads/restarts
                val key = org.bukkit.NamespacedKey(plugin, "worldboard")
                textDisplay.persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, id)
                
                if (textGlow) {
                    textDisplay.brightness = Display.Brightness(15, 15)
                }

                // Calculate the exact initial state (In-State) based on the chosen animation
                val startScale = if (animation == WorldBoardAnimation.NONE || animation == WorldBoardAnimation.FADE) scale else Vector3f(0f, 0f, 0f)
                val startTrans = when(animation) {
                    WorldBoardAnimation.SLIDE_UP -> Vector3f(0f, -2f, 0f)
                    WorldBoardAnimation.SLIDE_DOWN -> Vector3f(0f, 2f, 0f)
                    else -> Vector3f(0f, 0f, 0f)
                }
                val startRot = when(animation) {
                    WorldBoardAnimation.SPIN -> org.joml.Quaternionf().rotateY(Math.PI.toFloat())
                    WorldBoardAnimation.FLIP -> org.joml.Quaternionf().rotateX(Math.PI.toFloat())
                    else -> org.joml.Quaternionf()
                }

                textDisplay.transformation = Transformation(
                    startTrans,
                    startRot,
                    startScale,
                    org.joml.Quaternionf()
                )
                
                if (animation == WorldBoardAnimation.FADE) {
                    textDisplay.textOpacity = 0.toByte()
                }
            }
            this.entity = spawnedEntity

            val spawnedSubtitle = world.spawn(spawnLocation, TextDisplay::class.java) { textDisplay ->
                textDisplay.text(subtitle ?: Component.empty())
                textDisplay.billboard = billboardSetting
                textDisplay.backgroundColor = org.bukkit.Color.fromARGB(0, 0, 0, 0)
                textDisplay.isShadowed = textShadow
                textDisplay.lineWidth = lineWidth
                textDisplay.viewRange = viewRange
                
                val key = org.bukkit.NamespacedKey(plugin, "worldboard")
                textDisplay.persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, id + "_sub")
                
                if (textGlow) textDisplay.brightness = Display.Brightness(15, 15)

                val subScale = if (animation == WorldBoardAnimation.NONE || animation == WorldBoardAnimation.FADE) {
                    Vector3f(scale.x * 0.6f, scale.y * 0.6f, scale.z * 0.6f)
                } else {
                    Vector3f(0f, 0f, 0f)
                }
                
                // Position it below the main text
                val startTrans = when(animation) {
                    WorldBoardAnimation.SLIDE_UP -> Vector3f(0f, -2f - 0.35f, 0f)
                    WorldBoardAnimation.SLIDE_DOWN -> Vector3f(0f, 2f - 0.35f, 0f)
                    else -> Vector3f(0f, -0.35f, 0f)
                }
                val startRot = when(animation) {
                    WorldBoardAnimation.SPIN -> org.joml.Quaternionf().rotateY(Math.PI.toFloat())
                    WorldBoardAnimation.FLIP -> org.joml.Quaternionf().rotateX(Math.PI.toFloat())
                    else -> org.joml.Quaternionf()
                }

                textDisplay.transformation = Transformation(
                    startTrans,
                    startRot,
                    subScale,
                    org.joml.Quaternionf()
                )
                
                if (animation == WorldBoardAnimation.FADE) textDisplay.textOpacity = 0.toByte()
            }
            this.subtitleEntity = spawnedSubtitle

            for (iconDef in icons) {
                val iconDisp = world.spawn(spawnLocation, org.bukkit.entity.ItemDisplay::class.java) { itemDisplay ->
                    itemDisplay.setItemStack(iconDef.item)
                    itemDisplay.billboard = billboardSetting
                    itemDisplay.viewRange = viewRange
                    
                    val key = org.bukkit.NamespacedKey(plugin, "worldboard")
                    itemDisplay.persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, id)
                    
                    if (textGlow) {
                        itemDisplay.brightness = Display.Brightness(15, 15)
                    }
                    
                    val startScale = if (animation == WorldBoardAnimation.NONE || animation == WorldBoardAnimation.FADE) {
                        Vector3f(scale.x * iconDef.scaleMultiplier, scale.y * iconDef.scaleMultiplier, scale.z * iconDef.scaleMultiplier)
                    } else {
                        Vector3f(0f, 0f, 0f)
                    }
                    
                    val finalTranslation = Vector3f(
                        iconDef.translation.x * scale.x,
                        iconDef.translation.y * scale.y,
                        iconDef.translation.z * scale.z
                    )
                    
                    itemDisplay.transformation = Transformation(
                        finalTranslation,
                        org.joml.Quaternionf(),
                        startScale,
                        org.joml.Quaternionf()
                    )
                }
                this.iconEntities.add(iconDisp)
            }

            // Trigger smooth scaling pop-in animation on the client's GPU
            // We use a 3L tick delay so the client definitely registers the initial state before the interpolation packet is sent
            val finalEntity = spawnedEntity
            finalEntity.scheduler.execute(plugin, {
                if (finalEntity.isValid && !isRemoved.get() && animation != WorldBoardAnimation.NONE) {
                    if (animation == WorldBoardAnimation.POP) {
                        // Elastic/Bouncy Pop Intro
                        // Step 1: Snappy Overshoot (0.0 -> 1.35 over 6 ticks)
                        finalEntity.interpolationDuration = 6
                        finalEntity.interpolationDelay = 0
                        finalEntity.transformation = Transformation(
                            Vector3f(0f, 0f, 0f),
                            org.joml.Quaternionf(),
                            Vector3f(scale.x * 1.2f, scale.y * 1.2f, scale.z * 1.2f),
                            org.joml.Quaternionf()
                        )
                        
                        // Step 2: Compensating Undershoot
                        finalEntity.scheduler.execute(plugin, {
                            if (finalEntity.isValid && !isRemoved.get()) {
                                finalEntity.interpolationDuration = 4
                                finalEntity.interpolationDelay = 0
                                finalEntity.transformation = Transformation(
                                    Vector3f(0f, 0f, 0f),
                                    org.joml.Quaternionf(),
                                    Vector3f(scale.x * 0.95f, scale.y * 0.95f, scale.z * 0.95f),
                                    org.joml.Quaternionf()
                                )
                            }
                        }, null, 6L)
                        
                        // Step 3: Settle
                        finalEntity.scheduler.execute(plugin, {
                            if (finalEntity.isValid && !isRemoved.get()) {
                                finalEntity.interpolationDuration = 3
                                finalEntity.interpolationDelay = 0
                                finalEntity.transformation = Transformation(
                                    Vector3f(0f, 0f, 0f),
                                    org.joml.Quaternionf(),
                                    scale,
                                    org.joml.Quaternionf()
                                )
                            }
                        }, null, 10L)
                    } else if (animation == WorldBoardAnimation.FADE) {
                        // Vanilla text_opacity does NOT interpolate automatically!
                        // We must manually interpolate it over 20 ticks (1 second) using ease-in-out
                        for (i in 1..20) {
                            finalEntity.scheduler.execute(plugin, {
                                if (finalEntity.isValid && !isRemoved.get()) {
                                    val t = i.toDouble() / 20.0
                                    val ease = (1.0 - kotlin.math.cos(Math.PI * t)) / 2.0
                                    val opacity = (ease * 255).toInt().toByte()
                                    finalEntity.textOpacity = opacity
                                }
                            }, null, i.toLong())
                        }
                    } else {
                        // Standard linear animations
                        finalEntity.interpolationDuration = 10
                        finalEntity.interpolationDelay = 0
                        
                        // The standard default target matrix for all boards
                        finalEntity.transformation = Transformation(
                            Vector3f(0f, 0f, 0f),
                            org.joml.Quaternionf(),
                            scale,
                            org.joml.Quaternionf()
                        )
                    }
                }
                
                // Animate subtitle
                subtitleEntity?.scheduler?.execute(plugin, {
                    val sub = subtitleEntity ?: return@execute
                    if (sub.isValid && !isRemoved.get() && animation != WorldBoardAnimation.NONE) {
                        if (animation == WorldBoardAnimation.POP) {
                            sub.interpolationDuration = 6
                            sub.interpolationDelay = 0
                            sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(scale.x * 0.7f, scale.y * 0.7f, scale.z * 0.7f), org.joml.Quaternionf())
                            sub.scheduler.execute(plugin, {
                                if (sub.isValid && !isRemoved.get()) {
                                    sub.interpolationDuration = 4
                                    sub.interpolationDelay = 0
                                    sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(scale.x * 0.55f, scale.y * 0.55f, scale.z * 0.55f), org.joml.Quaternionf())
                                }
                            }, null, 6L)
                            sub.scheduler.execute(plugin, {
                                if (sub.isValid && !isRemoved.get()) {
                                    sub.interpolationDuration = 3
                                    sub.interpolationDelay = 0
                                    sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(scale.x * 0.6f, scale.y * 0.6f, scale.z * 0.6f), org.joml.Quaternionf())
                                }
                            }, null, 10L)
                        } else if (animation == WorldBoardAnimation.FADE) {
                            for (i in 1..20) {
                                sub.scheduler.execute(plugin, {
                                    if (sub.isValid && !isRemoved.get()) {
                                        val t = i.toDouble() / 20.0
                                        val ease = (1.0 - kotlin.math.cos(Math.PI * t)) / 2.0
                                        sub.textOpacity = (ease * 255).toInt().toByte()
                                    }
                                }, null, i.toLong())
                            }
                        } else {
                            sub.interpolationDuration = 10
                            sub.interpolationDelay = 0
                            sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(scale.x * 0.6f, scale.y * 0.6f, scale.z * 0.6f), org.joml.Quaternionf())
                        }
                    }
                }, null, 3L)
            }, null, 3L)

            val spawnedIcons = this.iconEntities.toList() // Snapshot for async task
            for ((index, finalIcon) in spawnedIcons.withIndex()) {
                val iconDef = icons.getOrNull(index) ?: continue
                finalIcon.scheduler.execute(plugin, {
                    if (finalIcon.isValid && !isRemoved.get() && animation != WorldBoardAnimation.NONE) {
                        finalIcon.interpolationDuration = 10
                        finalIcon.interpolationDelay = 0
                        
                        val finalTranslation = Vector3f(
                            iconDef.translation.x * scale.x,
                            iconDef.translation.y * scale.y,
                            iconDef.translation.z * scale.z
                        )
                        
                        finalIcon.transformation = Transformation(
                            finalTranslation,
                            org.joml.Quaternionf(),
                            Vector3f(scale.x * iconDef.scaleMultiplier, scale.y * iconDef.scaleMultiplier, scale.z * iconDef.scaleMultiplier),
                            org.joml.Quaternionf()
                        )
                    }
                }, null, 3L)
            }
        }
    }

    /**
     * Updates the text dynamically on the Folia entity thread.
     */
    fun updateText(newText: Component) {
        this.currentText = newText
        val activeEntity = entity ?: return

        val finalComponent = if (frame != null) {
            frame!!.wrap(currentText, frameGradient ?: frame!!.defaultGradient)
        } else {
            currentText
        }
        
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && !isRemoved.get()) {
                activeEntity.text(finalComponent)
            }
        }, null, 1L)
    }

    /**
     * Updates both the text and the icons dynamically.
     * Old icons are despawned and new ones are spawned in their place.
     */
    fun updateContent(newText: Component, newIcons: List<WorldBoardIcon>, newSubtitle: Component? = null) {
        this.subtitle = newSubtitle
        updateText(newText)
        this.icons = newIcons

        val activeEntity = entity ?: return
        
        subtitleEntity?.scheduler?.execute(plugin, {
            if (subtitleEntity?.isValid == true && !isRemoved.get()) {
                subtitleEntity?.text(newSubtitle ?: Component.empty())
            }
        }, null, 0L)
        
        // Schedule icon replacement on the region thread
        activeEntity.scheduler.execute(plugin, {
            if (isRemoved.get() || !activeEntity.isValid) return@execute
            val world = activeEntity.world

            // Remove old icons
            val oldIcons = this.iconEntities.toList()
            for (icon in oldIcons) {
                if (icon.isValid) icon.remove()
            }
            this.iconEntities.clear()

            // Spawn new icons
            for (iconDef in newIcons) {
                world.spawnEntity(activeEntity.location, org.bukkit.entity.EntityType.ITEM_DISPLAY, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM) { spawned ->
                    val itemDisplay = spawned as org.bukkit.entity.ItemDisplay
                    itemDisplay.setItemStack(iconDef.item)
                    itemDisplay.billboard = billboardSetting
                    itemDisplay.viewRange = viewRange
                    
                    val key = org.bukkit.NamespacedKey(plugin, "worldboard")
                    itemDisplay.persistentDataContainer.set(key, org.bukkit.persistence.PersistentDataType.STRING, id)
                    
                    val finalTranslation = org.joml.Vector3f(
                        iconDef.translation.x * scale.x,
                        iconDef.translation.y * scale.y,
                        iconDef.translation.z * scale.z
                    )
                    
                    itemDisplay.transformation = org.bukkit.util.Transformation(
                        finalTranslation,
                        org.joml.Quaternionf(),
                        org.joml.Vector3f(scale.x * iconDef.scaleMultiplier, scale.y * iconDef.scaleMultiplier, scale.z * iconDef.scaleMultiplier),
                        org.joml.Quaternionf()
                    )
                    
                    this.iconEntities.add(itemDisplay)
                }
            }
        }, null, 1L)
    }

    /**
     * Checks if the given entity is owned by this board instance.
     */
    fun isOwnedEntity(checkEntity: org.bukkit.entity.Display): Boolean {
        if (this.entity?.uniqueId == checkEntity.uniqueId) return true
        if (this.subtitleEntity?.uniqueId == checkEntity.uniqueId) return true
        if (this.iconEntities.any { it.uniqueId == checkEntity.uniqueId }) return true
        return false
    }

    /**
     * Updates the scale dynamically.
     */
    fun triggerScaleAnimation(targetScale: Vector3f, durationTicks: Int) {
        val activeEntity = entity ?: return
        
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && !isRemoved.get()) {
                activeEntity.interpolationDuration = durationTicks
                activeEntity.interpolationDelay = 0
                
                val currentTrans = activeEntity.transformation
                activeEntity.transformation = Transformation(
                    currentTrans.translation,
                    currentTrans.leftRotation,
                    targetScale,
                    currentTrans.rightRotation
                )
            }
        }, null, 1L)
    }

    /**
     * Complete Matrix Interpolation: smoothly animates scale, translation, and rotation simultaneously!
     */
    fun triggerTransformAnimation(
        targetScale: Vector3f? = null,
        targetTranslation: Vector3f? = null,
        targetLeftRotation: org.joml.Quaternionf? = null,
        targetRightRotation: org.joml.Quaternionf? = null,
        durationTicks: Int
    ) {
        val activeEntity = entity ?: return
        
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && !isRemoved.get()) {
                activeEntity.interpolationDuration = durationTicks
                activeEntity.interpolationDelay = 0
                
                val currentTrans = activeEntity.transformation
                activeEntity.transformation = Transformation(
                    targetTranslation ?: currentTrans.translation,
                    targetLeftRotation ?: currentTrans.leftRotation,
                    targetScale ?: currentTrans.scale,
                    targetRightRotation ?: currentTrans.rightRotation
                )
            }
        }, null, 1L)
    }

    @Volatile var targetLocation: Location? = null

    fun teleport(location: Location, durationTicks: Int = 0) {
        val activeEntity = entity ?: return
        this.targetLocation = location.clone()
        
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && !isRemoved.get()) {
                activeEntity.teleportDuration = durationTicks.coerceAtLeast(0)
                activeEntity.teleportAsync(location)
            }
        }, null, 0L)
        
        subtitleEntity?.scheduler?.execute(plugin, {
            if (subtitleEntity?.isValid == true && !isRemoved.get()) {
                subtitleEntity?.teleportDuration = durationTicks.coerceAtLeast(0)
                subtitleEntity?.teleportAsync(location)
            }
        }, null, 0L)
        
        for (activeIcon in iconEntities) {
            activeIcon.scheduler.execute(plugin, {
                if (activeIcon.isValid && !isRemoved.get()) {
                    activeIcon.teleportDuration = durationTicks.coerceAtLeast(0)
                    activeIcon.teleportAsync(location)
                }
            }, null, 0L)
        }
    }

    /**
     * Safely despawns the board with a smooth "Out" scale animation.
     */
    internal fun remove(instant: Boolean = false) {
        if (isRemoved.getAndSet(true)) return // Prevent double-execution
        
        val activeEntity = entity ?: return
        val activeIcons = iconEntities.toList()
        
        if (instant) {
            if (activeEntity.isValid && !plugin.stopping) {
                try {
                    activeEntity.remove()
                } catch (t: Throwable) {}
            }
            for (activeIcon in activeIcons) {
                if (activeIcon.isValid && !plugin.stopping) {
                    try {
                        activeIcon.remove()
                    } catch (t: Throwable) {}
                }
            }
            entity = null
            subtitleEntity?.let {
                if (it.isValid && !plugin.stopping) try { it.remove() } catch (t: Throwable) {}
            }
            subtitleEntity = null
            iconEntities.clear()
            return
        }

        for ((index, activeIcon) in activeIcons.withIndex()) {
            val iconDef = icons.getOrNull(index) ?: continue
            activeIcon.scheduler.execute(plugin, {
                if (activeIcon.isValid && animation != WorldBoardAnimation.NONE) {
                    activeIcon.interpolationDuration = 10
                    activeIcon.interpolationDelay = 0
                    
                    val finalTranslation = Vector3f(
                        iconDef.translation.x * scale.x,
                        iconDef.translation.y * scale.y,
                        iconDef.translation.z * scale.z
                    )
                    
                    activeIcon.transformation = Transformation(
                        finalTranslation,
                        org.joml.Quaternionf(),
                        Vector3f(0f, 0f, 0f),
                        org.joml.Quaternionf()
                    )
                }
            }, null, 1L)
        }

        // 1. Trigger Outro Animation
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && animation != WorldBoardAnimation.NONE) {
                if (animation == WorldBoardAnimation.POP) {
                    // Snappy Bouncy/Elastic Outro
                    // Step 1: Anticipation Wind-up (scale up to 1.25f over 3 ticks)
                    activeEntity.interpolationDuration = 3
                    activeEntity.interpolationDelay = 0
                    activeEntity.transformation = Transformation(
                        Vector3f(0f, 0f, 0f),
                        org.joml.Quaternionf(),
                        Vector3f(1.25f, 1.25f, 1.25f),
                        org.joml.Quaternionf()
                    )
                    
                    // Step 2: Rapid drop to 0.3f (4 ticks duration, scheduled 3 ticks later)
                    activeEntity.scheduler.execute(plugin, {
                        if (activeEntity.isValid) {
                            activeEntity.interpolationDuration = 4
                            activeEntity.interpolationDelay = 0
                            activeEntity.transformation = Transformation(
                                Vector3f(0f, 0f, 0f),
                                org.joml.Quaternionf(),
                                Vector3f(0.3f, 0.3f, 0.3f),
                                org.joml.Quaternionf()
                            )
                        }
                    }, null, 3L)

                    // Step 3: Elastic recoil bounce back to 0.55f (3 ticks duration, scheduled 7 ticks later)
                    activeEntity.scheduler.execute(plugin, {
                        if (activeEntity.isValid) {
                            activeEntity.interpolationDuration = 3
                            activeEntity.interpolationDelay = 0
                            activeEntity.transformation = Transformation(
                                Vector3f(0f, 0f, 0f),
                                org.joml.Quaternionf(),
                                Vector3f(0.55f, 0.55f, 0.55f),
                                org.joml.Quaternionf()
                            )
                        }
                    }, null, 7L)

                    // Step 4: Rapid final collapse to 0.0f (3 ticks duration, scheduled 10 ticks later)
                    activeEntity.scheduler.execute(plugin, {
                        if (activeEntity.isValid) {
                            activeEntity.interpolationDuration = 3
                            activeEntity.interpolationDelay = 0
                            activeEntity.transformation = Transformation(
                                Vector3f(0f, 0f, 0f),
                                org.joml.Quaternionf(),
                                Vector3f(0.0f, 0.0f, 0.0f),
                                org.joml.Quaternionf()
                            )
                        }
                    }, null, 10L)
                } else if (animation == WorldBoardAnimation.FADE) {
                    // Manually interpolate opacity out over 20 ticks using ease-in-out
                    for (i in 1..20) {
                        activeEntity.scheduler.execute(plugin, {
                            if (activeEntity.isValid) {
                                val t = i.toDouble() / 20.0
                                val ease = (1.0 - kotlin.math.cos(Math.PI * t)) / 2.0
                                val opacity = (255 - (ease * 255).toInt()).toByte()
                                activeEntity.textOpacity = opacity
                            }
                        }, null, i.toLong())
                    }
                } else {
                    // Standard linear outro
                    activeEntity.interpolationDuration = 20
                    activeEntity.interpolationDelay = 0
                    
                    val targetScale = Vector3f(0f, 0f, 0f)
                    val targetTrans = when(animation) {
                        WorldBoardAnimation.SLIDE_UP -> Vector3f(0f, -2f, 0f)
                        WorldBoardAnimation.SLIDE_DOWN -> Vector3f(0f, 2f, 0f)
                        else -> Vector3f(0f, 0f, 0f)
                    }
                    val targetRot = when(animation) {
                        WorldBoardAnimation.SPIN -> org.joml.Quaternionf().rotateY(Math.PI.toFloat())
                        WorldBoardAnimation.FLIP -> org.joml.Quaternionf().rotateX(Math.PI.toFloat())
                        else -> org.joml.Quaternionf()
                    }

                    activeEntity.transformation = Transformation(
                        targetTrans,
                        targetRot,
                        targetScale,
                        org.joml.Quaternionf()
                    )
                }
            }
        }, null, 1L)

        // 2. Actually remove the entity after the animation finishes
        val removeDelay = if (animation == WorldBoardAnimation.POP) 15L else if (animation == WorldBoardAnimation.FADE) 25L else 25L
        activeEntity.scheduler.execute(plugin, {
            if (activeEntity.isValid && !plugin.stopping) {
                try {
                    activeEntity.remove()
                } catch (t: Throwable) {
                    // Ignore NMS region shutdown exceptions
                }
            }
            subtitleEntity?.let {
                if (it.isValid && !plugin.stopping) try { it.remove() } catch (t: Throwable) {}
            }
            for (activeIcon in activeIcons) {
                if (activeIcon.isValid && !plugin.stopping) {
                    try {
                        activeIcon.remove()
                    } catch (t: Throwable) {}
                }
            }
            entity = null
            subtitleEntity = null
            iconEntities.clear()
        }, null, removeDelay)
        
        // Outro for subtitle
        subtitleEntity?.scheduler?.execute(plugin, {
            val sub = subtitleEntity ?: return@execute
            if (sub.isValid && animation != WorldBoardAnimation.NONE) {
                if (animation == WorldBoardAnimation.POP) {
                    sub.interpolationDuration = 3
                    sub.interpolationDelay = 0
                    sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(0.7f, 0.7f, 0.7f), org.joml.Quaternionf())
                    sub.scheduler.execute(plugin, {
                        if (sub.isValid) {
                            sub.interpolationDuration = 4
                            sub.interpolationDelay = 0
                            sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(0.15f, 0.15f, 0.15f), org.joml.Quaternionf())
                        }
                    }, null, 3L)
                    sub.scheduler.execute(plugin, {
                        if (sub.isValid) {
                            sub.interpolationDuration = 3
                            sub.interpolationDelay = 0
                            sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(0.3f, 0.3f, 0.3f), org.joml.Quaternionf())
                        }
                    }, null, 7L)
                    sub.scheduler.execute(plugin, {
                        if (sub.isValid) {
                            sub.interpolationDuration = 3
                            sub.interpolationDelay = 0
                            sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(0.0f, 0.0f, 0.0f), org.joml.Quaternionf())
                        }
                    }, null, 10L)
                } else if (animation == WorldBoardAnimation.FADE) {
                    for (i in 1..20) {
                        sub.scheduler.execute(plugin, {
                            if (sub.isValid) {
                                val t = i.toDouble() / 20.0
                                val ease = (1.0 - kotlin.math.cos(Math.PI * t)) / 2.0
                                sub.textOpacity = (255 - (ease * 255).toInt()).toByte()
                            }
                        }, null, i.toLong())
                    }
                } else {
                    sub.interpolationDuration = 20
                    sub.interpolationDelay = 0
                    sub.transformation = Transformation(Vector3f(0f, -0.35f, 0f), org.joml.Quaternionf(), Vector3f(0f, 0f, 0f), org.joml.Quaternionf())
                }
            }
        }, null, 1L)
    }
}
