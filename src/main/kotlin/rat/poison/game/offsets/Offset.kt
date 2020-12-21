package rat.poison.game.offsets

import com.sun.jna.Memory
import com.sun.jna.Pointer
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.jire.kna.Addressed
import org.jire.kna.attach.AttachedModule
import org.jire.kna.attach.windows.WindowsAttachedModule
import org.jire.kna.attach.windows.WindowsAttachedProcess
import rat.poison.utils.extensions.readForced
import rat.poison.utils.extensions.unsign
import kotlin.reflect.KProperty

class Offset(val module: AttachedModule, private val patternOffset: Long, private val addressOffset: Long,
             val read: Boolean, private val subtract: Boolean, private val mask: ByteArray) : Addressed {
	
	companion object {
		val memoryByModule = Object2ObjectArrayMap<AttachedModule, Memory>()
		
		private fun Offset.cachedMemory(): Memory {
			var memory = memoryByModule[module]
			if (memory == null) {
				memory = Memory(module.size)
				if (module is WindowsAttachedModule) {
					module.readForced(0, memory, module.size.toInt())
				}
				memoryByModule[module] = memory
			}
			return memory
		}
	}
	
	private val memory = cachedMemory()
	
	override val address: Long = run {
		val offset = module.size - mask.size
		val process = module.process as WindowsAttachedProcess
		val readMemory = Memory(4)
		
		var currentAddress = 0L
		while (currentAddress < offset) {
			if (memory.mask(currentAddress, mask)) {
				currentAddress += module.address + patternOffset
				if (read) {
					if (process.readForced(currentAddress, readMemory, 4) == 0L) throw IllegalStateException("Couldn't resolve currentAddress pointer")
					currentAddress = readMemory.getInt(0).unsign()
				}
				if (subtract) currentAddress -= module.address
				return@run currentAddress + addressOffset
			}
			currentAddress++
		}
		
		throw IllegalStateException("Failed to resolve offset")
	}
	
	private var value = -1L
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>): Long {
		if (value == -1L)
			value = address
		return value
	}
	
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
		this.value = value
	}
	
}

fun Pointer.mask(offset: Long, mask: ByteArray, skipZero: Boolean = true): Boolean {
	for (i in 0..mask.lastIndex) {
		val value = mask[i]
		if (skipZero && 0 == value.toInt()) continue
		if (value != getByte(offset + i))
			return false
	}
	return true
}