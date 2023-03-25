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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.util.Map;

import javax.swing.JButton;

/**
 * Test for renaming {@link AbstractSimpleVariableSupport} on "text" property change.
 *
 * @author scheglov_ke
 */
public class TextPropertyRenameTest extends AbstractVariableTest {
  private static final ToolkitDescription TOOLKIT = ToolkitProvider.DESCRIPTION;

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
  // Auto rename
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getNameForText() throws Exception {
    // parse to have context for loading ComponentDescription
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "}",
        "}");
    //
    JavaInfo component =
        JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("javax.swing.JButton"),
            new ConstructorCreationSupport());
    assertEquals(
        "firstButton",
        invoke_getNameForText(component, "First", 3, "${text}${class_name}"));
    assertEquals(
        "btnFirst",
        invoke_getNameForText(component, "First", 3, "${class_acronym}${text}"));
    assertEquals(
        "veryLongTextButton",
        invoke_getNameForText(component, "Very long text for my JButton", 3, "${text}${class_name}"));
    assertEquals(
        "textCommaButton",
        invoke_getNameForText(
            component,
            "!!Text,  comma. \tdot... more~",
            2,
            "${text}${class_name}"));
    assertNull(invoke_getNameForText(component, "!..!", 3, "${text}${class_name}"));
  }

  private static String invoke_getNameForText(JavaInfo javaInfo,
      String text,
      int wordsLimit,
      String template) throws Exception {
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(TOOLKIT.getPreferences());
    try {
      preferencesRepairer.setValue(IPreferenceConstants.P_VARIABLE_TEXT_WORDS_LIMIT, wordsLimit);
      preferencesRepairer.setValue(IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE, template);
      return (String) ReflectionUtils.invokeMethod2(
          NamesManager.class,
          "getNameForText",
          JavaInfo.class,
          String.class,
          javaInfo,
          text);
    } finally {
      preferencesRepairer.restore();
    }
  }

  public void test_textPropertyRename_never() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setText(\"first\");",
            "    add(button);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "first",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_NEVER);
  }

  public void test_textPropertyRename_alwaysBad_controlCharacters() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setText(\"...\");",
            "    add(button);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "...",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
  }

  public void test_textPropertyRename_alwaysBad_nonLatinCharacters() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    button.setText(\"\u0410\u0411\");",
            "    add(button);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "\u0410\u0411",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
  }

  public void test_textPropertyRename_alwaysGood() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton firstButton = new JButton();",
            "    firstButton.setText(\"first\");",
            "    add(firstButton);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "first",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
  }

  public void test_textPropertyRename_defaultFalse() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton aButton = new JButton();",
            "    add(aButton);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton aButton = new JButton();",
            "    aButton.setText(\"first\");",
            "    add(aButton);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "first",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_DEFAULT);
  }

  public void test_textPropertyRename_defaultTrue() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button_1 = new JButton();",
            "    add(button_1);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton firstButton = new JButton();",
            "    firstButton.setText(\"first\");",
            "    add(firstButton);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "first",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_DEFAULT);
  }

  public void test_textPropertyRename_field() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  private JButton m_button_Q;",
            "  public Test() {",
            "    m_button_Q = new JButton();",
            "    add(m_button_Q);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  private JButton m_firstButton_Q;",
            "  public Test() {",
            "    m_firstButton_Q = new JButton();",
            "    m_firstButton_Q.setText(\"first\");",
            "    add(m_firstButton_Q);",
            "  }",
            "}"};
    // remember existing project options and set prefix/suffix
    Map<String, String> options;
    {
      options = ProjectUtils.getOptions(javaProject);
      javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
      javaProject.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, "_Q");
    }
    try {
      check_textPropertyRename(
          lines,
          expectedLines,
          "first",
          IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
    } finally {
      javaProject.setOptions(options);
    }
  }

  public void test_textPropertyRename_duplicate() throws Exception {
    String[] lines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    //",
            "    JButton firstButton = new JButton();",
            "    add(firstButton);",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    JButton firstButton_1 = new JButton();",
            "    firstButton_1.setText(\"first\");",
            "    add(firstButton_1);",
            "    //",
            "    JButton firstButton = new JButton();",
            "    add(firstButton);",
            "  }",
            "}"};
    check_textPropertyRename(
        lines,
        expectedLines,
        "first",
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
  }

  private void check_textPropertyRename(String[] lines,
      String[] expectedLines,
      String text,
      int mode) throws Exception {
    ContainerInfo panel = parseContainer(lines);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(TOOLKIT.getPreferences());
    try {
      preferencesRepairer.setValue(IPreferenceConstants.P_VARIABLE_TEXT_MODE, mode);
      preferencesRepairer.setValue(
          IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE,
          "${text}${class_name}");
      // set "text" property value
      Property textProperty = button.getPropertyByTitle("text");
      textProperty.setValue(text);
      // validate source
      assertEditor(expectedLines);
    } finally {
      preferencesRepairer.restore();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename new
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that rename happens for newly added component.
   */
  public void test_renameNewComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // add new JButton
    ComponentInfo newButton = createComponent(JButton.class);
    flowLayout.add(newButton, null);
    //
    PreferencesRepairer preferences = new PreferencesRepairer(TOOLKIT.getPreferences());
    try {
      preferences.setValue(
          IPreferenceConstants.P_VARIABLE_TEXT_MODE,
          IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
      preferences.setValue(IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE, "${text}${class_name}");
      // set "text" property value
      newButton.getPropertyByTitle("text").setValue("first");
    } finally {
      preferences.restore();
    }
    // validate source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton firstButton = new JButton('first');",
        "      add(firstButton);",
        "    }",
        "  }",
        "}");
  }
}
