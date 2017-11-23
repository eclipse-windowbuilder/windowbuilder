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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormAttachmentInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link FormLayoutInfo}.
 * 
 * @author mitin_aa
 */
public class FormLayoutTest extends XwtModelTest {
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
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_attachmentPropertiesExist() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='100' offset='50'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(buttonInfo);
    {
      Property[] properties = layoutData.getProperties();
      String[] propertyTitles = PropertyUtils.getTitles(properties);
      assertThat(propertyTitles).contains("left", "right", "top", "bottom");
    }
  }

  public void test_deleteAttachment_using_property() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button text='button'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(buttonInfo);
    // remove
    Property attachmentProperty = PropertyUtils.getByPath(layoutData, "left");
    attachmentProperty.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button text='button'/>",
        "</Shell>");
  }

  public void test_FormData_width_height() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'/>",
            "</Shell>");
    shell.refresh();
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(buttonInfo);
    FormDataInfo formDataInfo = (FormDataInfo) layoutData;
    assertNotNull(formDataInfo.getPropertyByTitle("width"));
    assertNotNull(formDataInfo.getPropertyByTitle("height"));
    formDataInfo.setWidth(150);
    formDataInfo.setHeight(50);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'>",
        "    <Button.layoutData>",
        "      <FormData width='150' height='50'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_attachmentsExist() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='100' offset='50'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(buttonInfo);
    assertInstanceOf(FormDataInfo.class, layoutData);
    FormDataInfo formDataInfo = (FormDataInfo) layoutData;
    assertNotNull(formDataInfo.getAttachment(IPositionConstants.LEFT));
    assertNotNull(formDataInfo.getAttachment(IPositionConstants.RIGHT));
    assertNotNull(formDataInfo.getAttachment(IPositionConstants.TOP));
    assertNotNull(formDataInfo.getAttachment(IPositionConstants.BOTTOM));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control attachments
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_change_to_control_below() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout />",
            "  </Shell.layout>",
            "  <Button x:Name='button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='50'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "  <Button text='button2'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='150'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // tests
    List<ControlInfo> childrenControls = shell.getChildrenControls();
    ControlInfo button1 = childrenControls.get(0);
    ControlInfo button2 = childrenControls.get(1);
    FormDataInfo layoutData = (FormDataInfo) LayoutInfo.getLayoutData(button1);
    FormAttachmentInfo leftAttachment = layoutData.getAttachment(IPositionConstants.LEFT);
    ((GenericPropertyImpl) getPropertyByTitle(leftAttachment, "control")).setExpression("", button2);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout />",
        "  </Shell.layout>",
        "  <Button x:Name='button1' text='button1'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment control='{Binding ElementName=button}'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='button2' x:Name='button'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='150'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_change_to_control() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout />",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='50'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "  <Button text='button2'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='150'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // tests
    List<ControlInfo> childrenControls = shell.getChildrenControls();
    ControlInfo button1 = childrenControls.get(0);
    ControlInfo button2 = childrenControls.get(1);
    FormDataInfo layoutData = (FormDataInfo) LayoutInfo.getLayoutData(button2);
    FormAttachmentInfo leftAttachment = layoutData.getAttachment(IPositionConstants.LEFT);
    ((GenericPropertyImpl) getPropertyByTitle(leftAttachment, "control")).setExpression("", button1);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout />",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='50'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='button2'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment control='{Binding ElementName=Button1}'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_parse_control_attachment() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout />",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='10' offset='0'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "  <Button text='button2'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment control='{Binding ElementName=Button1}' offset='10'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // tests
    List<ControlInfo> childrenControls = shell.getChildrenControls();
    assertThat(childrenControls).hasSize(2);
    ControlInfo button1 = childrenControls.get(0);
    ControlInfo button2 = childrenControls.get(1);
    FormDataInfo layoutData = (FormDataInfo) LayoutInfo.getLayoutData(button2);
    FormAttachmentInfo leftAttachment = layoutData.getAttachment(IPositionConstants.LEFT);
    assertThat(leftAttachment).isNotNull();
    assertThat(leftAttachment.getControl()).isSameAs(button1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout managing tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_delete() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    assertHierarchy(
        "<Shell>",
        "  <FormLayout>",
        "  <Button x:Name='Button1' text='button1'>",
        "    <FormData>",
        "      (0, 100)",
        "      (none)",
        "      (none)",
        "      (none)");
    // get layout and button
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    // delete
    buttonInfo.delete();
    // test
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_deleteAttachment() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    // detach
    ((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).detach(
        buttonInfo,
        IPositionConstants.LEFT);
    // test
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'/>",
        "</Shell>");
  }

  public void test_deleteSingleAttachment() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "        <FormData.top>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.top>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    // detach
    ((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).detach(
        buttonInfo,
        IPositionConstants.LEFT);
    // test
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='100'/>",
        "        </FormData.top>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_deleteAttachmentAndAttach() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    // detach
    ((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).detach(
        buttonInfo,
        IPositionConstants.LEFT);
    ((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).attachAbsolute(
        buttonInfo,
        IPositionConstants.LEFT,
        10);
    // test
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='10'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_FormAttachment_use_three_properties() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button text='button'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='50'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    // get layout and button
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    FormDataInfo formDataInfo = (FormDataInfo) LayoutInfo.getLayoutData(buttonInfo);
    FormAttachmentInfo attachment = formDataInfo.getAttachment(IPositionConstants.LEFT);
    attachment.setDenominator(15);
    attachment.setNumerator(90);
    attachment.setOffset(100);
    attachment.write();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button text='button'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.left>",
        "          <FormAttachment numerator='90' offset='100' denominator='15'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_setLayout() throws Exception {
    CompositeInfo shell = parse("<Shell/>");
    setFormLayout(
        shell,
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_changeFromGridEmpty() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "</Shell>");
    shell.refresh();
    setFormLayout(
        shell,
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_changeFromGridWithData() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'>",
            "    <Button.layoutData>",
            "      <GridData widthHint='200'/>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    ControlInfo button = getObjectByName("button");
    //
    int expectedRight = button.getModelBounds().right();
    setFormLayout(
        shell,
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.right>",
        "          <FormAttachment numerator='0' offset='" + expectedRight + "'/>",
        "        </FormData.right>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='5'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='5'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_changeFromAbsolute() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell layout='{x:Null}'>",
            "  <Button wbp:name='button' bounds='5, 5, 100, 30'/>",
            "</Shell>");
    shell.refresh();
    setFormLayout(
        shell,
        "<Shell>",
        "  <Shell.layout>",
        "    <FormLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <FormData>",
        "        <FormData.bottom>",
        "          <FormAttachment numerator='0' offset='35'/>",
        "        </FormData.bottom>",
        "        <FormData.right>",
        "          <FormAttachment numerator='0' offset='105'/>",
        "        </FormData.right>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='5'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='5'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_changeToGridWithData() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FormLayout/>",
            "  </Shell.layout>",
            "  <Button x:Name='Button1' text='button1'>",
            "    <Button.layoutData>",
            "      <FormData>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='100'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </Button.layoutData>",
            "  </Button>",
            "</Shell>");
    shell.refresh();
    setGridLayout(
        shell,
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button x:Name='Button1' text='button1'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test for copy/paste.
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    CompositeInfo composite =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Composite>",
            "    <Composite.layout>",
            "      <FormLayout/>",
            "    </Composite.layout>",
            "    <Button wbp:name='button1' text='button1'>",
            "      <Button.layoutData>",
            "        <FormData>",
            "          <FormData.left>",
            "            <FormAttachment numerator='0' offset='100'/>",
            "          </FormData.left>",
            "        </FormData>",
            "      </Button.layoutData>",
            "    </Button>",
            "    <Button wbp:name='button2' text='button2'>",
            "      <Button.layoutData>",
            "        <FormData>",
            "          <FormData.right>",
            "            <FormAttachment numerator='100' offset='-10'/>",
            "          </FormData.right>",
            "        </FormData>",
            "      </Button.layoutData>",
            "    </Button>",
            "  </Composite>",
            "</Shell>");
    composite.refresh();
    // prepare memento
    XmlObjectMemento memento;
    {
      ControlInfo inner = composite.getChildrenControls().get(0);
      memento = XmlObjectMemento.createMemento(inner);
    }
    // add copy
    ControlInfo copy = (ControlInfo) memento.create(composite);
    composite.getLayout().command_CREATE(copy, null);
    memento.apply();
    // test
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button1' text='button1'>",
        "      <Button.layoutData>",
        "        <FormData>",
        "          <FormData.left>",
        "            <FormAttachment numerator='0' offset='100'/>",
        "          </FormData.left>",
        "        </FormData>",
        "      </Button.layoutData>",
        "    </Button>",
        "    <Button wbp:name='button2' text='button2'>",
        "      <Button.layoutData>",
        "        <FormData>",
        "          <FormData.right>",
        "            <FormAttachment numerator='100' offset='-10'/>",
        "          </FormData.right>",
        "        </FormData>",
        "      </Button.layoutData>",
        "    </Button>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "    <Button text='button1'>",
        "      <Button.layoutData>",
        "        <FormData>",
        "          <FormData.left>",
        "            <FormAttachment numerator='0' offset='100'/>",
        "          </FormData.left>",
        "        </FormData>",
        "      </Button.layoutData>",
        "    </Button>",
        "    <Button text='button2'>",
        "      <Button.layoutData>",
        "        <FormData>",
        "          <FormData.right>",
        "            <FormAttachment numerator='100' offset='-10'/>",
        "          </FormData.right>",
        "        </FormData>",
        "      </Button.layoutData>",
        "    </Button>",
        "  </Composite>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property getPropertyByTitle(ObjectInfo info, String title) throws Exception {
    Property[] properties = info.getProperties();
    for (Property property : properties) {
      if (title.equals(property.getTitle())) {
        return property;
      }
    }
    return null;
  }

  /**
   * Sets the {@link FormLayout} for given {@link CompositeInfo}.
   */
  private void setFormLayout(CompositeInfo composite, String... expectedLines) throws Exception {
    composite.getRoot().refresh();
    // set FormLayout
    FormLayoutInfo layout = createObject("org.eclipse.swt.layout.FormLayout");
    composite.setLayout(layout);
    // check source
    assertXML(expectedLines);
  }

  /**
   * Sets the {@link GridLayout} for given {@link CompositeInfo}.
   */
  private void setGridLayout(CompositeInfo composite, String... expectedLines) throws Exception {
    composite.getRoot().refresh();
    // set GridLayout
    GridLayoutInfo layout = createObject("org.eclipse.swt.layout.GridLayout");
    composite.setLayout(layout);
    // check source
    assertXML(expectedLines);
  }
}