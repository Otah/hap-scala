package com.github.otah.hap.api.json

import com.github.otah.hap.api._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object AccessoryJson {

  // TODO check the exact field names

  private def build(what: Seq[Future[JsValue]])(f: JsArray => Map[String, JsValue])(implicit ec: ExecutionContext) =
    Future.sequence(what) map (seq => JsObject(f.apply(JsArray(seq.toVector))))

  def apply(accessory: Identified[HomeKitAccessory])(implicit ec: ExecutionContext): Future[JsObject] = {

    val (aid, acc) = accessory

    val allServices = acc.services/*:+ infoService*/ //TODO add info service

    val iids = allServices flatMap {
      case (serviceId, service) =>
        val characteristicsIds = service.characteristics map {
          case (characteristicId, _) => characteristicId
        }
        serviceId +: characteristicsIds
    }
    val duplicates = iids map (_.value) groupBy identity collect {
      case (iid, occurrences) if occurrences.tail.nonEmpty => s"$iid defined ${occurrences.size}x"
    }
    if (duplicates.nonEmpty) throw new IllegalStateException("Duplicate IIDs found: " + duplicates.mkString(", "))

    val servicesFutJson: Seq[Future[JsObject]] = allServices map { case (serviceId, service) =>
      val xxx = service.characteristics map {
        case (characteristicId, characteristic) => characteristic.asJson(characteristicId.value) // TODO glue IID here instead of in asJson?
      }
      build(xxx) { characteristics =>
        Map(
          "type" -> JsString(service.serviceType.minimalForm),
          "iid" -> JsNumber(serviceId.value),
          "characteristics" -> characteristics
        )
      }
    }

    build(servicesFutJson) { services =>
      Map(
        "aid" -> JsNumber(aid.value),
        "services" -> services
      )
    }
  }

  def list(accessories: Seq[Identified[HomeKitAccessory]])(implicit ec: ExecutionContext) =
    Future.sequence(accessories map AccessoryJson.apply) map { jsons =>
      JsObject("accessories" -> JsArray(jsons.toVector))
    }

  def characteristicsValues(characteristics: Seq[(Int, Int, Future[JsValue])])(implicit ec: ExecutionContext) = {
    val swapped = characteristics map {
      case (aid, iid, futureValue) => futureValue map ((aid, iid, _))
    }
    Future.sequence(swapped) map { results =>
      val jsons = results map {
        case (aid, iid, value) => JsObject(
          "aid" -> JsNumber(aid),
          "iid" -> JsNumber(iid),
          "value" -> value,
        )
      }
      JsObject("characteristics" -> JsArray(jsons.toVector))
    }
  }
}