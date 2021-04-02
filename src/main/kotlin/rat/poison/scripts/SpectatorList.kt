package rat.poison.scripts

import rat.poison.curSettings
import rat.poison.game.CSGO.csgoEXE
import rat.poison.game.entity.*
import rat.poison.game.forEntities
import rat.poison.game.me
import rat.poison.game.netvars.NetVarOffsets.m_hObserverTarget
import rat.poison.game.offsets.ClientOffsets.dwIndex
import rat.poison.overlay.App.haveTarget
import rat.poison.overlay.opened
import rat.poison.ui.uiPanels.specListText
import rat.poison.utils.every
import rat.poison.utils.extensions.readIndex
private const val id = "spectatorlist"
internal fun spectatorList() = every(100, inGameCheck = true) {
    if (!curSettings.bool["SPECTATOR_LIST"] || !curSettings.bool["MENU"]) {
        return@every
    }

    var spectators = ""
    var entCount = 1

    val playerSpecTarget = csgoEXE.readIndex(me + dwIndex)

    forEntities(EntityType.CCSPlayer, identifier = id) {
        val entity = it.entity

        if (entity.isSpectating() && !entity.hltv() && !entity.dormant()) {
            val entSpecTarget = csgoEXE.readIndex(entity + m_hObserverTarget)
            val entName = entity.name()

            if (entSpecTarget > -1 && entSpecTarget == playerSpecTarget) {
                if (!spectators.contains(entName)) {
                    spectators += "$entCount. $entName\n"
                    entCount++
                }
            }
        }
        return@forEntities
    }

    if (opened && haveTarget) {
        specListText.setText(spectators)
    }
}

// Move to normal spot whenever
fun Int.toIndex() = ((this and 0xFFF) - 1).run {
    if (this == 4094) -1 else this
}
