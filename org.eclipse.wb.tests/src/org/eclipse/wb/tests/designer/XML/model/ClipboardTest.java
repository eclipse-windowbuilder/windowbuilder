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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardObjectProperty;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMementoTransfer;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;

import java.util.List;

/**
 * Test for {@link XmlObjectMemento} and {@link ClipboardCommand}.
 *
 * @author scheglov_ke
 */
public class ClipboardTest extends AbstractCoreTest {
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
   * Test for {@link XmlObjectMemento#hasMemento(XmlObjectInfo)}.
   */
  public void test_hasMemento() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    ControlInfo button = getObjectByName("button");
    //
    assertFalse(XmlObjectMemento.hasMemento(shell));
    assertTrue(XmlObjectMemento.hasMemento(button));
  }

  public void test_forControl() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare memento
    ComponentInfoMemento memento = (ComponentInfoMemento) XmlObjectMemento.createMemento(button);
    assertEquals("org.eclipse.swt.widgets.Button", memento.getComponentClassName());
    // has image
    Image mementoImage;
    {
      mementoImage = memento.getImage();
      assertNotNull(mementoImage);
      assertFalse(mementoImage.isDisposed());
      assertEquals(button.getBounds().width, mementoImage.getBounds().width);
      assertEquals(button.getBounds().height, mementoImage.getBounds().height);
    }
    // create new Button
    ControlInfo newButton = (ControlInfo) memento.create(shell);
    assertSame(mementoImage, ComponentInfoMemento.getImage(newButton));
    // do create
    shell.getLayout().command_CREATE(newButton, null);
    memento.apply();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='My button'/>",
        "  <Button text='My button'/>",
        "</Shell>");
    // after apply() image is disposed
    assertTrue(mementoImage.isDisposed());
    assertSame(null, ComponentInfoMemento.getImage(newButton));
  }

  public void test_forControl_hasBounds() throws Exception {
    CompositeInfo composite =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Button wbp:name='button' text='My button' bounds='10, 20, 100, 50'/>",
            "</Composite>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare memento
    ComponentInfoMemento memento = (ComponentInfoMemento) XmlObjectMemento.createMemento(button);
    assertEquals("org.eclipse.swt.widgets.Button", memento.getComponentClassName());
    // create new Button
    ControlInfo newButton = (ControlInfo) memento.create(composite);
    // has bounds
    assertEquals(new Rectangle(10, 20, 100, 50), newButton.getBounds());
    assertEquals(new Rectangle(10, 20, 100, 50), newButton.getModelBounds());
  }

  public void test_forControl_includeStyle() throws Exception {
    final CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button' x:Style='CHECK'/>",
            "</Shell>");
    refresh();
    // do copy/paste
    {
      ControlInfo button = getObjectByName("button");
      doCopyPaste(button, new PasteProcedure<ControlInfo>() {
        public void run(ControlInfo copy) throws Exception {
          shell.getLayout().command_CREATE(copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='My button' x:Style='CHECK'/>",
        "  <Button text='My button' x:Style='CHECK'/>",
        "</Shell>");
  }

  /**
   * {@link Button} on "null" layout, so no image.
   */
  public void test_noImage() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='My button'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // no image in "button"
    assertNull(button.getImage());
    // ...so, no image in memento
    ComponentInfoMemento memento = (ComponentInfoMemento) XmlObjectMemento.createMemento(button);
    assertNull(memento.getImage());
  }

  /**
   * Test for using {@link XmlObjectMemento#apply(XmlObjectInfo)}.
   */
  public void test_useStaticApply() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // create new Button
    ControlInfo newButton;
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(button);
      newButton = (ControlInfo) memento.create(shell);
    }
    // add new Button
    shell.getLayout().command_CREATE(newButton, null);
    XmlObjectMemento.apply(newButton);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='My button'/>",
        "  <Button text='My button'/>",
        "</Shell>");
  }

  /**
   * Test for using {@link XmlObjectMemento#isApplying(XmlObjectInfo)}.
   */
  public void test_isApplying() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // no
    assertFalse(XmlObjectMemento.isApplying(button));
    // create new Button
    ControlInfo newButton;
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(button);
      newButton = (ControlInfo) memento.create(shell);
    }
    // yes
    assertTrue(XmlObjectMemento.isApplying(newButton));
  }

  public void test_forLayout() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite_1'>",
            "    <Composite.layout>",
            "      <RowLayout fill='true'/>",
            "    </Composite.layout>",
            "  </Composite>",
            "  <Composite wbp:name='composite_2'/>",
            "</Shell>");
    refresh();
    CompositeInfo composite_1 = getObjectByName("composite_1");
    CompositeInfo composite_2 = getObjectByName("composite_2");
    // copy layout
    LayoutInfo newLayout;
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(composite_1.getLayout());
      newLayout = (LayoutInfo) memento.create(shell);
    }
    // set new layout
    composite_2.setLayout(newLayout);
    XmlObjectMemento.apply(newLayout);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite_1'>",
        "    <Composite.layout>",
        "      <RowLayout fill='true'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Composite wbp:name='composite_2'>",
        "    <Composite.layout>",
        "      <RowLayout fill='true'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "</Shell>");
  }

  /**
   * Test for {@link IClipboardObjectProperty}.
   */
  public void test_IClipboardObjectProperty() throws Exception {
    final CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // contribute "foo" property
    class FooProperty extends XmlProperty implements IClipboardObjectProperty {
      public FooProperty(XmlObjectInfo object) {
        super(object, "foo", BooleanPropertyEditor.INSTANCE);
      }

      @Override
      public boolean isModified() throws Exception {
        return true;
      }

      @Override
      public Object getValue() throws Exception {
        return false;
      }

      @Override
      public void setValue(Object value) throws Exception {
      }

      public Object getClipboardObject() throws Exception {
        return Boolean.FALSE;
      }

      public void setClipboardObject(Object value) throws Exception {
        m_object.getPropertyByTitle("enabled").setValue(value);
      }
    }
    shell.addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object.getDescription().getComponentClass() == Button.class) {
          properties.add(new FooProperty(object));
        }
      }
    });
    // do copy/paste
    doCopyPaste(button, new PasteProcedure<ControlInfo>() {
      public void run(ControlInfo copy) throws Exception {
        shell.getLayout().command_CREATE(copy, null);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Button enabled='false'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XMLObjectMementoTransfer
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_transfer() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // invoke just to cover
    {
      assertNotNull(ReflectionUtils.invokeMethod(
          XmlObjectMementoTransfer.getInstance(),
          "getTypeNames()"));
    }
    // copy to Clipboard
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(button);
      Clipboard clipboard = new Clipboard(Display.getCurrent());
      try {
        clipboard.setContents(
            new Object[]{new XmlObjectMemento[]{memento}},
            new Transfer[]{XmlObjectMementoTransfer.getInstance()});
      } finally {
        clipboard.dispose();
      }
    }
    // get from Clipboard
    {
      XmlObjectMemento[] mementos;
      {
        Clipboard clipboard = new Clipboard(Display.getCurrent());
        try {
          mementos =
              (XmlObjectMemento[]) clipboard.getContents(XmlObjectMementoTransfer.getInstance());
        } finally {
          clipboard.dispose();
        }
      }
      // use to create new XMLObject_Info
      assertEquals(1, mementos.length);
      XmlObjectInfo newObject = mementos[0].create(shell);
      assertInstanceOf(ControlInfo.class, newObject);
    }
    // set text and try to ask XMLObjectMementoTransfer
    {
      Clipboard clipboard = new Clipboard(Display.getCurrent());
      try {
        clipboard.setContents(new Object[]{"some text"}, new Transfer[]{TextTransfer.getInstance()});
        assertNull(clipboard.getContents(XmlObjectMementoTransfer.getInstance()));
        // cover unsupported type
        assertNull(XmlObjectMementoTransfer.getInstance().nativeToJava(null));
      } finally {
        clipboard.dispose();
      }
    }
  }
}