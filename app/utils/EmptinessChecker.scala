/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.Mirror

sealed trait EmptyPathsResult

object EmptyPathsResult {
  case class EmptyPaths(values: List[String]) extends EmptyPathsResult
  case object NoEmptyPaths                    extends EmptyPathsResult
  case object CompletelyEmpty                 extends EmptyPathsResult
}

/** Type class to locate paths to empty objects or arrays within an instance of an object.
 */
trait EmptinessChecker[A] {
  import EmptinessChecker.*

  final def findEmptyPaths(a: A): EmptyPathsResult = {

    def search(acc: List[String], path: String, structure: Structure): List[String] = {
      def recurseIfNotEmpty(keyedChildren: Seq[(String, Structure)]) =
        if (keyedChildren.isEmpty) {
          path :: acc
        } else {
          keyedChildren.foldLeft(acc) { case (acc, (name, value)) =>
            search(acc, s"$path/$name", value)
          }
        }

      structure match {
        case obj: Structure.Obj => recurseIfNotEmpty(obj.keyedChildren)
        case arr: Structure.Arr => recurseIfNotEmpty(arr.keyedChildren)
        case _                  => acc
      }
    }

    val structure = structureOf(a)

    structure match {
      // So that we can treat RuleIncompleteOrEmptyBody for completely empty body specially...
      case o: Structure.Obj if o.keyedChildren.isEmpty => EmptyPathsResult.CompletelyEmpty
      case _ =>
        search(Nil, "", structure) match {
          case Nil   => EmptyPathsResult.NoEmptyPaths
          case paths => EmptyPathsResult.EmptyPaths(paths.reverse)
        }
    }
  }

  protected def structureOf(value: A): Structure
}

// Internal specialization of EmptinessChecker for object instances so we can directly access its fields
private[utils] trait ObjEmptinessChecker[A] extends EmptinessChecker[A] {
  import EmptinessChecker.*

  def structureOf(value: A): Structure.Obj
}

object EmptinessChecker {

  def findEmptyPaths[A: EmptinessChecker](a: A): EmptyPathsResult =
    EmptinessChecker[A].findEmptyPaths(a)

  private[utils] sealed abstract class Structure

  private[utils] object Structure {

    case class Obj(fields: List[(String, Structure)]) extends Structure {
      def keyedChildren: Seq[(String, Structure)] = fields.filter(_._2 != Structure.Null)
    }

    case class Arr(items: Seq[Structure]) extends Structure {
      def keyedChildren: Seq[(String, Structure)] = items.zipWithIndex.map(x => x._2.toString -> x._1)
    }

    case object Primitive extends Structure
    case object Null      extends Structure
  }

  def apply[A](using aInstance: EmptinessChecker[A]): EmptinessChecker[A] = aInstance

  private def instance[A](func: A => Structure): EmptinessChecker[A] = (value: A) => func(value)

  def use[A](func: A => List[(String, Structure)]): EmptinessChecker[A] = EmptinessChecker.instance { a =>
    Structure.Obj(func(a))
  }

  def field[A](name: String, value: A)(using checker: EmptinessChecker[A]): (String, Structure) = name -> checker.structureOf(value)

  def primitive[A]: EmptinessChecker[A] = EmptinessChecker.instance(_ => Structure.Primitive)

  given EmptinessChecker[String]  = instance(_ => Structure.Primitive)
  given EmptinessChecker[Int]     = instance(_ => Structure.Primitive)
  given EmptinessChecker[Double]  = instance(_ => Structure.Primitive)
  given EmptinessChecker[Boolean] = instance(_ => Structure.Primitive)

  given EmptinessChecker[BigInt]     = instance(_ => Structure.Primitive)
  given EmptinessChecker[BigDecimal] = instance(_ => Structure.Primitive)

  given [A](using aInstance: EmptinessChecker[A]): EmptinessChecker[Option[A]] =
    instance(opt => opt.map(aInstance.structureOf).getOrElse(Structure.Null))

  given [A](using aInstance: EmptinessChecker[A]): EmptinessChecker[Seq[A]] =
    instance(seq => Structure.Arr(seq.map(aInstance.structureOf)))

  given [A](using aInstance: EmptinessChecker[A]): EmptinessChecker[List[A]] =
    instance(list => Structure.Arr(list.map(aInstance.structureOf)))

  // Lazy prevents infinite recursion in generic derivation
  final class Lazy[+A](val value: () => A) extends AnyVal

  object Lazy {
    given [A](using a: => A): Lazy[A] = new Lazy(() => a)
  }

  inline given derived[A](using m: Mirror.ProductOf[A]): EmptinessChecker[A] =
    instance { a =>
      val elemLabels    = summonLabels[m.MirroredElemLabels]
      val elemInstances = summonAllInstances[m.MirroredElemTypes]
      val elems         = a.asInstanceOf[Product].productIterator.toList
      val fields = elemLabels.lazyZip(elems).lazyZip(elemInstances).map { (label, value, checker) =>
        label -> checker.value().structureOf(value)
      }
      Structure.Obj(fields)
    }

  private inline def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match {
      case _: (h *: t)   => constValue[h].asInstanceOf[String] :: summonLabels[t]
      case _: EmptyTuple => Nil
    }

  private inline def summonAllInstances[T <: Tuple]: List[Lazy[EmptinessChecker[Any]]] =
    inline erasedValue[T] match {
      case _: (h *: t)   => summonInline[Lazy[EmptinessChecker[h]]].asInstanceOf[Lazy[EmptinessChecker[Any]]] :: summonAllInstances[t]
      case _: EmptyTuple => Nil
    }

}