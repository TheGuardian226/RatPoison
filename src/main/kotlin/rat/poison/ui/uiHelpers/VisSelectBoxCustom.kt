package rat.poison.ui.uiHelpers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.Tooltip
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTable
import rat.poison.curSettings
import rat.poison.ui.changed
import rat.poison.ui.tabs.categorySelected
import rat.poison.ui.tabs.updateDisableRCrosshair

//Swap VisSelectBoxCustom to showText false is mainText is " "
class VisSelectBoxCustom(mainText: String, varName: String, useCategory: Boolean, showText: Boolean = true, vararg items: String) : VisTable() {
    private val textLabel = mainText
    private val variableName = varName
    private val useGunCategory = useCategory
    private var hasTooltip = false

    private var boxLabel = VisLabel("$textLabel:")
    private val selectBox = VisSelectBox<String>()

    private val boxItems = items

    var value = 0

    init {
        val itemsArray = Array<String>()
        for (i in boxItems) {
            itemsArray.add(i)
        }

        selectBox.items = itemsArray
        update()
        updateTooltip()

        selectBox.changed { _, _ ->
            curSettings[if (useGunCategory) { categorySelected + variableName } else { variableName }] = boxItems[selectBox.selectedIndex]

            updateDisableRCrosshair()

            false
        }

        if (showText) {
            add(boxLabel).width(200F)
        }

        add(selectBox).spaceRight(6F)
    }

    fun update() {
        selectBox.selectedIndex = boxItems.indexOf(curSettings[if (useGunCategory) { categorySelected + variableName } else { variableName }].toUpperCase())

        updateTooltip()
    }

    private fun updateTooltip() {
        if (curSettings.bool["MENU_TOOLTIPS"]) {
            //TODO tooltips
        } else {
            if (hasTooltip) {
                Tooltip.removeTooltip(this)
                hasTooltip = false
            }
        }
    }

    fun disable(bool: Boolean, col: Color) {
        boxLabel.color = col
        selectBox.color = col
        selectBox.isDisabled = bool
    }
}