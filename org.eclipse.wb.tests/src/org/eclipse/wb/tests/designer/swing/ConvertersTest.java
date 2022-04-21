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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.swing.model.property.converter.ColorConverter;
import org.eclipse.wb.internal.swing.model.property.converter.DimensionConverter;
import org.eclipse.wb.internal.swing.model.property.converter.InsetsConverter;
import org.eclipse.wb.internal.swing.model.property.converter.PointConverter;
import org.eclipse.wb.internal.swing.model.property.converter.RectangleConverter;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.KeyValue;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import java.awt.EventQueue;
import java.awt.SystemColor;

import javax.swing.UIManager;

/**
 * Tests for Swing {@link ExpressionConverter}'s.
 *
 * @author scheglov_ke
 * @author lobas_av
 */
public class ConvertersTest extends DesignerTestCase {
  public void test_PointConverter() throws Exception {
    ExpressionConverter converter = PointConverter.INSTANCE;
    assertEquals("new java.awt.Point(1, 2)", converter.toJavaSource(null, new java.awt.Point(1, 2)));
    assertEquals("(java.awt.Point) null", converter.toJavaSource(null, null));
  }

  public void test_DimensionConverter() throws Exception {
    ExpressionConverter converter = DimensionConverter.INSTANCE;
    assertEquals(
        "new java.awt.Dimension(1, 2)",
        converter.toJavaSource(null, new java.awt.Dimension(1, 2)));
    assertEquals("(java.awt.Dimension) null", converter.toJavaSource(null, null));
  }

  public void test_InsetsConverter() throws Exception {
    ExpressionConverter converter = InsetsConverter.INSTANCE;
    assertEquals(
        "new java.awt.Insets(1, 2, 3, 4)",
        converter.toJavaSource(null, new java.awt.Insets(1, 2, 3, 4)));
    assertEquals("(java.awt.Insets) null", converter.toJavaSource(null, null));
  }

  public void test_RectangleConverter() throws Exception {
    ExpressionConverter converter = RectangleConverter.INSTANCE;
    assertEquals(
        "new java.awt.Rectangle(1, 2, 3, 4)",
        converter.toJavaSource(null, new java.awt.Rectangle(1, 2, 3, 4)));
    assertEquals("(java.awt.Rectangle) null", converter.toJavaSource(null, null));
  }

  public void test_ColorConverter() throws Exception {
    ExpressionConverter converter = ColorConverter.INSTANCE;
    assertEquals("(java.awt.Color) null", converter.toJavaSource(null, null));
    assertEquals("java.awt.Color.RED", converter.toJavaSource(null, java.awt.Color.red));
    assertEquals("java.awt.Color.RED", converter.toJavaSource(null, new java.awt.Color(255, 0, 0)));
    assertEquals(
        "new java.awt.Color(1, 2, 3, 4)",
        converter.toJavaSource(null, new java.awt.Color(1, 2, 3, 4)));
    //
    class SystemColorValue extends KeyValue<SystemColor> {
      public SystemColorValue(String _key, SystemColor _value) {
        super(_key, _value);
      }
    }
    assertEquals(
        Expectations.get("java.awt.SystemColor.textHighlight", new StrValue[]{
            new StrValue("kosta-home", "java.awt.SystemColor.textInactiveText"),
            new StrValue("scheglov-win", "java.awt.SystemColor.textHighlight")}),
        converter.toJavaSource(
            null,
            Expectations.get(java.awt.SystemColor.textHighlight, new SystemColorValue[]{
                new SystemColorValue("kosta-home", java.awt.SystemColor.textInactiveText),
                new SystemColorValue("scheglov-win", java.awt.SystemColor.textHighlight)})));
    //
    EventQueue.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    });
    assertEquals(
        "javax.swing.UIManager.getColor(\"Button.darkShadow\")",
        converter.toJavaSource(null, javax.swing.UIManager.getColor("Button.darkShadow")));
  }
}