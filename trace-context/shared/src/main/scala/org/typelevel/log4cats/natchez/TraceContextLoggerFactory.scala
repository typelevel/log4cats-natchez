package org.typelevel.log4cats.natchez

import cats.*
import cats.syntax.all.*
import natchez.Trace
import org.typelevel.log4cats.*

object TraceContextLoggerFactory {
  def apply[F[+_] : Monad : Trace](delegate: LoggerFactory[F]): TraceContextLoggerFactory[F] =
    new TraceContextLoggerFactory(delegate)
}

class TraceContextLoggerFactory[F[+_] : Monad : Trace](delegate: LoggerFactory[F]) extends LoggerFactory[F] {
  override def getLoggerFromName(name: String): TraceContextLogger[F] =
    new TraceContextLogger[F](delegate.getLoggerFromName(name))

  override def fromName(name: String): F[TraceContextLogger[F]] =
    delegate.fromName(name).map(new TraceContextLogger[F](_))
}
