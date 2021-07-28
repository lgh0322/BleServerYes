package com.vaca.bleserveryes


interface DeviceAPI {
	/**
	 * Change the value of the GATT characteristic that we're publishing
	 */
	fun setMyCharacteristicValue(value: String)
}