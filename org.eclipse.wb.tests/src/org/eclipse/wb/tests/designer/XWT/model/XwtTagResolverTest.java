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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.xwt.model.util.XwtTagResolver;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link XwtTagResolver}.
 *
 * @author scheglov_ke
 */
public class XwtTagResolverTest extends XwtModelTest {
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
  public void test_standardControl() throws Exception {
    parse("<Shell/>");
    String componentClassName = "org.eclipse.swt.widgets.Button";
    String expectedNamespace = "";
    String expectedTag = "Button";
    String[] expectedLines = {"<Shell/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  public void test_standardLayout() throws Exception {
    parse("<Shell/>");
    String componentClassName = "org.eclipse.swt.layout.RowLayout";
    String expectedNamespace = "";
    String expectedTag = "RowLayout";
    String[] expectedLines = {"<Shell/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  public void test_customControl() throws Exception {
    parse("<Shell/>");
    String componentClassName = "org.eclipse.swt.custom.CLabel";
    String expectedNamespace = "";
    String expectedTag = "CLabel";
    String[] expectedLines = {"<Shell/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  /**
   * Test for "standard custom" controls which are not imported yet by XWT.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=305992
   */
  public void test_customControl_notImportedYet() throws Exception {
    parse("<Shell/>");
    String expectedNamespace = "";
    String[] expectedLines = {"<Shell/>"};
    // TableTree
    {
      String componentClassName = "org.eclipse.swt.custom.TableTree";
      String expectedTag = "TableTree";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
    // ViewForm
    {
      String componentClassName = "org.eclipse.swt.custom.ViewForm";
      String expectedTag = "ViewForm";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
    // CBanner
    {
      String componentClassName = "org.eclipse.swt.custom.CBanner";
      String expectedTag = "CBanner";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
    // TableCursor
    {
      String componentClassName = "org.eclipse.swt.custom.TableCursor";
      String expectedTag = "TableCursor";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
  }

  public void test_standardViewer() throws Exception {
    parse("<Shell/>");
    String componentClassName = "org.eclipse.jface.viewers.TableViewer";
    String expectedNamespace = "";
    String expectedTag = "TableViewer";
    String[] expectedLines = {"<Shell/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  public void test_forms_noFormsComment() throws Exception {
    parse("<Shell/>");
    {
      String componentClassName = "org.eclipse.ui.forms.widgets.Form";
      String expectedNamespace = "p1";
      String expectedTag = "Form";
      String[] expectedLines = {"<Shell xmlns:p1='clr-namespace:org.eclipse.ui.forms.widgets'/>"};
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
  }

  public void test_forms_hasFormsComment() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<!-- Forms API -->",
        "<Shell/>");
    {
      String componentClassName = "org.eclipse.ui.forms.widgets.Form";
      String expectedNamespace = "";
      String expectedTag = "Form";
      String[] expectedLines =
          {
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<!-- Forms API -->",
              "<Shell/>"};
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
  }

  /**
   * Test for Forms API layouts, which are not imported by XWT yet.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=310241
   */
  public void test_forms_notImportedYet() throws Exception {
    parse("<Shell/>");
    String expectedNamespace = "p1";
    String[] expectedLines = {"<Shell xmlns:p1='clr-namespace:org.eclipse.ui.forms.widgets'/>"};
    // ColumnLayout
    {
      String componentClassName = "org.eclipse.ui.forms.widgets.ColumnLayout";
      String expectedTag = "ColumnLayout";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
    // TableWrapLayout
    {
      String componentClassName = "org.eclipse.ui.forms.widgets.TableWrapLayout";
      String expectedTag = "TableWrapLayout";
      assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
    }
  }

  /**
   * No default namespace for XWT, but it is used by default, so don't import it.
   */
  public void test_standardControl_noDefaultNamespace() throws Exception {
    m_getSource_includeStandardNamespaces = false;
    parse("<Shell/>");
    //
    String componentClassName = "org.eclipse.swt.widgets.Button";
    String expectedNamespace = "";
    String expectedTag = "Button";
    String[] expectedLines = {"<Shell/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  /**
   * Default namespace is declared, but it is not standard XWT.
   */
  public void test_standardControl_overrideDefaultNamespace() throws Exception {
    m_getSource_includeStandardNamespaces = false;
    String source = "<xwt:Shell xmlns:xwt='http://www.eclipse.org/xwt/presentation'/>";
    parse(source);
    //
    String componentClassName = "org.eclipse.swt.widgets.Button";
    String expectedNamespace = "xwt";
    String expectedTag = "Button";
    String[] expectedLines = {source};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  public void test_customControl_hasPackageNamespace() throws Exception {
    m_getSource_includeStandardNamespaces = false;
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY);
    parse("<Shell xmlns:abc='clr-namespace:test'/>");
    //
    String componentClassName = "test.MyComponent";
    String expectedNamespace = "abc";
    String expectedTag = "MyComponent";
    String[] expectedLines = {"<Shell xmlns:abc='clr-namespace:test'/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  public void test_customControl_noPackageNamespace() throws Exception {
    m_getSource_includeStandardNamespaces = false;
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY);
    parse("<Shell/>");
    //
    String componentClassName = "test.MyComponent";
    String expectedNamespace = "p1";
    String expectedTag = "MyComponent";
    String[] expectedLines = {"<Shell xmlns:p1='clr-namespace:test'/>"};
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, expectedLines);
  }

  /**
   * Asserts that adding {@link XmlObjectInfo} with given name produces expected namespace and tag.
   */
  private void assertNamespaceTag(String componentClassName,
      String expectedNamespace,
      String expectedTag,
      String[] expectedLines) throws Exception {
    XmlObjectInfo object = createObject(componentClassName);
    Class<?> componentClass = object.getDescription().getComponentClass();
    //
    String[] namespaceArray = new String[1];
    String[] tagArray = new String[1];
    object.getBroadcast(XmlObjectResolveTag.class).invoke(
        object,
        componentClass,
        namespaceArray,
        tagArray);
    String namespace = namespaceArray[0];
    String tag = tagArray[0];
    //
    assertEquals(expectedNamespace, namespace);
    assertEquals(expectedTag, tag);
    assertXML(expectedLines);
  }
}