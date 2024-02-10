package site.ftka.survivalcore.initless.proprietaryEvents.annotations

import site.ftka.survivalcore.initless.proprietaryEvents.enums.PropEventPriority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropEventHandler(val priority: PropEventPriority = PropEventPriority.NORMAL)