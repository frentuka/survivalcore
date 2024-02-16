package site.ftka.survivalcore.essentials.configs

class ConfigsAPI(private val ess: ConfigsEssential) {

    fun generalCfg()
        = ess.generalCfg()

    fun playerDataCfg()
        = ess.playerdataCfg()

}