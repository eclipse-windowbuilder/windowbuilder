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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Tests for {@link MorphingTargetDescription}.
 * 
 * @author sablin_aa
 */
public class MorphingTargetDescriptionTest extends AbstractCoreTest {
  private static final String[] ESA = ArrayUtils.EMPTY_STRING_ARRAY;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for loading {@link MorphingTargetDescription}'s from "*.wbp-component.xml" files.
   */
  public void test_loadFromDescriptions() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <morphTargets>",
        "    <morphTarget class='org.eclipse.swt.widgets.Label'/>",
        "    <morphTarget class='org.eclipse.swt.widgets.Button' creationId='someId'/>",
        "  </morphTargets>"});
    ComponentDescription description = getMyDescription();
    List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
    // check targets
    assertEquals(2, morphingTargets.size());
    {
      MorphingTargetDescription morphingTarget = morphingTargets.get(0);
      assertEquals("org.eclipse.swt.widgets.Label", morphingTarget.getComponentClass().getName());
      assertNull(morphingTarget.getCreationId());
    }
    {
      MorphingTargetDescription morphingTarget = morphingTargets.get(1);
      assertEquals("org.eclipse.swt.widgets.Button", morphingTarget.getComponentClass().getName());
      assertEquals("someId", morphingTarget.getCreationId());
    }
  }

  /**
   * We should ignore invalid target classes.
   */
  public void test_noTargetClass() throws Exception {
    prepareMyComponent(ESA, new String[]{
        "  <morphTargets>",
        "    <morphTarget class='no.such.Class'/>",
        "    <morphTarget class='org.eclipse.swt.widgets.Label'/>",
        "  </morphTargets>"});
    ComponentDescription description = getMyDescription();
    List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
    // check targets
    assertThat(morphingTargets).hasSize(1);
    {
      MorphingTargetDescription morphingTarget = morphingTargets.get(0);
      assertEquals("org.eclipse.swt.widgets.Label", morphingTarget.getComponentClass().getName());
      assertNull(morphingTarget.getCreationId());
    }
  }

  /**
   * We should clear targets on "noInhetit=true"
   * 
   * @throws Exception
   */
  public void test_noInherit() throws Exception {
    // MyBaseButton
    {
      setFileContentSrc(
          "test/MyBaseComposite.java",
          getSourceDQ(
              "package test;",
              "import org.eclipse.swt.SWT;",
              "import org.eclipse.swt.widgets.*;",
              "public class MyBaseComposite extends Composite {",
              "  public MyBaseComposite(Composite parent, int style) {",
              "    super(parent, style);",
              "  }",
              "}"));
      setFileContentSrc(
          "test/MyBaseComposite.wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <morphTargets>",
              "    <morphTarget class='org.eclipse.swt.widgets.Label'/>",
              "  </morphTargets>",
              "</component>"));
    }
    // MyButton1
    {
      setFileContentSrc(
          "test/MyComposite1.java",
          getSourceDQ(
              "package test;",
              "import org.eclipse.swt.SWT;",
              "import org.eclipse.swt.widgets.*;",
              "public class MyComposite1 extends MyBaseComposite {",
              "  public MyComposite1(Composite parent, int style) {",
              "    super(parent, style);",
              "  }",
              "}"));
      setFileContentSrc(
          "test/MyComposite1.wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <morphTargets>",
              "    <morphTarget class='org.eclipse.swt.widgets.Button'/>",
              "  </morphTargets>",
              "</component>"));
    }
    // MyButton2
    {
      setFileContentSrc(
          "test/MyComposite2.java",
          getSourceDQ(
              "package test;",
              "import org.eclipse.swt.SWT;",
              "import org.eclipse.swt.widgets.*;",
              "public class MyComposite2 extends MyBaseComposite {",
              "  public MyComposite2(Composite parent, int style) {",
              "    super(parent, style);",
              "  }",
              "}"));
      setFileContentSrc(
          "test/MyComposite2.wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <morphTargets>",
              "    <noInherit/>",
              "    <morphTarget class='org.eclipse.swt.widgets.Text'/>",
              "  </morphTargets>",
              "</component>"));
    }
    waitForAutoBuild();
    // check description for MyButton1
    {
      ComponentDescription description = getDescription("test.MyComposite1");
      List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
      assertThat(morphingTargets).hasSize(2);
      // check targets
      {
        MorphingTargetDescription morphingTarget = morphingTargets.get(0);
        assertEquals("org.eclipse.swt.widgets.Label", morphingTarget.getComponentClass().getName());
        assertNull(morphingTarget.getCreationId());
      }
      {
        MorphingTargetDescription morphingTarget = morphingTargets.get(1);
        assertEquals("org.eclipse.swt.widgets.Button", morphingTarget.getComponentClass().getName());
        assertNull(morphingTarget.getCreationId());
      }
    }
    // check description for MyButton2
    {
      ComponentDescription description = getDescription("test.MyComposite2");
      List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
      // check targets
      assertThat(morphingTargets).hasSize(1); // no target JButton
      {
        MorphingTargetDescription morphingTarget = morphingTargets.get(0);
        assertEquals("org.eclipse.swt.widgets.Text", morphingTarget.getComponentClass().getName());
        assertNull(morphingTarget.getCreationId());
      }
    }
  }
}
