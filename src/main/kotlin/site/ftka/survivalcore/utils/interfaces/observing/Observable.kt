package site.ftka.survivalcore.utils.interfaces.observing

interface Observable {
    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)
    fun notifyObservers()
}