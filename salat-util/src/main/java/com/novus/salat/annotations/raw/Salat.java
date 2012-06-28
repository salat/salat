/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-util
 * Class:         Salat.java
 * Last modified: 2012-06-27 23:42:09 EDT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat.annotations.raw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optimises deserialization for class hierarchies.  Although <i>not necessary</i> for deserializing, by giving Salat a
 * hint that the class is typed to a trait or an abstract superclass, it removes the need for Salat to try to figure this
 * out via class introspection.
 * <p/>
 * {@code
 *  import com.novus.salat.annotations._
 *
 *  @Salat
 *  trait BabyAnimal
 *  case class Kitten() extends BabyAnimal
 *  case class Bunny() extends BabyAnimal
 *  case class Duckling() extends BabyAnimal
 * }
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Salat {}
