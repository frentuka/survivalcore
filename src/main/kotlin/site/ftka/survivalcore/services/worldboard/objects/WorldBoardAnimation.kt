package site.ftka.survivalcore.services.worldboard.objects

/**
 * Defines the kinematic transitions applied to a WorldBoard when it spawns (In) and despawns (Out).
 */
enum class WorldBoardAnimation {
    /** Instantly appears with no interpolation. */
    NONE,
    
    /** Custom bouncy scale-in with overshoot and custom elastic spring pop-out. */
    POP,
    
    /** Starts 2 blocks below and smoothly rises while scaling up. */
    SLIDE_UP,
    
    /** Starts 2 blocks above and smoothly drops while scaling up. */
    SLIDE_DOWN,
    
    /** Executes a cinematic 180-degree Y-axis spin while scaling. */
    SPIN,
    
    /** Executes a cinematic 180-degree X-axis backflip while scaling. */
    FLIP,
    
    /** Smoothly fades the opacity of the text in and out without scaling. */
    FADE
}
