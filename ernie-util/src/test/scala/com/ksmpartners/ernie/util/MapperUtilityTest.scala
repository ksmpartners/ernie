/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import org.testng.annotations.Test
import org.testng.Assert

class MapperUtilityTest {

  @Test
  def canUseMapperUtility() {
    val list = MapperUtility.mapper.readValue("""[1,2,3]""", classOf[java.util.List[Int]])
    Assert.assertEquals(list.get(0), 1)
    Assert.assertEquals(list.get(1), 2)
    Assert.assertEquals(list.get(2), 3)
  }

}
