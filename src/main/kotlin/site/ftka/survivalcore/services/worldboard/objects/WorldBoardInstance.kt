package site.ftka.survivalcore.services.worldboard.objects

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import site.ftka.survivalcore.MClass
import java.util.concurrent.atomic.AtomicBoolean

class WorldBoardInstance(
    private val plugin: MClass,
    val id: String,
    private val spawnLocation: Location,
    @Volatile private var currentText: Component
) {
    private var entity: TextDisplay? = null
    
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
            }, null, 3L)
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
        }, null, 0L)
    }

    /**
     * Smoothly animates the board on the client's GPU over time.
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

    /**
     * Safely teleports the board to a new location on the region thread.
     * Uses manual 1-tick interpolation combined with client-side 1-tick linear interpolation
     * to create a perfectly smooth ease-in-out cinematic curve.
     */
    fun teleport(location: Location, durationTicks: Int = 0) {
        val activeEntity = entity ?: return
        
        if (durationTicks <= 0) {
            activeEntity.scheduler.execute(plugin, {
                if (activeEntity.isValid && !isRemoved.get()) {
                    activeEntity.teleportDuration = 0
                    activeEntity.teleportAsync(location)
                }
            }, null, 0L)
            return
        }
        
        val gen = teleportGeneration.incrementAndGet()
        
        activeEntity.scheduler.execute(plugin, {
            if (!activeEntity.isValid || isRemoved.get()) return@execute
            if (teleportGeneration.get() != gen) return@execute
            
            val startLoc = activeEntity.location.clone()
            
            for (i in 1..durationTicks) {
                activeEntity.scheduler.execute(plugin, {
                    if (activeEntity.isValid && !isRemoved.get() && teleportGeneration.get() == gen) {
                        val t = i.toDouble() / durationTicks
                        // Sine ease-in-out curve
                        val ease = (1.0 - kotlin.math.cos(Math.PI * t)) / 2.0
                        
                        val currentLoc = startLoc.clone().add(
                            (location.x - startLoc.x) * ease,
                            (location.y - startLoc.y) * ease,
                            (location.z - startLoc.z) * ease
                        )
                        currentLoc.yaw = location.yaw
                        currentLoc.pitch = location.pitch
                        
                        activeEntity.teleportDuration = 1
                        activeEntity.teleportAsync(currentLoc)
                    }
                }, null, i.toLong())
            }
        }, null, 0L)
    }

    /**
     * Safely despawns the board with a smooth "Out" scale animation.
     */
    internal fun remove(instant: Boolean = false) {
        if (isRemoved.getAndSet(true)) return // Prevent double-execution
        
        val activeEntity = entity ?: return
        
        if (instant) {
            if (activeEntity.isValid) {
                activeEntity.remove()
            }
            entity = null
            return
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
            if (activeEntity.isValid) {
                activeEntity.remove()
            }
            entity = null
        }, null, removeDelay)
    }
}
