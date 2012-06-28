/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         EnumAs.java
 * Last modified: 2012-06-28 15:37:35 EDT
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

import com.novus.salat.EnumStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Override context enum handling defaults on an ad hoc basis for an individual enum.
 * <p/>
 * To force an individual enum to be serialized using its ordinal values:
 * {@code
 *     import com.novus.salat.annotations._
 *
 *     @EnumAs(strategy = EnumStrategy.BY_ID)
 *     object BabyAnimalsById extends Enumeration {
 *       val Kitten, Puppy, Bunny, Cub, Fawn, Duckling, Calf = Value
 *     }
 * }
 * <p/>
 * To force an individual enum to be serialized using the toString representation of each value:
  * {@code
  *     import com.novus.salat.annotations._
  *
  *     @EnumAs(strategy = EnumStrategy.BY_VALUE)
  *     object BabyAnimalsByValue extends Enumeration {
  *       val Kitten, Puppy, Bunny, Cub, Fawn, Duckling, Calf = Value
  *     }
  * }
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumAs {
    /**
     * @return enum serialization strategy (default handling is by value)
     */
    EnumStrategy strategy() default EnumStrategy.BY_VALUE;
}
