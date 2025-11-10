package org.typelevel.log4cats.natchez

import cats.*
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import munit.{CatsEffectSuite, ScalaCheckSuite}
import natchez.{Kernel, Span, Trace, TraceValue}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.effect.PropF
import org.scalacheck.{Arbitrary, Gen}
import org.typelevel.log4cats.testing.StructuredTestingLogger
import org.typelevel.log4cats.testing.StructuredTestingLogger.*

import java.net.URI

class TraceContextLoggerSpec extends CatsEffectSuite with ScalaCheckSuite {

  private val genLogMessage: Gen[LogMessage] =
    for {
      msg <- arbitrary[String]
      ex <- arbitrary[Option[Throwable]]
      ctx <- arbitrary[Map[String, String]]
      logMessage <- Gen.oneOf(
        Gen.const(TRACE(msg, ex, ctx)),
        Gen.const(DEBUG(msg, ex, ctx)),
        Gen.const(INFO(msg, ex, ctx)),
        Gen.const(WARN(msg, ex, ctx)),
        Gen.const(ERROR(msg, ex, ctx))
      )
    } yield logMessage
  private implicit val arbitraryLogMessage: Arbitrary[LogMessage] = Arbitrary(genLogMessage)

  test("log events have trace details attached as context") {
    PropF.forAllF { (arbitraryTraceId: Option[String],
                     arbitrarySpanId: Option[String],
                     events: Vector[LogMessage],
                    ) =>
      for {
        underlying <- StructuredTestingLogger.ref[IO]()
        trace = new Trace[IO] {
          override def traceId: IO[Option[String]] = arbitraryTraceId.pure[IO]
          override def spanId(implicit F: Applicative[IO]): IO[Option[String]] = arbitrarySpanId.pure[IO]

          override def put(fields: (String, TraceValue)*): IO[Unit] = IO.unit
          override def log(fields: (String, TraceValue)*): IO[Unit] = IO.unit
          override def log(event: String): IO[Unit] = IO.unit
          override def attachError(err: Throwable, fields: (String, TraceValue)*): IO[Unit] = IO.unit
          override def kernel: IO[Kernel] = IO.stub
          override def spanR(name: String, options: Span.Options): Resource[IO, IO ~> IO] = Resource.eval(IO.stub)
          override def span[A](name: String, options: Span.Options)(k: IO[A]): IO[A] = IO.stub
          override def traceUri: IO[Option[URI]] = none.pure[IO]
        }

        logger = new TraceContextLogger(underlying)(implicitly, trace)

        _ <- events.traverse {
          case TRACE(message, Some(ex), ctx) if ctx.isEmpty => logger.trace(ex)(message)
          case TRACE(message, None, ctx) if ctx.isEmpty => logger.trace(message)
          case TRACE(message, Some(ex), ctx) => logger.trace(ctx, ex)(message)
          case TRACE(message, None, ctx) => logger.trace(ctx)(message)

          case DEBUG(message, Some(ex), ctx) if ctx.isEmpty => logger.debug(ex)(message)
          case DEBUG(message, None, ctx) if ctx.isEmpty => logger.debug(message)
          case DEBUG(message, Some(ex), ctx) => logger.debug(ctx, ex)(message)
          case DEBUG(message, None, ctx) => logger.debug(ctx)(message)

          case INFO(message, Some(ex), ctx) if ctx.isEmpty => logger.info(ex)(message)
          case INFO(message, None, ctx) if ctx.isEmpty => logger.info(message)
          case INFO(message, Some(ex), ctx) => logger.info(ctx, ex)(message)
          case INFO(message, None, ctx) => logger.info(ctx)(message)

          case WARN(message, Some(ex), ctx) if ctx.isEmpty => logger.warn(ex)(message)
          case WARN(message, None, ctx) if ctx.isEmpty => logger.warn(message)
          case WARN(message, Some(ex), ctx) => logger.warn(ctx, ex)(message)
          case WARN(message, None, ctx) => logger.warn(ctx)(message)

          case ERROR(message, Some(ex), ctx) if ctx.isEmpty => logger.error(ex)(message)
          case ERROR(message, None, ctx) if ctx.isEmpty => logger.error(message)
          case ERROR(message, Some(ex), ctx) => logger.error(ctx, ex)(message)
          case ERROR(message, None, ctx) => logger.error(ctx)(message)
        }

        loggedEvents <- underlying.logged
      } yield {
        assertEquals(loggedEvents, events.map {
          case TRACE(msg, t, ctx) =>
            TRACE(msg, t, addContext(arbitraryTraceId, arbitrarySpanId)(ctx))
          case DEBUG(msg, t, ctx) =>
            DEBUG(msg, t, addContext(arbitraryTraceId, arbitrarySpanId)(ctx))
          case INFO(msg, t, ctx) =>
            INFO(msg, t, addContext(arbitraryTraceId, arbitrarySpanId)(ctx))
          case WARN(msg, t, ctx) =>
            WARN(msg, t, addContext(arbitraryTraceId, arbitrarySpanId)(ctx))
          case ERROR(msg, t, ctx) =>
            ERROR(msg, t, addContext(arbitraryTraceId, arbitrarySpanId)(ctx))
        })
      }
    }
  }

  private def addContext(traceId: Option[String],
                         spanId: Option[String])
                        (ctx: Map[String, String]): Map[String, String] = {
    ctx ++ traceId.map("traceId" -> _) ++ spanId.map("spanId" -> _)
  }
}
