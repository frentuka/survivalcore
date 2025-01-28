package site.ftka.survivalcore.essentials.configs

/*
 this class should be internal, as it's only meant to be used internally
 */
internal class ConfigsAPI(private val ess: ConfigsEssential) {

    fun generalCfg()
        = ess.generalCfg()

    fun playerDataCfg()
        = ess.playerdataCfg()

}