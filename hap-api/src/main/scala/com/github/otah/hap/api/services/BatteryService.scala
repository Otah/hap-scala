package com.github.otah.hap.api.services

import com.github.otah.hap.api.characteristics._

trait BatteryService extends HigherKindService with OptionalName {

  override final val serviceType = hap.service.battery

  def battery: Required[BatteryCharacteristic]

  def statusLowBattery: Required[StatusLowBatteryCharacteristic]

  def chargingState: Required[ChargingStateCharacteristic]

  override def options: Options = Options(name, battery, statusLowBattery, chargingState)
}
