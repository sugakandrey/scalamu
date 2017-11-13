package org.scalamu.core.utils

import cats.{Applicative, Eval, Foldable, Traverse}

import scala.collection.Set

object SetInstances {
  implicit val setTraverse: Traverse[Set] = new Traverse[Set] {
    override def traverse[G[_]: Applicative, A, B](sa: Set[A])(f: A => G[B]): G[Set[B]] = {
      val G = Applicative[G]
      sa.foldLeft(G.pure(Set.empty[B])) { (buf, a) =>
        G.map2(buf, f(a))(_ + _)
      }
    }

    override def foldLeft[A, B](fa: Set[A], b: B)(f: (B, A) => B): B =
      fa.foldLeft(b)(f)

    override def foldRight[A, B](fa: Set[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      Foldable.iterateRight(fa, lb)(f)
  }
}
