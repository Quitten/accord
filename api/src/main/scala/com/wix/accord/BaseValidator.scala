/*
  Copyright 2013-2017 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.wix.accord

/** Simplifies base validator implementation. Validators typically consist of an assertion/test and a resulting
  * violation; this implementation takes two functions that describe this behavior and wires the appropriate logic. For
  * example:
  *
  * ```
  * class IsNull extends BaseValidator[ AnyRef ]( _ == null, _ -> "is not a null" )
  * ```
  *
  * @param test The predicate that determines whether or not validation is successful.
  * @param failure A generator function for producing [[com.wix.accord.Failure]]s if validation fails. The helper
  *                methods in [[com.wix.accord.ViolationBuilder]] can be used to simplify this task.
  * @tparam T The object type this validator operates on.
  */
class BaseValidator[ T ]( val test: T => Boolean, val failure: T => Failure ) extends Validator [ T ] {
  def apply( value: T ): Result =
    if ( test( value ) ) Success else failure( value )
}

/** An extension to [[com.wix.accord.BaseValidator]] that transparently fails on nulls.
  *
  * @param test The predicate that determines whether or not validation is successful.
  * @param failure A generator function for producing [[com.wix.accord.Failure]]s if validation fails. The helper
  *                methods in [[com.wix.accord.ViolationBuilder]] can be used to simplify this task.
  * @param onNull The resulting failure for nulls. Defaults to [[com.wix.accord.Validator.nullFailure]].
  * @tparam T The object type this validator operates on.
  */
class NullSafeValidator[ T <: AnyRef ]( test: T => Boolean,
                                        failure: T => Failure,
                                        onNull: => Failure = Validator.nullFailure )
  extends BaseValidator[ T ]( test, failure ) {

  final override def apply( value: T ): Result =
    if ( value == null )
      onNull
    else if ( test( value ) )
      Success
    else
      failure( value )
}

/**
  * A type class that indicates whether or not a type is nullable.
  * @tparam T The type for which nullability is indicated.
  */
trait Nullability[ T ] {
  def isNullable: Boolean
}

object Nullability {
  import scala.language.implicitConversions

  /** Provides an instance of [[com.wix.accord.Nullability]] for reference types. */
  implicit def referenceTypeNullability[ T ]( implicit ev: T <:< AnyRef ): Nullability[ T ] =
    new Nullability[ T ] { def isNullable = true }

  /** Provides an instance of [[com.wix.accord.Nullability]] for value types. */
  implicit def valueTypeNullability[ T ]( implicit ev: T <:< AnyVal ): Nullability[ T ] =
    new Nullability[ T ] { def isNullable = false }
}

/** A base validator implementation that, depends on the specified type's [[com.wix.accord.Nullability nullability]],
  * checks for null values prior to executing the specified test.
  *
  * @param test The predicate that determines whether or not validation is successful.
  * @param onFailure A generator function for producing [[com.wix.accord.Failure]]s if validation fails. The helper
  *                  methods in [[com.wix.accord.ViolationBuilder]] can be used to simplify this task.
  * @param onNull The resulting failure for nulls. Defaults to [[com.wix.accord.Validator.nullFailure]].
  * @tparam T The object type this validator operates on.
  */
class MaybeNullSafeValidator[ T : Nullability ]( test: T => Boolean,
                                                 onFailure: T => Failure,
                                                 onNull: => Failure = Validator.nullFailure )
  extends Validator[ T ] {

  def apply( v: T ): Result =
    if ( implicitly[ Nullability[ T ] ].isNullable && v == null )
      onNull
    else if ( test( v ) )
      Success
    else
      onFailure( v )
}

