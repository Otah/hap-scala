package com.github.otah.hap.api.services.experimental.sensor

import com.github.otah.hap.api._
import com.github.otah.hap.api.characteristics._
import com.github.otah.hap.api.services._
import experimental._

trait HumiditySensorService extends AccessoryService with experimental.OptionalName with Has3Characteristics {

  override final val serviceType = hap.service.sensor.humidity

  def currentHumidity: CurrentRelativeHumidityCharacteristic

  def statusActive: Option[StatusActiveCharacteristic] = None

  override def characteristics = Characteristics(
    id1 ->> name,
    id2 -> currentHumidity,
    id3 ->> statusActive,
  )
}