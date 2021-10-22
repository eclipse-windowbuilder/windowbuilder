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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.icon.IconPropertyEditor;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IFile;

/**
 * Test for {@link IconPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class IconPropertyEditorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_noIcon() throws Exception {
    assertIconPropertyText(null, new String[]{
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}"});
  }

  public void test_getText_null() throws Exception {
    assertIconPropertyText("(null)", new String[]{
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    button.setIcon(null);",
        "    add(button);",
        "  }",
        "}"});
  }

  public void test_getText_fromFile_1() throws Exception {
    IFile imageFile = TestUtils.createImagePNG(m_testProject, "1.png", 10, 10);
    try {
      String absoluteImagePath = imageFile.getLocation().toPortableString();
      assertIconPropertyText("File: " + absoluteImagePath, new String[]{
          "public class Test extends JPanel {",
          "  public Test() {",
          "    JButton button = new JButton();",
          "    button.setIcon(new ImageIcon(\"" + absoluteImagePath + "\"));",
          "    add(button);",
          "  }",
          "}"});
    } finally {
      imageFile.delete(true, null);
    }
  }

  public void test_getText_fromFile_2() throws Exception {
    IFile imageFile = TestUtils.createImagePNG(m_testProject, "1.png", 10, 10);
    try {
      String absoluteImagePath = imageFile.getLocation().toPortableString();
      assertIconPropertyText(
          "File: " + absoluteImagePath,
          new String[]{
              "public class Test extends JPanel {",
              "  public Test() {",
              "    JButton button = new JButton();",
              "    button.setIcon(new ImageIcon(\""
                  + absoluteImagePath
                  + "\", \"some description\"));",
              "    add(button);",
              "  }",
              "}"});
    } finally {
      imageFile.delete(true, null);
    }
  }

  public void test_getText_Class_getResource_1() throws Exception {
    assertIconPropertyText(
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setIcon(new ImageIcon(Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\")));",
            "    add(button);",
            "  }",
            "}"});
  }

  public void test_getText_Class_getResource_2() throws Exception {
    assertIconPropertyText(
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setIcon("
                + "      new ImageIcon(Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"), "
                + "      \"Some description\"));",
            "    add(button);",
            "  }",
            "}"});
  }

  public void test_getText_Class_getResource_3() throws Exception {
    assertIconPropertyText(
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    Icon icon = new ImageIcon(Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"));",
            "    button.setIcon(icon);",
            "    add(button);",
            "  }",
            "}"});
  }

  public void test_getText_Class_getResource_4() throws Exception {
    assertIconPropertyText(
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    java.net.URL url = Test.class.getResource(\"/javax/swing/plaf/basic/icons/JavaCup16.png\");",
            "    Icon icon = new ImageIcon(url);",
            "    button.setIcon(icon);",
            "    add(button);",
            "  }",
            "}"});
  }

  private void assertIconPropertyText(String expectedText, String[] lines) throws Exception {
    ContainerInfo panel = parseContainer(lines);
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // property
    Property iconProperty = button.getPropertyByTitle("icon");
    assertEquals(expectedText, getPropertyText(iconProperty));
  }
}
