package org.typelevel.log4cats.natchez

import org.typelevel.log4cats.*

private[natchez] abstract class DelegatingLogger[F[_]](delegate: SelfAwareStructuredLogger[F]) extends SelfAwareStructuredLogger[F] {
  protected def transformation(f: SelfAwareStructuredLogger[F] => F[Unit]): F[Unit]

  override def isTraceEnabled: F[Boolean] = delegate.isTraceEnabled
  override def isDebugEnabled: F[Boolean] = delegate.isDebugEnabled
  override def isInfoEnabled: F[Boolean] = delegate.isInfoEnabled
  override def isWarnEnabled: F[Boolean] = delegate.isWarnEnabled
  override def isErrorEnabled: F[Boolean] = delegate.isErrorEnabled

  override def trace(ctx: Map[String, String])(msg: => String): F[Unit] = transformation(_.trace(ctx)(msg))
  override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = transformation(_.trace(ctx, t)(msg))
  override def debug(ctx: Map[String, String])(msg: => String): F[Unit] = transformation(_.debug(ctx)(msg))
  override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = transformation(_.debug(ctx, t)(msg))
  override def info(ctx: Map[String, String])(msg: => String): F[Unit] = transformation(_.info(ctx)(msg))
  override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = transformation(_.info(ctx, t)(msg))
  override def warn(ctx: Map[String, String])(msg: => String): F[Unit] = transformation(_.warn(ctx)(msg))
  override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = transformation(_.warn(ctx, t)(msg))
  override def error(ctx: Map[String, String])(msg: => String): F[Unit] = transformation(_.error(ctx)(msg))
  override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] = transformation(_.error(ctx, t)(msg))
  override def error(t: Throwable)(message: => String): F[Unit] = transformation(_.error(t)(message))
  override def warn(t: Throwable)(message: => String): F[Unit] = transformation(_.warn(t)(message))
  override def info(t: Throwable)(message: => String): F[Unit] = transformation(_.info(t)(message))
  override def debug(t: Throwable)(message: => String): F[Unit] = transformation(_.debug(t)(message))
  override def trace(t: Throwable)(message: => String): F[Unit] = transformation(_.trace(t)(message))
  override def error(message: => String): F[Unit] = transformation(_.error(message))
  override def warn(message: => String): F[Unit] = transformation(_.warn(message))
  override def info(message: => String): F[Unit] = transformation(_.info(message))
  override def debug(message: => String): F[Unit] = transformation(_.debug(message))
  override def trace(message: => String): F[Unit] = transformation(_.trace(message))
}
