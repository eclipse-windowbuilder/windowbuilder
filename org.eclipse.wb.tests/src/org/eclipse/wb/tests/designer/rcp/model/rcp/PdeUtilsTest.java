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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewCategoryInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.ui.IPageLayout;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link PdeUtils}.
 * 
 * @author scheglov_ke
 */
public class PdeUtilsTest extends AbstractPdeTest {
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
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#getProject()}.
   */
  public void test_getProject() throws Exception {
    assertSame(m_project, m_utils.getProject());
  }

  /**
   * Test for {@link PdeUtils#hasPDENature(IProject)}.
   */
  public void test_hasPDENature() throws Exception {
    assertTrue(PdeUtils.hasPDENature(m_project));
    // empty project
    do_projectDispose();
    do_projectCreate();
    assertFalse(PdeUtils.hasPDENature(m_project));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ID generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_generateUniqueID() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <foo id='id'/>",
        "    <bar id='id_1'/>",
        "    <baz id='id_3'/>",
        "  </extension>",
        "</plugin>"});
    assertEquals("id_2", m_utils.generateUniqueID("id"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPluginModelBase utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#getId(IPluginModelBase)}.
   */
  public void test_getId_validPluginProject() throws Exception {
    IPluginModelBase plugin = PluginRegistry.findModel(m_project);
    assertNotNull(plugin);
    assertEquals("TestProject", PdeUtils.getId(plugin));
  }

  /**
   * Test for {@link PdeUtils#getId(IPluginModelBase)}.
   */
  public void test_getId_pluginProjectWithoutManifest() throws Exception {
    do_projectDispose();
    do_projectCreate();
    try {
      ProjectUtils.addNature(m_project, "org.eclipse.pde.PluginNature");
      createPluginXML(new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<?eclipse version='3.0'?>",
          "<plugin/>"});
      waitForAutoBuild();
      // we can get IPluginModelBase using IProject, but it has no ID
      IPluginModelBase plugin = PluginRegistry.findModel(m_project);
      assertNotNull(plugin);
      assertNull(PdeUtils.getId(plugin));
    } finally {
      do_projectDispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_notPlugin() throws Exception {
    do_projectDispose();
    do_projectCreate();
    // getExtensionElements()
    {
      List<IPluginElement> elements = m_utils.getExtensionElements("org.eclipse.ui.views", "view");
      assertThat(elements).isEmpty();
    }
  }

  public void test_readOnly() throws Exception {
    {
      String manifest = getFileContent("META-INF/MANIFEST.MF");
      manifest += "Bundle-Localization: plugin\n";
      setFileContent("META-INF", "MANIFEST.MF", manifest);
    }
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "    <view id='id_2' name='name 2' class='C_2'/>",
        "  </extension>",
        "</plugin>"});
    // getExtensionElements()
    {
      List<IPluginElement> elements = m_utils.getExtensionElements("org.eclipse.ui.views", "view");
      assertThat(elements).hasSize(2);
      assertId("id_1", elements.get(0));
      assertId("id_2", elements.get(1));
    }
    // getExtensionElementById()
    {
      assertId("id_1", m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1"));
      assertId("id_2", m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_2"));
      assertNull(m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "noSuchID"));
    }
    // getExtensionElementByClass()
    {
      assertId("id_1", m_utils.getExtensionElementByClass("org.eclipse.ui.views", "view", "C_1"));
      assertId("id_2", m_utils.getExtensionElementByClass("org.eclipse.ui.views", "view", "C_2"));
      assertNull(m_utils.getExtensionElementByClass("org.eclipse.ui.views", "view", "noSuchClass"));
    }
    // getAttribute()
    {
      IPluginElement element =
          m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
      assertNull(PdeUtils.getAttribute(null, "nullElement"));
      assertNull("id_1", PdeUtils.getAttribute(element, "noSuchAttribute"));
      assertEquals("id_1", PdeUtils.getAttribute(element, "id"));
      assertEquals("name 1", PdeUtils.getAttribute(element, "name"));
      assertEquals("C_1", PdeUtils.getAttribute(element, "class"));
    }
  }

  /**
   * Test for attribute value from <code>plugin.properties</code> file.
   */
  public void test_localizedAttribute() throws Exception {
    // update META-INF/MANIFEST.MF to use localization
    {
      String manifest = getFileContent("META-INF/MANIFEST.MF");
      manifest += "Bundle-Localization: plugin\n";
      setFileContent("META-INF/MANIFEST.MF", manifest);
    }
    // prepare plugin.properties
    {
      setFileContent(
          getFile("plugin.properties"),
          getSourceDQ("name_1 = First name", "name_2 = Second name"));
    }
    // prepare plugin.xml
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='%name_1' class='C_1'/>",
        "    <view id='id_2' name='%name_2' class='C_2'/>",
        "  </extension>",
        "</plugin>"});
    // getAttribute()
    {
      IPluginElement element =
          m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
      assertEquals("First name", PdeUtils.getAttribute(element, "name"));
    }
    // getAttribute()
    {
      IPluginElement element =
          m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_2");
      assertEquals("Second name", PdeUtils.getAttribute(element, "name"));
    }
  }

  /**
   * Test for {@link PdeUtils#addPluginImport(String)}.
   */
  public void test_addPluginImport() throws Exception {
    // no org.eclipse.jdt.core.IType
    assertFalse(ProjectUtils.hasType(m_javaProject, "org.eclipse.jdt.core.IType"));
    // add new import: org.eclipse.jdt.core
    m_utils.addPluginImport("org.eclipse.jdt.core");
    // add exist import: org.eclipse.jdt.core
    m_utils.addPluginImport("org.eclipse.jdt.core");
    // OK, org.eclipse.jdt.core.IType now exists in project
    waitForAutoBuild();
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.jdt.core.IType"));
    // validate manifest
    {
      String manifest = getManifest();
      assertThat(manifest).contains(",\n org.eclipse.jdt.core\n");
    }
  }

  /**
   * Test for {@link PdeUtils#addPluginImport(String)}.
   * <p>
   * Check that "\r\n" does not cause problems.
   */
  public void test_addPluginImport_useRN() throws Exception {
    // use "\r\n"
    {
      String manifest = getManifest();
      manifest = manifest.replace("\n", "\r\n");
      setManifest(manifest);
    }
    // add imports
    m_utils.addPluginImport("org.eclipse.jdt.core");
    m_utils.addPluginImport("org.eclipse.jdt.ui");
    // validate
    {
      String manifest = getManifest();
      assertThat(manifest).contains(" org.eclipse.jdt.core,\r\n org.eclipse.jdt.ui\r\n");
    }
  }

  /**
   * Test for {@link PdeUtils#addLibrary(String)}.
   */
  @DisposeProjectAfter
  public void test_addLibrary() throws Exception {
    // prepare empty PDE project
    do_projectDispose();
    do_projectCreate();
    PdeProjectConversionUtils.convertToPDE(m_project, null);
    // add library
    String jarPath = TestUtils.createTemporaryJar("foo.txt", "bar");
    try {
      // add library into manifest
      String jarName = FilenameUtils.getName(jarPath);
      m_utils.addLibrary(jarName);
      // validate manifest
      String manifest = getManifest();
      assertThat(manifest).contains(jarName);
      assertThat(manifest).contains("Bundle-ClassPath: .,\n " + jarName + "\n");
    } finally {
      new File(jarPath).delete();
    }
  }

  /**
   * Test for {@link PdeUtils#setAttribute(IPluginElement, String, String)}.
   */
  public void test_setAttribute_existingAttribute() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // modify existing attribute
    IPluginElement element =
        m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
    m_utils.setAttribute(element, "name", "New name");
    // plugin.xml updated
    assertPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='New name' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // "source" element is still same
    assertEquals("name 1", PdeUtils.getAttribute(element, "name"));
    // request element again, not we see updated attribute
    element = m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
    assertEquals("New name", PdeUtils.getAttribute(element, "name"));
  }

  /**
   * Test for {@link PdeUtils#setAttribute(IPluginElement, String, String)}.
   */
  public void test_setAttribute_newAttribute() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    assertThat(getPluginXML()).doesNotContain("newAttr");
    // set new attribute
    IPluginElement element =
        m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
    m_utils.setAttribute(element, "newAttr", "New value");
    // PDE formats plugin.xml very bad, so check using "contains"
    assertThat(getPluginXML()).contains("newAttr=\"New value\"");
  }

  /**
   * Test for {@link PdeUtils#setAttribute(IPluginElement, String, String)}.
   */
  public void test_setAttribute_removeAttribute() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    assertThat(getPluginXML()).contains("name=");
    // remove existing attribute
    IPluginElement element =
        m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
    m_utils.setAttribute(element, "name", null);
    // plugin.xml updated
    assertPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#setAttribute(IPluginElement, String, String)}.<br>
   * Set value with special (for XML) characters.
   */
  public void test_setAttribute_specialValue() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    assertThat(getPluginXML()).contains("name=");
    // update existing attribute
    {
      IPluginElement element =
          m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
      m_utils.setAttribute(element, "name", "a > b && !c");
    }
    // plugin.xml updated
    assertPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='a &gt; b &amp;&amp; !c' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // try to read
    {
      IPluginElement element =
          m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1");
      assertEquals("a > b && !c", PdeUtils.getAttribute(element, "name"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // createExtensionElement()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#createExtensionElement(String, String, Map)}.<br>
   * Adds new {@link IPluginElement} into existing {@link IPluginExtension}.
   */
  public void test_createExtensionElement_1() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // do create
    IPluginElement element;
    {
      String pointId = "org.eclipse.ui.views";
      m_utils.createExtensionElement(
          pointId,
          "view",
          ImmutableMap.of("id", "id_2", "name", "name 2", "class", "C_2"));
      element = m_utils.getExtensionElementById(pointId, "view", "id_2");
    }
    assertEquals("id_2", PdeUtils.getAttribute(element, "id"));
    assertEquals("name 2", PdeUtils.getAttribute(element, "name"));
    assertEquals("C_2", PdeUtils.getAttribute(element, "class"));
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "\t<extension point='org.eclipse.ui.views'>",
        "\t\t<view id='id_1' name='name 1' class='C_1'/>",
        "  <view",
        "        class='C_2'",
        "        id='id_2'",
        "        name='name 2'>",
        "  </view>",
        "\t</extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#createExtensionElement(String, String, Map)}.<br>
   * Adds new {@link IPluginElement} without existing {@link IPluginExtension}.
   */
  public void test_createExtensionElement_2() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    IPluginElement element;
    {
      String pointId = "org.eclipse.ui.views";
      m_utils.createExtensionElement(
          pointId,
          "view",
          ImmutableMap.of("id", "id_2", "name", "name 2", "class", "C_2"));
      element = m_utils.waitExtensionElementById(pointId, "view", "id_2");
      assertNotNull(element);
    }
    assertEquals("id_2", PdeUtils.getAttribute(element, "id"));
    assertEquals("name 2", PdeUtils.getAttribute(element, "name"));
    assertEquals("C_2", PdeUtils.getAttribute(element, "class"));
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.views'>",
        "      <view",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </view>",
        "   </extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#createExtensionElement(String, String, Map)}.<br>
   * No <code>plugin.xml</code> initially.
   */
  public void test_createExtensionElement_3() throws Exception {
    // initially no plugin.xml file
    assertFalse(getFile("plugin.xml").exists());
    // do create
    IPluginElement element;
    {
      String pointId = "org.eclipse.ui.views";
      m_utils.createExtensionElement(
          pointId,
          "view",
          ImmutableMap.of("id", "id_2", "name", "name 2", "class", "C_2"));
      element = m_utils.waitExtensionElementById(pointId, "view", "id_2");
    }
    assertEquals("id_2", PdeUtils.getAttribute(element, "id"));
    assertEquals("name 2", PdeUtils.getAttribute(element, "name"));
    assertEquals("C_2", PdeUtils.getAttribute(element, "class"));
    // plugin.xml created
    assertTrue(getFile("plugin.xml").exists());
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "<?eclipse version=\"3.2\"?>",
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.views'>",
        "      <view",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </view>",
        "   </extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#createExtensionElement(String, String, Map)}.
   * <p>
   * There was problem with adding extension and waiting for it. Solution - run "ModelModification"
   * in UI thread. However this test in reality does not reproduce it.
   */
  public void test_createExtensionElement_notInUI() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    IPluginElement element;
    {
      final String pointId = "org.eclipse.ui.views";
      Thread thread = new Thread() {
        @Override
        public void run() {
          try {
            m_utils.createExtensionElement(
                pointId,
                "view",
                ImmutableMap.of("id", "id_2", "name", "name 2", "class", "C_2"));
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      };
      thread.start();
      while (thread.isAlive()) {
        waitEventLoop(0);
      }
      //
      element = m_utils.waitExtensionElementById(pointId, "view", "id_2");
      assertNotNull(element);
    }
    assertEquals("id_2", PdeUtils.getAttribute(element, "id"));
    assertEquals("name 2", PdeUtils.getAttribute(element, "name"));
    assertEquals("C_2", PdeUtils.getAttribute(element, "class"));
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.views'>",
        "      <view",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </view>",
        "   </extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#createViewCategoryElement(String, String)}.
   */
  public void test_createViewCategoryElement() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    {
      IPluginElement category = m_utils.createViewCategoryElement("id_2", "name 2");
      assertNotNull(category);
      assertEquals("id_2", PdeUtils.getAttribute(category, "id"));
      assertEquals("name 2", PdeUtils.getAttribute(category, "name"));
    }
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.views'>",
        "      <category",
        "            id='id_2'",
        "            name='name 2'>",
        "      </category>",
        "   </extension>",
        "</plugin>"});
    {
      ViewCategoryInfo category = PdeUtils.getViewCategoryInfo("id_2");
      assertNotNull(category);
      assertEquals("id_2", category.getId());
      assertEquals("name 2", category.getName());
    }
  }

  /**
   * Test for {@link PdeUtils#createViewElement(String, String, String)}.
   */
  public void test_createViewElement() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    m_utils.createViewElement("id_2", "name 2", "C_2");
    m_utils.waitExtensionElementById("org.eclipse.ui.views", "view", "id_2");
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.views'>",
        "      <view",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </view>",
        "   </extension>",
        "</plugin>"});
    {
      ViewInfo view = PdeUtils.getViewInfo("id_2");
      assertNotNull(view);
      assertEquals("id_2", view.getId());
      assertEquals("name 2", view.getName());
      assertEquals("C_2", view.getClassName());
    }
  }

  /**
   * Test for {@link PdeUtils#createEditorElement(String, String, String)}.
   */
  public void test_createEditorElement() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    m_utils.createEditorElement("id_2", "name 2", "C_2");
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.editors'>",
        "      <editor",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </editor>",
        "   </extension>",
        "</plugin>"});
  }

  /**
   * Test for {@link PdeUtils#createPerspectiveElement(String, String, String)}.
   */
  public void test_createPerspectiveElement() throws Exception {
    createPluginXML(new String[]{"<plugin>", "</plugin>"});
    // do create
    m_utils.createPerspectiveElement("id_2", "name 2", "C_2");
    // plugin.xml updated
    m_getSource_ignoreSpaces = true;
    assertPluginXML(new String[]{
        "<plugin>",
        "   <extension",
        "         point='org.eclipse.ui.perspectives'>",
        "      <perspective",
        "            class='C_2'",
        "            id='id_2'",
        "            name='name 2'>",
        "      </perspective>",
        "   </extension>",
        "</plugin>"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove element
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#removeElement(IPluginElement)}.
   */
  public void test_removeElement() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1'/>",
        "    <view id='id_2'/>",
        "    <view id='id_3'/>",
        "  </extension>",
        "</plugin>"});
    // remove element
    IPluginElement element =
        m_utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_2");
    assertNotNull(element);
    m_utils.removeElement(element);
    // plugin.xml updated
    assertPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1'/>",
        "    <view id='id_3'/>",
        "  </extension>",
        "</plugin>"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ViewInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#getViewInfo(String)}.<br>
   * No view with such ID.
   */
  public void test_getViewInfo_noView() throws Exception {
    assertNull(PdeUtils.getViewInfo("no.such.view"));
  }

  /**
   * Test for {@link PdeUtils#getViewInfoDefault(String)}.<br>
   * No view with such ID.
   */
  public void test_getViewInfoDefault() throws Exception {
    String id = "no.such.view";
    ViewInfo viewInfo = PdeUtils.getViewInfoDefault(id);
    assertNotNull(viewInfo);
    assertEquals(id, viewInfo.getId());
    assertEquals(id, viewInfo.getName());
    assertNotNull(viewInfo.getIcon());
  }

  /**
   * Test for {@link PdeUtils#getViewInfo(String)}.<br>
   * From runtime plugin.
   */
  public void test_getViewInfo_runtime() throws Exception {
    String viewId = "org.eclipse.jdt.ui.PackageExplorer";
    // get ViewInfo
    ViewInfo viewInfo = PdeUtils.getViewInfo(viewId);
    assertNotNull(viewInfo);
    assertEquals(viewId, viewInfo.getId());
    assertEquals(
        "org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart",
        viewInfo.getClassName());
    assertEquals("org.eclipse.jdt.ui.java", viewInfo.getCategory());
    assertEquals("Package Explorer", viewInfo.getName());
    // icon exists and is not default
    assertNotNull(viewInfo.getIcon());
    assertNotSame(Activator.getImage("info/perspective/view.gif"), viewInfo.getIcon());
    // same ViewInfo should be returned
    assertSame(viewInfo, PdeUtils.getViewInfo(viewId));
    assertSame(viewInfo, PdeUtils.getViewInfoDefault(viewId));
  }

  /**
   * Test for {@link PdeUtils#getViewInfo(String)}.<br>
   * From workspace plugin.
   */
  public void test_getViewInfo_workspace() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' icon='icons/1.png' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    ensureFolderExists("icons");
    TestUtils.createImagePNG(m_testProject, "icons/1.png", 10, 20);
    // get ViewInfo
    ViewInfo viewInfo = PdeUtils.getViewInfo("id_1");
    assertNotNull(viewInfo);
    assertEquals("id_1", viewInfo.getId());
    assertEquals("C_1", viewInfo.getClassName());
    assertNull(viewInfo.getCategory());
    assertEquals("name 1", viewInfo.getName());
    // icon exists and is not default
    assertNotNull(viewInfo.getIcon());
    assertNotSame(Activator.getImage("info/perspective/view.gif"), viewInfo.getIcon());
    assertEquals(10, viewInfo.getIcon().getBounds().width);
    assertEquals(20, viewInfo.getIcon().getBounds().height);
    // toString()
    assertEquals("(id_1, C_1, null, name 1)", viewInfo.toString());
  }

  /**
   * Test for {@link PdeUtils#getViews()}.
   */
  public void test_getViews() throws Exception {
    List<ViewInfo> views = PdeUtils.getViews();
    Map<String, ViewInfo> idToView = Maps.newTreeMap();
    for (ViewInfo viewInfo : views) {
      idToView.put(viewInfo.getId(), viewInfo);
    }
    // analyze views
    assertThat(views).isNotEmpty();
    assertThat(views.size()).isGreaterThan(35);
    // check for some known views
    Set<String> idSet = idToView.keySet();
    assertThat(idSet).contains("org.eclipse.jdt.ui.PackageExplorer");
    assertThat(idSet).contains(IPageLayout.ID_RES_NAV);
    assertThat(idSet).contains(IPageLayout.ID_OUTLINE);
    assertThat(idSet).contains(IPageLayout.ID_BOOKMARKS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Categories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#getViewCategories()}.
   */
  public void test_getViewCategories() throws Exception {
    List<ViewCategoryInfo> categories = PdeUtils.getViewCategories();
    Map<String, ViewCategoryInfo> idToCategory = Maps.newHashMap();
    for (ViewCategoryInfo category : categories) {
      idToCategory.put(category.getId(), category);
    }
    // analyze categories
    assertThat(categories).isNotEmpty();
    assertThat(categories.size()).isGreaterThan(8);
    // check "Other" category
    {
      ViewCategoryInfo category = idToCategory.get(null);
      assertNotNull(category);
      assertEquals(null, category.getId());
      assertEquals("Other", category.getName());
    }
    // check known category
    {
      ViewCategoryInfo category = idToCategory.get("org.eclipse.jdt.ui.java");
      assertNotNull(category);
      assertEquals("org.eclipse.jdt.ui.java", category.getId());
      assertEquals("Java", category.getName());
      assertEquals("(org.eclipse.jdt.ui.java, Java)", category.toString());
      // views
      List<ViewInfo> views = category.getViews();
      Map<String, ViewInfo> idToView = Maps.newTreeMap();
      for (ViewInfo viewInfo : views) {
        idToView.put(viewInfo.getId(), viewInfo);
      }
      // analyze views
      assertThat(views).isNotEmpty();
      assertThat(views.size()).isGreaterThan(5);
      // check for some known views
      assertNotNull(idToView.get("org.eclipse.jdt.ui.PackageExplorer"));
      assertNull(idToView.get(IPageLayout.ID_RES_NAV));
    }
  }

  /**
   * Test for {@link PdeUtils#getViewCategories()}, using "Other"category for views without
   * category.
   */
  public void test_getViewCategories_otherViews() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1'/>",
        "  </extension>",
        "</plugin>"});
    ViewCategoryInfo otherCategory = PdeUtils.getViewCategories().get(0);
    assertEquals(null, otherCategory.getId());
    // check all views
    boolean hasOurView = false;
    for (ViewInfo view : otherCategory.getViews()) {
      if (view.getId().equals("id_1")) {
        assertEquals("name 1", view.getName());
        assertEquals(null, view.getCategory());
        hasOurView = true;
        break;
      }
    }
    assertTrue(hasOurView);
  }

  /**
   * Test for {@link PdeUtils#getViewCategoryInfo(String)}.<br>
   * From workspace plugin.
   */
  public void test_getViewCategoryInfo_workspace() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <category id='id_1' name='name 1'/>",
        "  </extension>",
        "</plugin>"});
    // get ViewCategoryInfo
    {
      ViewCategoryInfo category = PdeUtils.getViewCategoryInfo("id_1");
      assertNotNull(category);
      assertEquals("id_1", category.getId());
      assertEquals("name 1", category.getName());
    }
    // not existing
    {
      ViewCategoryInfo category = PdeUtils.getViewCategoryInfo("noSuchCategory");
      assertNull(category);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PerspectiveInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PdeUtils#getPerspectiveInfo(String)}.<br>
   * No perspective with such ID.
   */
  public void test_getPerspectiveInfo_noPerspective() throws Exception {
    assertNull(PdeUtils.getPerspectiveInfo("no.such.perspective"));
  }

  /**
   * Test for {@link PdeUtils#getPerspectiveInfoDefault(String)}.<br>
   * No perspective with such ID.
   */
  public void test_getPerspectiveInfoDefault() throws Exception {
    String id = "no.such.perspective";
    PerspectiveInfo perspectiveInfo = PdeUtils.getPerspectiveInfoDefault(id);
    assertNotNull(perspectiveInfo);
    assertEquals(id, perspectiveInfo.getId());
    assertEquals(id, perspectiveInfo.getName());
    assertNotNull(perspectiveInfo.getIcon());
  }

  /**
   * Test for {@link PdeUtils#getPerspectiveInfo(String)}.<br>
   * From runtime plugin.
   */
  public void test_getPerspectiveInfo_runtime() throws Exception {
    String id = "org.eclipse.jdt.ui.JavaPerspective";
    // get PerspectiveInfo
    PerspectiveInfo perspectiveInfo = PdeUtils.getPerspectiveInfo(id);
    assertNotNull(perspectiveInfo);
    assertEquals(id, perspectiveInfo.getId());
    assertEquals(
        "org.eclipse.jdt.internal.ui.JavaPerspectiveFactory",
        perspectiveInfo.getClassName());
    assertEquals("Java", perspectiveInfo.getName());
    assertEquals(
        "(org.eclipse.jdt.ui.JavaPerspective, org.eclipse.jdt.internal.ui.JavaPerspectiveFactory, Java)",
        perspectiveInfo.toString());
    // icon exists and is not default
    assertNotNull(perspectiveInfo.getIcon());
    assertNotSame(Activator.getImage("info/perspective/perspective.gif"), perspectiveInfo.getIcon());
    // same PerspectiveInfo should be returned
    assertSame(perspectiveInfo, PdeUtils.getPerspectiveInfo(id));
    assertSame(perspectiveInfo, PdeUtils.getPerspectiveInfo(id));
  }

  /**
   * Test for {@link PdeUtils#getPerspectives()}.
   */
  public void test_getPerspectives() throws Exception {
    List<PerspectiveInfo> perspectives = PdeUtils.getPerspectives();
    Map<String, PerspectiveInfo> idToPerspective = Maps.newTreeMap();
    for (PerspectiveInfo perspectiveInfo : perspectives) {
      idToPerspective.put(perspectiveInfo.getId(), perspectiveInfo);
    }
    // analyze perspectives
    assertThat(perspectives).isNotEmpty();
    assertThat(perspectives.size()).isGreaterThan(5);
    // check for some known perspectives
    Set<String> idSet = idToPerspective.keySet();
    assertThat(idSet).contains("org.eclipse.ui.resourcePerspective");
    assertThat(idSet).contains("org.eclipse.jdt.ui.JavaPerspective");
    assertThat(idSet).contains("org.eclipse.pde.ui.PDEPerspective");
  }
}