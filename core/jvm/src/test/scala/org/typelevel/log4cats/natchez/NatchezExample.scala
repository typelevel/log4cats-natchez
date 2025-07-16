package org.typelevel.log4cats.natchez

import cats.*
import cats.data.Kleisli
import cats.effect.{Trace as _, *}
import cats.syntax.all.*
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.resources.Resource as OTResource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.{BatchSpanProcessor, SimpleSpanProcessor}
import io.opentelemetry.semconv.ServiceAttributes
import natchez.*
import natchez.opentelemetry.*
import org.typelevel.log4cats.*

import scala.util.control.NoStackTrace

object NatchezExample extends IOApp.Simple {
  private def app[F[_] : Applicative : Trace]: F[Unit] =
    StructuredLogger[F].info(Map("it's me" -> "hi"))("hello") *>
      StructuredLogger[F].warn(Map("hi" -> "I'm the problem it's me"), new RuntimeException("boom") with NoStackTrace {})("Hmm, might be a problem")

  override def run: IO[Unit] =
    OpenTelemetry.entryPoint[IO](globallyRegister = true) { builder =>
      Resource.fromAutoCloseable(IO {
        BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build).build()
      })
        .flatMap { bsp =>
          Resource.fromAutoCloseable(IO {
            SdkTracerProvider
              .builder()
              .setResource {
                OTResource
                  .getDefault
                  .merge(OTResource.create(Attributes.of(
                    ServiceAttributes.SERVICE_NAME, "NatchezExample",
                  )))
              }
              .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create))
              .addSpanProcessor(bsp)
              .build()
          })
        }
        .evalMap(stp => IO(builder.setTracerProvider(stp)))
    }
      .use { entryPoint =>
        entryPoint.root("NatchezExample")
          .use(app[Kleisli[IO, Span[IO], *]].run)
      }
}
