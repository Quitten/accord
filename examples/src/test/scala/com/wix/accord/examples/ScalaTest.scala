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

package com.wix.accord.examples

import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.util.Random

class ScalaTest extends AnyWordSpec with Matchers with ResultMatchers {
  import com.wix.accord._

  val validAdult = Adult( name = "Grace", surname = "Hopper", age = 85, contactInfo = "Arlington National Cemetery" )

  "Adult validator" should {
    "succeed on a valid adult" in {
      validate( validAdult ) shouldBe aSuccess
    }
    "fail on an invalid person" in {
      val invalidAdult = validAdult.copy( name = "" )
      validate( invalidAdult ) shouldBe aFailure
    }
    "fail on a minor" in {
      val invalidAdult = validAdult.copy( age = 15 )
      validate( invalidAdult ) should failWith( "age" -> "got 15, expected 18 or more" )
    }
    "fail on an adult with missing contact info" in {
      val invalidAdult = validAdult.copy( contactInfo = "" )
      validate( invalidAdult ) should failWith( "contactInfo" -> "must not be empty" )
    }
  }

  "Minor validator" should {
    val validMinor = Minor(
      name = "Blaise",
      surname = "Pascal",
      age = 16,
      guardians = Set( Adult( name = "Étienne", surname = "Pascal", age = 63, contactInfo = "Paris, France" ) ) )

    "succeed on a valid minor" in {
      validate( validMinor ) shouldBe aSuccess
    }

    "fail on an invalid person" in {
      val invalidMinor = validMinor.copy( name = "" )
      validate( invalidMinor ) shouldBe aFailure
    }

    "fail on an adult" in {
      val invalidMinor = validMinor.copy( age = 25 )
      validate( invalidMinor ) should failWith( "age" -> "got 25, expected less than 18" )
    }

    "fail on a minor with no guardians" in {
      val invalidMinor = validMinor.copy( guardians = Set.empty )
      validate( invalidMinor ) should failWith( "guardians" -> "must not be empty" )
    }

    "fail on a minor with an invalid guardian" in {
      val invalidMinor = validMinor.copy( guardians = Set( validAdult.copy( name = "" ) ) )
      validate( invalidMinor ) should failWith(
        GroupViolationMatcher(
          legacyDescription = "guardians",
          violations = Set(
            GroupViolationMatcher(
              legacyDescription = "value",
              constraint = "is invalid" ) ) ) )
    }
  }

  "Classroom validator" should {
    val minorPool: Iterator[ Minor ] =
      Iterator.continually {
        Minor( name = Random.nextString( 10 ), surname = Random.nextString( 10 ), age = Random.nextInt( 18 ), guardians = Set( validAdult ) )
      }
    val validClassroom = new Classroom( grade = 3, teacher = validAdult, students = minorPool.take( 20 ).toSet )

    "succeed on a valid classroom" in {
      validate( validClassroom ) shouldBe aSuccess
    }

    "fail on a classroom with an invalid grade" in {
      val invalidClassroom = validClassroom.copy( grade = -3 )
      validate( invalidClassroom ) should failWith( "grade" -> "got -3, expected between 1 and 12" )
    }

    "fail on a classroom with an invalid teacher" in {
      val invalidClassroom = validClassroom.copy( teacher = validAdult.copy( age = -5 ) )
      validate( invalidClassroom ) should failWith( GroupViolationMatcher( legacyDescription = "teacher", constraint = "is invalid" ) )
    }

    "fail on a classroom with an invalid student" in {
      val invalidStudent = minorPool.next().copy( name = "" )
      val invalidClassroom = validClassroom.copy( students = validClassroom.students + invalidStudent )
      validate( invalidClassroom ) should failWith(
        GroupViolationMatcher(
          legacyDescription = "students",
          violations = Set(
            GroupViolationMatcher(
              legacyDescription = "value",
              constraint = "is invalid" ) ) ) )
    }

    "fail on an empty classroom" in {
      val invalidClassroom = validClassroom.copy( students = Set.empty )
      validate( invalidClassroom ) should failWith( "students" -> "has size 0, expected between 18 and 25" )
    }

    "fail on an overcrowded classroom" in {
      val invalidClassroom = validClassroom.copy( students = minorPool.take( 100 ).toSet )
      validate( invalidClassroom ) should failWith( "students" -> "has size 100, expected between 18 and 25" )
    }
  }
}
