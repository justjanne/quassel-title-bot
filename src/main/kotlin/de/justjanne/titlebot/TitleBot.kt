package de.justjanne.titlebot

import de.justjanne.libquassel.client.session.ClientSession
import de.justjanne.libquassel.protocol.connection.ProtocolFeature
import de.justjanne.libquassel.protocol.connection.ProtocolMeta
import de.justjanne.libquassel.protocol.connection.ProtocolVersion
import de.justjanne.libquassel.protocol.features.FeatureSet
import de.justjanne.libquassel.protocol.io.CoroutineChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import org.intellij.lang.annotations.Language
import org.jsoup.Jsoup
import java.net.InetSocketAddress
import javax.net.ssl.SSLContext

@Language("RegExp")
const val URL_REGEX =
  "(https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*)"

class TitleBot(
  address: InetSocketAddress,
  private val authentication: Pair<String, String>,
  private val clientInfo: Pair<String, String>
) {
  private val regex = URL_REGEX.toRegex()

  private val channel = CoroutineChannel().apply {
    runBlocking { connect(address) }
  }
  private val session = ClientSession(
    channel,
    ProtocolFeature.all,
    listOf(
      ProtocolMeta(
        ProtocolVersion.Datastream,
        0x0000u
      )
    ),
    SSLContext.getInstance("TLSv1.3").apply {
      init(null, null, null)
    }
  )

  suspend fun connect() {
    session.handshakeHandler.init(
      clientInfo.first,
      clientInfo.second,
      FeatureSet.all()
    )
    session.handshakeHandler.login(
      authentication.first,
      authentication.second
    )
    session.baseInitHandler.waitForInitDone()
    session.rpcHandler.messages().collect {
      val matches = regex.findAll(it.content)
      for (match in matches) {
        val doc = runInterruptible {
          Jsoup.connect(match.value).get()
        }
        session.rpcHandler.sendInput(
          it.bufferInfo,
          "/PRINT ${doc.title()}"
        )
      }
    }
  }
}
