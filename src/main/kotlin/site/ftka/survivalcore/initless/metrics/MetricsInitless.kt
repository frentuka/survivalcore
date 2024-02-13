package site.ftka.survivalcore.initless.metrics

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass

class MetricsInitless(private val plugin: MClass) {
    val logger = plugin.loggingInitless.getLog("Metrics", Component.text("Metrics").color(NamedTextColor.BLUE))

    /* todo
        Tools to measure task execution times
        all around the project
     */

}