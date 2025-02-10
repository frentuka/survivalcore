package site.ftka.survivalcore.essentials.chat.objects

import net.kyori.adventure.text.Component

abstract class ChatScreen {

    abstract val name: String
    var isActive: Boolean = true

    val timeoutMillis: Long
        get() = if (currentPage == "home" || currentPage == "player")
                30L * 1000
            else
                60L * 1000


    // <Name, Page>
    open var screenContent: MutableMap<String, ChatScreenPage> = mutableMapOf()

    /**
     * The current page of the screen.
     * @see (default) -> "home"
     */
    var currentPage: String = "home"
        set(value) {
            if (field != value && screenContent.containsKey(value))
                if (history.isNotEmpty() && history[history.size - 1] != value) // don't add the same page to the history
                    history.add(value)
            if (screenContent.containsKey(value))
                field = value
        }

    val history: MutableList<String> = mutableListOf(currentPage)

    fun getCurrentChatScreenPageObject(): ChatScreenPage?
        = screenContent[currentPage]

    fun previousPage() {
        print(history.toString())

        if (history.size > 1) {
            history.removeAt(history.size - 1)
            currentPage = history[history.size - 1]
        } else
            currentPage = "home"
    }

    // the Process It boolean can be turned false to prevent StackOverflow when a screen watches the same screen
    fun getFrame(processIt: Boolean = true): Component?
        = screenContent[currentPage]?.getMessage(processIt)
}