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
package org.eclipse.wb.tests.designer.XWT.model.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;
import org.eclipse.wb.internal.xwt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.xwt.model.property.editor.color.ColorSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ColorPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ColorPropertyEditorTest extends XwtModelTest {
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
  // ColorSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColorSupport#toString(Color)}.
   */
  public void test_ColorSupport_toString() throws Exception {
    Color color = new Color(null, 1, 2, 3);
    assertEquals("1,2,3", ColorSupport.toString(color));
  }

  /**
   * Test for {@link ColorSupport#createInfo(Color)}.
   */
  public void test_ColorSupport_createInfo() throws Exception {
    Color color = new Color(null, 1, 2, 3);
    ColorInfo colorInfo = ColorSupport.createInfo(color);
    assertEquals("", colorInfo.getName());
    assertEquals("1, 2, 3", colorInfo.getCommaRGB());
  }

  /**
   * Test for {@link ColorSupport#getSystemColors()}.
   */
  public void test_ColorSupport_getSystemColors() throws Exception {
    ColorInfo[] colors = ColorSupport.getSystemColors();
    // check some names
    List<String> names = Lists.newArrayList();
    List<Object> datas = Lists.newArrayList();
    for (ColorInfo colorInfo : colors) {
      names.add(colorInfo.getName());
      datas.add(colorInfo.getName());
    }
    assertThat(names).contains("COLOR_RED", "COLOR_BLUE");
    assertThat(datas).contains("COLOR_RED", "COLOR_BLUE");
    // cached
    assertSame(colors, ColorSupport.getSystemColors());
  }

  /**
   * Test for {@link ColorSupport#getSystemNamedColors()}.
   */
  public void test_ColorSupport_getNamedColors() throws Exception {
    ColorInfo[] colors = ColorSupport.getNamedColors();
    // check some names
    List<String> names = Lists.newArrayList();
    List<Object> datas = Lists.newArrayList();
    for (ColorInfo colorInfo : colors) {
      names.add(colorInfo.getName());
      datas.add(colorInfo.getData());
    }
    assertThat(names).contains("Aqua", "SkyBlue");
    assertThat(datas).contains("Aqua", "SkyBlue");
    // cached
    assertSame(colors, ColorSupport.getNamedColors());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_GenericProperty_modified() throws Exception {
    CompositeInfo shell = parse("<Shell background='Red'/>");
    refresh();
    // prepare property
    Property property = shell.getPropertyByTitle("background");
    assertNotNull(property);
    assertInstanceOf(ColorPropertyEditor.class, property.getEditor());
    // check state
    assertTrue(property.isModified());
    assertEquals("Red", getPropertyText(property));
    assertEquals("Red", getPropertyClipboardSource(property));
  }

  public void test_getText_GenericProperty_notModified() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    refresh();
    // prepare property
    Property property = shell.getPropertyByTitle("background");
    assertNotNull(property);
    assertInstanceOf(ColorPropertyEditor.class, property.getEditor());
    // check state
    assertFalse(property.isModified());
    assertEquals("240,240,240", getPropertyText(property));
    assertEquals("240,240,240", getPropertyClipboardSource(property));
  }

  public void test_getText_notColorValue() throws Exception {
    Property property = new EmptyProperty(ColorPropertyEditor.INSTANCE);
    // check state
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // paint()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_paint() throws Exception {
    CompositeInfo shell = parse("<Shell background='Red'/>");
    refresh();
    // prepare property
    Property property = shell.getPropertyByTitle("background");
    PropertyEditor propertyEditor = property.getEditor();
    // just call it
    Image image = new Image(null, 200, 30);
    GC gc = new GC(image);
    try {
      ReflectionUtils.invokeMethod(
          propertyEditor,
          "paint(org.eclipse.wb.internal.core.model.property.Property,org.eclipse.swt.graphics.GC,int,int,int,int)",
          property,
          gc,
          0,
          0,
          200,
          30);
    } finally {
      gc.dispose();
      image.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // activate()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_activate_clickIgnored() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    refresh();
    // prepare property
    Property property = shell.getPropertyByTitle("background");
    ColorPropertyEditor propertyEditor = (ColorPropertyEditor) property.getEditor();
    // click ignored
    boolean activated = propertyEditor.activate(null, property, new Point(0, 0));
    assertFalse(activated);
  }

  public void test_active_asUsingKeyboard() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    refresh();
    // animate dialog
    final Property property = shell.getPropertyByTitle("background");
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        ColorPropertyEditor propertyEditor = (ColorPropertyEditor) property.getEditor();
        propertyEditor.activate(null, property, null);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Color chooser");
        context.clickButton("Cancel");
      }
    });
    assertXML("<Shell/>");
  }

  public void test_openDialog_usingMethod() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    refresh();
    // animate dialog
    final Property property = shell.getPropertyByTitle("background");
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Color chooser");
        {
          ColorsGridComposite colorsComposite = context.findFirstWidget(ColorsGridComposite.class);
          EventSender sender = new EventSender(colorsComposite);
          sender.moveTo(50, 130);
          sender.click();
        }
        context.clickButton("OK");
      }
    });
    assertXML("<Shell background='COLOR_RED'/>");
  }

  public void test_openDialog_usingPresentation() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    refresh();
    // animate dialog
    final Property property = shell.getPropertyByTitle("background");
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        PropertyEditorPresentation presentation = property.getEditor().getPresentation();
        ReflectionUtils.invokeMethod(
            presentation,
            "onClick(org.eclipse.wb.internal.core.model.property.table.PropertyTable,"
                + "org.eclipse.wb.internal.core.model.property.Property)",
            null,
            property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Color chooser");
        {
          ColorsGridComposite colorsComposite = context.findFirstWidget(ColorsGridComposite.class);
          EventSender sender = new EventSender(colorsComposite);
          sender.moveTo(50, 180);
          sender.click();
        }
        context.clickButton("OK");
      }
    });
    assertXML("<Shell background='COLOR_DARK_GREEN'/>");
  }
}