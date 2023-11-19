package site.ftka.survivalcore.essentials.proprietaryEvents.annotations

import site.ftka.survivalcore.essentials.proprietaryEvents.enums.PropEventPriority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropEventHandler(val priority: PropEventPriority = PropEventPriority.NORMAL)