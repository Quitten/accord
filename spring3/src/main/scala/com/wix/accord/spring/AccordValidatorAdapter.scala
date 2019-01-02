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

package com.wix.accord.spring

import org.springframework.validation.Errors
import scala.reflect.ClassTag
import com.wix.accord._

/** An implementation of Spring Validation's [[org.springframework.validation.Validator]] which provides an
  * adapter to an Accord [[com.wix.accord.Validator]].
  */
class AccordValidatorAdapter[ T : ClassTag ]( validator: Validator[ T ] )
  extends org.springframework.validation.Validator with SpringAdapterBase {

  def supports( clazz: Class[_] ) = implicitly[ ClassTag[ T ] ].runtimeClass isAssignableFrom clazz

  override def validate( target: Any, errors: Errors ): Unit = {
    // Safety net
    if ( !supports( target.getClass ) )
      throw new IllegalArgumentException( s"Class ${target.getClass.getName} is not supported by this validator" )
    applyAdaptedValidator( validator, target.asInstanceOf[ T ], errors )
  }
}
