package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun voiceGangId_formatValidation() {
    val random = SecureRandom()
    val id = (10000000 + random.nextInt(90000000)).toString()
    assertEquals(8, id.length)
    assertTrue(id.all { it.isDigit() })
    assertTrue(id.toLong() in 10000000..99999999)
  }
}
