package site.ftka.survivalcore.services.language.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.language.objects.LanguagePack
import java.io.File

class LanguageService_InputSubservice(private val service: LanguageService, private val plugin: MClass) {

    private val languagePackFolderAbsolutePath = "${plugin.dataFolder.absolutePath}\\language"

    fun gatherAllLanguagePacks(): List<LanguagePack> {
        val lpackList = mutableListOf<LanguagePack>()

        val languagePackFolderFile = File(languagePackFolderAbsolutePath)
        if (!languagePackFolderFile.exists()) { languagePackFolderFile.mkdirs(); return lpackList }
        languagePackFolderFile.listFiles()?.forEach {
            val lpackobj = tryToDeserializeIntoLPack(it)
            lpackobj?.let { lpackList.add(it) }
        }

        return lpackList
    }

    private fun tryToDeserializeIntoLPack(file: File): LanguagePack? {
        return try {
            val text = file.readText()
            service.fromJson(text)
        } catch (e: Exception) { null }
    }

}