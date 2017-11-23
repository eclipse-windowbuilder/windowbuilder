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

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

/**
 * Test for {@link DesignerException}.
 * 
 * @author scheglov_ke
 */
public class DesignerExceptionTest extends TestCase {
  private static final int CODE = 12345;
  private static final String PARAMETER_0 = "a";
  private static final String PARAMETER_1 = "b";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_1() {
    DesignerException designerException = new DesignerException(CODE, PARAMETER_0);
    assertEquals(CODE, designerException.getCode());
    {
      String[] parameters = designerException.getParameters();
      assertThat(parameters).hasSize(1);
      assertSame(PARAMETER_0, parameters[0]);
    }
  }

  public void test_2() {
    DesignerException designerException = new DesignerException(CODE, PARAMETER_0, PARAMETER_1);
    assertEquals(CODE, designerException.getCode());
    {
      String[] parameters = designerException.getParameters();
      assertThat(parameters).hasSize(2);
      assertSame(PARAMETER_0, parameters[0]);
      assertSame(PARAMETER_1, parameters[1]);
    }
  }

  public void test_all() {
    Throwable cause = new Exception();
    DesignerException designerException = new DesignerException(CODE, cause, PARAMETER_0);
    assertEquals(CODE, designerException.getCode());
    assertSame(cause, designerException.getCause());
    {
      String[] parameters = designerException.getParameters();
      assertThat(parameters).hasSize(1);
      assertSame(PARAMETER_0, parameters[0]);
    }
  }

  /**
   * Test for {@link DesignerException#setSourcePosition(int)}.
   */
  public void test_setSourcePosition() {
    DesignerException designerException = new DesignerException(CODE);
    // initially no position
    assertEquals(-1, designerException.getSourcePosition());
    // set position
    int expectedPosition = 5;
    designerException.setSourcePosition(expectedPosition);
    assertEquals(expectedPosition, designerException.getSourcePosition());
  }

  /**
   * Just load {@link ICoreExceptionConstants} class to allow coverage to know that it is interface
   * and should be ignored.
   */
  public void test_ICoreExceptionConstants() throws Exception {
    Field wrapperField = ICoreExceptionConstants.class.getField("UNEXPECTED");
    assertEquals(1, wrapperField.getInt(null));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMessage()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMessage_useTitle_knownCode() {
    DesignerException designerException = new DesignerException(ICoreExceptionConstants.UNEXPECTED);
    assertEquals("1 (Internal Error).", designerException.getMessage());
  }

  public void test_getMessage_useTitle_knownCode_withParameters() {
    DesignerException designerException =
        new DesignerException(ICoreExceptionConstants.UNEXPECTED, "A", "BB");
    assertEquals("1 (Internal Error). A BB", designerException.getMessage());
  }

  public void test_getMessage_useTitle_unknownCode() {
    DesignerException designerException = new DesignerException(-1000);
    assertEquals("-1000 (No description).", designerException.getMessage());
  }
}
