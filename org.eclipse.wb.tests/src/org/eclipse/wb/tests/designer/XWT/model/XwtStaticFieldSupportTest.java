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
package org.eclipse.wb.tests.designer.XWT.model;

import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditorGetExpression;
import org.eclipse.wb.internal.xwt.model.util.XwtStaticFieldSupport;

import org.eclipse.swt.SWT;

import javax.swing.SwingConstants;

/**
 * Test for {@link XwtStaticFieldSupport}.
 *
 * @author scheglov_ke
 */
public class XwtStaticFieldSupportTest extends XwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No class for field from {@link SWT}.
   */
  public void test_SWT() throws Exception {
    parse("<Shell/>");
    //
    String fieldName = "LEFT";
    String[] expression = {null};
    m_lastObject.getBroadcast(StaticFieldPropertyEditorGetExpression.class).invoke(
        SWT.class,
        fieldName,
        expression);
    assertEquals("LEFT", expression[0]);
  }

  /**
   * Not {@link SWT}, so use fully qualified class name.
   */
  public void test_customStyle() throws Exception {
    parse("<Shell/>");
    //
    String fieldName = "LEFT";
    String[] expression = {null};
    m_lastObject.getBroadcast(StaticFieldPropertyEditorGetExpression.class).invoke(
        SwingConstants.class,
        fieldName,
        expression);
    assertEquals("(javax.swing.SwingConstants).LEFT", expression[0]);
  }
}