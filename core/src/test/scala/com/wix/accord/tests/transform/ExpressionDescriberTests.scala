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

package com.wix.accord.tests.transform

import com.wix.accord.Descriptions._
import org.scalatest.{Matchers, WordSpec}
import com.wix.accord.transform.ExpressionDescriber

class ExpressionDescriberTests extends WordSpec with Matchers {
  import com.wix.accord.dsl.Descriptor

  case class Nested( field: String )
  case class Test( field1: String, field2: String, nested: Nested )
  case class InSeq( field: Seq[ String ] )

  "A single-parameter function literal" should {
    "render an access chain description for a property getter" in {
      val path = ExpressionDescriber describe { ( t: Test ) => t.field1 }
      path shouldEqual Path( Generic( "field1" ) )
    }
    "render an access chain description for multiple indirections via property getters" in {
      val path = ExpressionDescriber describe { ( t: Test ) => t.nested.field }
      path shouldEqual Path( Generic( "nested" ), Generic( "field" ) )
    }
    "render an explicit description when \"as\" is used" in {
      val path = ExpressionDescriber describe { ( t: Test ) => t.field2 as "explicit" }
      path shouldEqual Path( Explicit( "explicit" ) )
    }
    "render a generic description for unsupported expressions" in {
      val path = ExpressionDescriber describe { ( _: Test ) => "arbitrary" }
      path shouldEqual Path( Generic( "\"arbitrary\"" ) )
    }
    "desugar unsupported expressions before rendering" in {
      val path = ExpressionDescriber describe { ( t: Test ) => t.field1.length * 2 }
      path shouldEqual Path( Generic( "t.field1.length * 2" ) )
    }
    "include the last token in full" in {
      val path = ExpressionDescriber describe { ( t: Test ) => t.field1.length * 180 }
      path shouldEqual Path( Generic( "t.field1.length * 180" ) )
    }
    "properly render a function call" in {
      def test( s: String ) = ???
      val path = ExpressionDescriber describe { ( t: Test ) => test( t.field1 ) }
      path shouldEqual Path( Generic( "test( t.field1 )" ) )
    }
    "successfully handle empty compiler-generated subtrees" in {
      // The Scala compiler (2.11.x at any rate) generates an empty subtree for the following
      // expression; an empty tree has no position, which is the root cause of #89.
      "com.wix.accord.transform.ExpressionDescriber describe { ( t: Test ) => t.field1.map( _.toUpper ) }" should compile
    }
    "handle anonymous functions in expressions" in {
      "com.wix.accord.transform.ExpressionDescriber describe { ( i: InSeq ) => i.field.collect { case p if p != \"\" => p } }" should compile
    }
    "render a self-reference description when the sample object itself is used anonymously" in pending
//    {
//      TODO find a way to encode such a function, or add yet another helper macro
//      val path = ExpressionDescriber describe[ String, String ] { _ }
//      path shouldEqual Path( SelfReference )
//    }
  }

  "ExpressionDescriber.describe" should {
    "fail to compile a non-literal function parameter" in {
      """
      val f: Int => String = { _ => "" }
      com.wix.accord.transform.ExpressionDescriber describe f
      """ shouldNot compile
    }
  }
}
