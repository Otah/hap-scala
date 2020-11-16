package com.github.otah.hap.server.beowulfe

import java.net.InetAddress

import com.github.otah.hap.api._
import com.github.otah.hap.api.server.HomeKitServer
import com.typesafe.scalalogging.Logger
import io.github.hapjava.server.impl.HomekitServer

import scala.concurrent.ExecutionContext

object BeowulfeServerDriver {

  private val log = Logger[BeowulfeServerDriver.type]

  def run(serverDef: HomeKitServer)(implicit ec: ExecutionContext): Unit = {

    val netAddress = serverDef.host map InetAddress.getByName getOrElse InetAddress.getLocalHost
    val port = serverDef.port

    log.info(s"HomeKit server address is $netAddress, port $port")

    val server = new HomekitServer(netAddress, port)

    import BeowulfeAccessoryAdapter.Implicit._

    val authInfo = new BeowulfeAuthInfoAdapter(serverDef.root.auth)

    serverDef.root.rootDevice match {
      case Left(accessory) =>
        // it seems aid 1 is reserved for bridge only in HAP Java
        server.createStandaloneAccessory(authInfo, 2 <=> accessory)

      case Right(bridgeDef) =>
        val info = bridgeDef.info
        import info._
        val bridge = server.createBridge(
          authInfo, label, manufacturer, model, serialNumber,
          firmwareRevision.asString, hardwareRevision.map(_.asString).orNull
        )
        bridgeDef.accessories foreach (acc => bridge.addAccessory(acc))
    }
  }
}