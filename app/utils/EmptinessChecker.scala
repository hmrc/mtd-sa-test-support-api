/*
 * Copyright 2023 HM Revenue & Customs
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

import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

sealed trait EmptyPathsResult

object EmptyPathsResult {
  case class EmptyPaths(values: List[String]) extends EmptyPathsResult
  case object NoEmptyPaths                    extends EmptyPathsResult
  case object CompletelyEmpty                 extends EmptyPathsResult
}

/** Type class to locate paths to empty objects or arrays within an instance of an object.
  */
trait EmptinessChecker[A] {
  import EmptinessChecker._

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
        case o: Structure.Obj => recurseIfNotEmpty(o.keyedChildren)
        case a: Structure.Arr => recurseIfNotEmpty(a.keyedChildren)
        case _                => acc
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
  import EmptinessChecker._

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

  def apply[A](implicit aInstance: EmptinessChecker[A]): EmptinessChecker[A] = aInstance

  def instance[A](func: A => Structure): EmptinessChecker[A] = (value: A) => func(value)

  def instanceObj[A](func: A => Structure.Obj): ObjEmptinessChecker[A] = (value: A) => func(value)

  def use[A, B: EmptinessChecker](func: A => B): EmptinessChecker[A] = EmptinessChecker.instance { a =>
    val b = func(a)
    EmptinessChecker[B].structureOf(b)
  }

  def primitive[A]: EmptinessChecker[A] = EmptinessChecker.instance(_ => Structure.Primitive)

  implicit val stringInstance: EmptinessChecker[String]   = instance(_ => Structure.Primitive)
  implicit val intInstance: EmptinessChecker[Int]         = instance(_ => Structure.Primitive)
  implicit val doubleInstance: EmptinessChecker[Double]   = instance(_ => Structure.Primitive)
  implicit val booleanInstance: EmptinessChecker[Boolean] = instance(_ => Structure.Primitive)

  implicit val bigIntInstance: EmptinessChecker[BigInt]         = instance(_ => Structure.Primitive)
  implicit val bigDecimalInstance: EmptinessChecker[BigDecimal] = instance(_ => Structure.Primitive)

  implicit def optionInstance[A](implicit aInstance: EmptinessChecker[A]): EmptinessChecker[Option[A]] =
    instance(opt => opt.map(aInstance.structureOf).getOrElse(Structure.Null))

  implicit def seqInstance[A, I](implicit aInstance: EmptinessChecker[A]): EmptinessChecker[Seq[A]] =
    instance(list => Structure.Arr(list.map(aInstance.structureOf)))

  implicit def listInstance[A](implicit aInstance: EmptinessChecker[A]): EmptinessChecker[List[A]] =
    instance(list => Structure.Arr(list.map(aInstance.structureOf)))

  implicit val hnilInstance: ObjEmptinessChecker[HNil] = instanceObj(_ => Structure.Obj(Nil))

  implicit def hlistInstance[K <: Symbol, H, T <: HList](implicit
      witness: Witness.Aux[K],
      hInstance: Lazy[EmptinessChecker[H]],
      tInstance: ObjEmptinessChecker[T]
  ): ObjEmptinessChecker[FieldType[K, H] :: T] =
    instanceObj { case h :: t =>
      val hField  = witness.value.name -> hInstance.value.structureOf(h)
      val tFields = tInstance.structureOf(t).fields
      Structure.Obj(hField :: tFields)
    }

  implicit def simpleHlistInstance[H, T <: HList](implicit
      hInstance: Lazy[EmptinessChecker[H]],
      tInstance: ObjEmptinessChecker[T]
  ): ObjEmptinessChecker[(String, H) :: T] =
    instanceObj { case h :: t =>
      val hField  = h._1 -> hInstance.value.structureOf(h._2)
      val tFields = tInstance.structureOf(t).fields
      Structure.Obj(hField :: tFields)
    }

  implicit def genericInstance[A, R](implicit
      gen: LabelledGeneric.Aux[A, R],
      enc: Lazy[EmptinessChecker[R]]
  ): EmptinessChecker[A] =
    instance(a => enc.value.structureOf(gen.to(a)))

}
