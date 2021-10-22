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

import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.MorphingSupport;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ShellInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import java.util.List;

/**
 * Tests for {@link MorphingSupport}.
 *
 * @author sablin_aa
 */
public class MorphingSupportTest extends AbstractCoreTest {
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
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validate() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.widgets.*;",
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <morphTargets>",
            "    <morphTarget class='org.eclipse.swt.widgets.Label'/>",
            "  </morphTargets>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ShellInfo shell =
        parse(
            "// filler filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComposite/>",
            "</Shell>");
    XmlObjectInfo myComposite;
    {
      List<ControlInfo> children = shell.getChildrenControls();
      assertEquals(1, children.size());
      myComposite = children.get(0);
    }
    // validate
    MorphingTargetDescription morphingTarget;
    {
      List<MorphingTargetDescription> morphingTargets =
          myComposite.getDescription().getMorphingTargets();
      assertEquals(1, morphingTargets.size());
      morphingTarget = morphingTargets.get(0);
      assertEquals("org.eclipse.swt.widgets.Label", morphingTarget.getComponentClass().getName());
    }
    String message = validate(myComposite, morphingTarget);
    assertNull(message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_morph_removeProperties() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "  private String value;",
        "  public String getTest() {",
        "    return value;",
        "  }",
        "  public void setTest(String value) {",
        "    this.value = value;",
        "  }"}, new String[]{
        "  <morphTargets>",
        "    <morphTarget class='org.eclipse.swt.widgets.Label'/>",
        "  </morphTargets>"});
    // parse
    ShellInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='123'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo myComponent;
    {
      List<ControlInfo> children = shell.getChildrenControls();
      assertEquals(1, children.size());
      myComponent = children.get(0);
      Property property = myComponent.getPropertyByTitle("test");
      assertNotNull(property);
      assertEquals("123", property.getValue());
    }
    // morphing
    MorphingTargetDescription morphingTarget;
    {
      List<MorphingTargetDescription> morphingTargets =
          myComponent.getDescription().getMorphingTargets();
      assertEquals(1, morphingTargets.size());
      morphingTarget = morphingTargets.get(0);
      assertEquals("org.eclipse.swt.widgets.Label", morphingTarget.getComponentClass().getName());
    }
    morph(myComponent, morphingTarget);
    // check
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Label wbp:name='component'/>",
        "</Shell>");
    {
      List<ControlInfo> children = shell.getChildrenControls();
      myComponent = children.get(0);
      assertEquals(
          "org.eclipse.swt.widgets.Label",
          myComponent.getDescription().getComponentClass().getName());
      Property property = myComponent.getPropertyByTitle("test");
      assertNull(property);
    }
  }

  /**
   * During morphing we move children from source to parent.
   */
  public void test_morph_keepChildren() throws Exception {
    prepareMyComponent(new String[]{}, new String[]{
        "  <morphTargets>",
        "    <morphTarget class='org.eclipse.swt.custom.ScrolledComposite'/>",
        "  </morphTargets>"});
    // parse
    ShellInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component'>",
            "    <Button text='button'/>",
            "  </t:MyComponent>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo myComponent;
    {
      List<ControlInfo> children = shell.getChildrenControls();
      assertEquals(1, children.size());
      myComponent = children.get(0);
      {
        List<ControlInfo> componentChildren = myComponent.getChildren(ControlInfo.class);
        assertEquals(1, componentChildren.size());
        assertEquals("button", componentChildren.get(0).getPropertyByTitle("text").getValue());
      }
    }
    // morphing
    MorphingTargetDescription morphingTarget;
    {
      List<MorphingTargetDescription> morphingTargets =
          myComponent.getDescription().getMorphingTargets();
      assertEquals(1, morphingTargets.size());
      morphingTarget = morphingTargets.get(0);
      assertEquals(
          "org.eclipse.swt.custom.ScrolledComposite",
          morphingTarget.getComponentClass().getName());
    }
    morph(myComponent, morphingTarget);
    // check
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <ScrolledComposite wbp:name='component'>",
        "    <Button text='button'/>",
        "  </ScrolledComposite>",
        "</Shell>");
    {
      List<ControlInfo> children = shell.getChildrenControls();
      assertEquals(1, children.size());
      myComponent = children.get(0);
      assertEquals(
          "org.eclipse.swt.custom.ScrolledComposite",
          myComponent.getDescription().getComponentClass().getName());
      {
        List<ControlInfo> componentChildren = myComponent.getChildren(ControlInfo.class);
        assertEquals(1, componentChildren.size());
        assertEquals("button", componentChildren.get(0).getPropertyByTitle("text").getValue());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs morphing of {@link XmlObjectInfo} into given target.
   */
  public static String validate(XmlObjectInfo objectInfo, MorphingTargetDescription target)
      throws Exception {
    return MorphingSupport.validate("org.eclipse.swt.widgets.Control", objectInfo, target);
  }

  /**
   * Performs morphing of {@link XmlObjectInfo} into given target.
   */
  public static void morph(XmlObjectInfo objectInfo, MorphingTargetDescription target)
      throws Exception {
    assertNull(validate(objectInfo, target));
    MorphingSupport.morph("org.eclipse.swt.widgets.Control", objectInfo, target);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "Morph" sub-menu is contributed during broadcast.
   */
  /* TODO public void test_actions() throws Exception {
  }*/
  /**
   * Thoroughly test one action from "Morph" sub-menu.
   */
  /* TODO public void test_actions_run() throws Exception {
  }*/
}