/*
  Copyright 2013-2019 Wix.com

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

import org.scalatest.{FlatSpec, Matchers}
import com.wix.accord.scalatest.ResultMatchers


object AlternativeValidationSyntaxTests {
  import com.wix.accord.dsl._

  case class Person( firstName: String, lastName: String )
  implicit val personValidator = validator[ Person ] { p =>
    p.firstName is notEmpty
    p.lastName is notEmpty
  }
}

class AlternativeValidationSyntaxTests extends FlatSpec with Matchers with ResultMatchers {
  import AlternativeValidationSyntaxTests._

  "Importing com.wix.accord.Implicits" should "enable alternative validation invocation syntax" in {
    import com.wix.accord.Implicits._

    val person = Person( "Edsger", "Dijkstra" )
    person.validate should be( aSuccess )
  }
}
