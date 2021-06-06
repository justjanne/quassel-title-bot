package de.justjanne.titlebot

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.net.InetSocketAddress

const val BOT_NAME = "Quassel Title Bot"
const val BOT_VERSION = "2021-06-06"

suspend fun main(args: Array<String>) {
  val parser = ArgParser("quasseltitlebot")
  val host = parser.option(ArgType.String, fullName = "hostname").required()
  val port = parser.option(ArgType.Int, fullName = "port").default(4242)
  val username = parser.option(ArgType.String, fullName = "username").required()
  val password = parser.option(ArgType.String, fullName = "password").required()
  parser.parse(args)

  val bot = TitleBot(
    InetSocketAddress(host.value, port.value),
    Pair(username.value, password.value),
    Pair(BOT_NAME, BOT_VERSION)
  )
  bot.connect()
}
