/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.utils.Version;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link Version}.
 * 
 * @author sablin_aa
 */
public class VersionTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // String
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Version#getStringMajorMinor()}.
   */
  public void test_getStringMajorMinor() {
    // no micro
    {
      Version version = new Version(3, 5);
      assertEquals("3.5", version.getStringMajorMinor());
    }
    // has micro
    {
      Version version = new Version(3, 5, 2);
      assertEquals("3.5", version.getStringMajorMinor());
    }
  }

  /**
   * Test for {@link Version#getStringMajorMinorMicro()}.
   */
  public void test_getStringMajorMinorMicro() {
    // no micro
    {
      Version version = new Version(3, 5);
      assertEquals("3.5.0", version.getStringMajorMinorMicro());
    }
    // has micro
    {
      Version version = new Version(3, 5, 2);
      assertEquals("3.5.2", version.getStringMajorMinorMicro());
    }
  }

  /**
   * Test for {@link Version#toString()}.
   */
  public void test_toString() {
    // no micro
    {
      Version version = new Version(3, 5);
      assertEquals("3.5.0", version.toString());
    }
    // has micro
    {
      Version version = new Version(3, 5, 2);
      assertEquals("3.5.2", version.toString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Version#equals(Object)}.
   */
  public void test_equals() {
    final Version version = new Version(3, 5);
    assertTrue(version.equals(version));
    assertFalse(version.equals(this));
    assertTrue(version.equals(new Version(3, 5)));
    assertFalse(version.equals(new Version(3, 5, 7)));
  }

  /**
   * Test for {@link Version#hashCode()}.
   */
  public void test_hashCode() {
    final Version version_1 = new Version(3, 5);
    final Version version_2 = new Version(3, 5);
    final Version version_3 = new Version(3, 6);
    assertThat(version_1.hashCode()).isEqualTo(version_2.hashCode());
    assertThat(version_1.hashCode()).isNotEqualTo(version_3.hashCode());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Version#getMajor()}.
   */
  public void test_getMajor() {
    {
      final Version version = new Version(3, 5);
      assertEquals(3, version.getMajor());
    }
    {
      final Version version = new Version(2, 1, 3);
      assertEquals(2, version.getMajor());
    }
  }

  /**
   * Test for {@link Version#getMinor()}.
   */
  public void test_getMinor() {
    {
      final Version version = new Version(3, 5);
      assertEquals(5, version.getMinor());
    }
    {
      final Version version = new Version(2, 1, 3);
      assertEquals(1, version.getMinor());
    }
  }

  /**
   * Test for {@link Version#getMicro()}.
   */
  public void test_getMicro() {
    {
      final Version version = new Version(3, 5);
      assertEquals(0, version.getMicro());
    }
    {
      final Version version = new Version(2, 1, 3);
      assertEquals(3, version.getMicro());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compare
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Version#isSame(Version)}.
   */
  public void test_isSame() {
    final Version version = new Version(3, 5, 1);
    assertTrue(version.isSame(new Version(3, 5, 1)));
    assertFalse(version.isSame(new Version(3, 5)));
    assertFalse(version.isSame(new Version(2, 5, 7)));
  }

  /**
   * Test for {@link Version#isHigher(Version)}.
   */
  public void test_isHigher() {
    final Version version = new Version(3, 5, 2);
    assertTrue(version.isHigher(new Version(2, 7, 15)));
    assertTrue(version.isHigher(new Version(3, 4, 8)));
    assertTrue(version.isHigher(new Version(3, 5)));
    assertTrue(version.isHigher(new Version(3, 5, 1)));
    assertFalse(version.isHigher(new Version(3, 5, 2)));
    assertFalse(version.isHigher(new Version(3, 5, 3)));
    assertFalse(version.isHigher(new Version(4, 1, 2)));
  }

  /**
   * Test for {@link Version#isHigherOrSame(Version)}.
   */
  public void test_isHigherOrSame() {
    final Version version = new Version(3, 5, 2);
    assertTrue(version.isHigherOrSame(new Version(2, 7, 15)));
    assertTrue(version.isHigherOrSame(new Version(3, 4, 8)));
    assertTrue(version.isHigherOrSame(new Version(3, 5)));
    assertTrue(version.isHigherOrSame(new Version(3, 5, 1)));
    assertTrue(version.isHigherOrSame(new Version(3, 5, 2)));
    assertFalse(version.isHigherOrSame(new Version(3, 5, 3)));
    assertFalse(version.isHigherOrSame(new Version(4, 1, 2)));
  }

  /**
   * Test for {@link Version#isLower(Version)}.
   */
  public void test_isLower() {
    final Version version = new Version(3, 5, 2);
    assertFalse(version.isLower(new Version(2, 7, 15)));
    assertFalse(version.isLower(new Version(3, 4, 8)));
    assertFalse(version.isLower(new Version(3, 5)));
    assertFalse(version.isLower(new Version(3, 5, 1)));
    assertFalse(version.isLower(new Version(3, 5, 2)));
    assertTrue(version.isLower(new Version(3, 5, 3)));
    assertTrue(version.isLower(new Version(3, 6, 1)));
    assertTrue(version.isLower(new Version(4, 1)));
    assertTrue(version.isLower(new Version(4, 1, 2)));
  }

  /**
   * Test for {@link Version#isLowerOrSame(Version)}.
   */
  public void test_isLowerOrSame() {
    final Version version = new Version(3, 5, 2);
    assertFalse(version.isLowerOrSame(new Version(2, 7, 15)));
    assertFalse(version.isLowerOrSame(new Version(3, 4, 8)));
    assertFalse(version.isLowerOrSame(new Version(3, 5)));
    assertFalse(version.isLowerOrSame(new Version(3, 5, 1)));
    assertTrue(version.isLowerOrSame(new Version(3, 5, 2)));
    assertTrue(version.isLowerOrSame(new Version(3, 5, 3)));
    assertTrue(version.isLowerOrSame(new Version(4, 1)));
    assertTrue(version.isLowerOrSame(new Version(4, 1, 2)));
  }
}
