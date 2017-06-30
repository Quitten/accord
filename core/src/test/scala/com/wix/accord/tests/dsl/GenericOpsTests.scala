/*
  Copyright 2013-2016 Wix.com

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

package com.wix.accord.tests.dsl

import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{Matchers, WordSpec}
import com.wix.accord.combinators.{AnInstanceOf, EqualTo, IsNotNull, IsNull, NotAnInstanceOf, NotEqualTo}

class GenericOpsTests extends WordSpec with Matchers with ResultMatchers {
  import GenericOpsTests._
  
  "The expression \"is aNull\"" should {
    "return an IsNull combinator" in {
      aNullValidator shouldBe an[ IsNull ]
    }
  }

  "The expression \"is notNull\"" should {
    "return an IsNotNull combinator" in {
      notNullValidator shouldBe an[ IsNotNull ]
    }
  }

  "The expression \"is equalTo\"" should {
    "return an EqualTo combinator" in {
      equalToValidator shouldBe an[ EqualTo[_] ]
    }
  }

  "The expression \"is notEqualTo\"" should {
    "return an NotEqualTo combinator" in {
      notEqualToValidator shouldBe a[ NotEqualTo[_] ]
    }
  }

  "The expression \"is anInstanceOf\"" should {
    "return an AnInstanceOf combinator" in {
      anInstanceOfValidator shouldBe an[ AnInstanceOf[_] ]
    }
  }

  "The expression \"is notAnInstanceOf\"" should {
    "return a NotAnInstanceOf combinator" in {
      notAnInstanceOfValidator shouldBe a[ NotAnInstanceOf[_] ]
    }
  }

  "Delegating via \"is valid\"" should {
    "result in the same description as directly delegating to the implicitly-resolved validator" in {
      val res1 = directDelegatingValidator( null ).toFailure.getOrElse( fail( "Failure expected!" ) )
      val res2 = indirectDelegatingValidator( null ).toFailure.getOrElse( fail( "Failure expected!" ) )
      res1.violations.map( _.path ) shouldEqual res2.violations.map( _.path )
    }
  }
}

object GenericOpsTests {
  import com.wix.accord.dsl._
  
  sealed trait Value
  case object Value1 extends Value
  case object Value2 extends Value
  
  val value: Value = Value2
  
  val aNullValidator = value is aNull
  val notNullValidator = value is notNull
  val equalToValidator = value is equalTo( Value1 )
  val notEqualToValidator = value is notEqualTo( Value1 )
  val anInstanceOfValidator = value is anInstanceOf[ Value1.type ]
  val notAnInstanceOfValidator = value is notAnInstanceOf[ Value1.type ]

  case class ConditionalTest( cond: Value, value: String )
  case class GuardedConditionalTest( cond: Int, value: String )

  val directDelegatingValidator = validator[ Value ] { _ is valid( notNullValidator ) }
  val indirectDelegatingValidator = validator[ Value ] { _ is notNullValidator }
}
