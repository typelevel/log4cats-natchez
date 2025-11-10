package org.typelevel.log4cats.natchez

import cats.*
import cats.syntax.all.*
import natchez.Trace
import org.typelevel.log4cats.*

class TraceContextLogger[F[_] : Monad : Trace](delegate: SelfAwareStructuredLogger[F]) extends DelegatingLogger[F](delegate) {
  protected override def transformation(f: SelfAwareStructuredLogger[F] => F[Unit]): F[Unit] =
    (Trace[F].traceId, Trace[F].spanId)
      .mapN {
        case (Some(traceId), Some(spanId)) =>
          delegate.addContext("traceId" -> Show.Shown(traceId), "spanId" -> Show.Shown(spanId))

        case (Some(traceId), None) =>
          delegate.addContext("traceId" -> Show.Shown(traceId))

        case (None, Some(spanId)) =>
          delegate.addContext("spanId" -> Show.Shown(spanId))

        case (None, None) => delegate
      }
      .flatMap(f)
}
