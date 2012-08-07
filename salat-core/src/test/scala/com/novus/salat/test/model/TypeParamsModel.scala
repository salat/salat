package com.novus.salat.test.model

case class CC(val field: CharSequence)

case class CCwithTypePar[T](val typeParamField: CCwithTypeParNest[T])

case class CCwithTypeParNest[T](val typeParamField: T)

case class CCwithCovarTP[+T](val typeParamField: CCwithCovarTPNest[T])

case class CCwithCovarTPNest[+T](val typeParamField: T)

case class CCwithCTPAndExistentialField[+T](val typeParamField: CCwithCTPAndExistentialFieldNest[_ <: T])

case class CCwithCTPAndExistentialFieldNest[+T](val typeParamField: T)
