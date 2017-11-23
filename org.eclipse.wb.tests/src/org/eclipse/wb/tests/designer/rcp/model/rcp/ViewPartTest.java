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

import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ViewCategoryPropertyEditor;
import org.eclipse.wb.internal.rcp.model.rcp.ViewPartInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.NotImplementedException;

/**
 * Test for {@link ViewPartInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewPartTest extends RcpModelTest {
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
   * Test for many elements of {@link ViewPartInfo}.
   */
  public void test_0() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "  public void setFocus() {",
            "  }",
            "  public void init(IViewSite site) throws PartInitException {",
            "    super.init(site);",
            "    createActions();",
            "    initializeToolBar();",
            "    initializeMenu();",
            "  }",
            "  private void createActions() {",
            "  }",
            "  private void initializeToolBar() {",
            "    IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();",
            "  }",
            "  private void initializeMenu() {",
            "    IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.ui.part.ViewPart} {this} {/getViewSite().getActionBars()/ /getViewSite().getActionBars()/}",
        "  {invocationChain: getViewSite().getActionBars().getToolBarManager()} {local-unique: toolbarManager} {/getViewSite().getActionBars().getToolBarManager()/}",
        "  {invocationChain: getViewSite().getActionBars().getMenuManager()} {local-unique: menuManager} {/getViewSite().getActionBars().getMenuManager()/}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    CompositeInfo parentComposite = part.getChildren(CompositeInfo.class).get(0);
    CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
    // refresh()
    part.refresh();
    assertNoErrors(part);
    // check bounds
    assertThat(part.getBounds().width).isEqualTo(600);
    assertThat(part.getBounds().height).isEqualTo(500);
    assertThat(parentComposite.getBounds().width).isGreaterThan(300);
    assertThat(parentComposite.getBounds().height).isGreaterThan(30);
    assertThat(container.getBounds().width).isGreaterThan(300);
    assertThat(container.getBounds().height).isGreaterThan(300);
    // check IMenuPopupInfo for MenuManager
    {
      MenuManagerInfo manager = part.getChildren(MenuManagerInfo.class).get(0);
      IMenuPopupInfo popupObject = part.getMenuImpl(manager);
      assertNotNull(popupObject);
      // model
      assertSame(manager, popupObject.getModel());
      assertSame(manager, popupObject.getToolkitModel());
      // presentation
      assertNull(popupObject.getImage());
      assertThat(popupObject.getBounds().width).isGreaterThan(10);
      assertThat(popupObject.getBounds().height).isGreaterThan(10);
      // menu
      assertSame(MenuObjectInfoUtils.getMenuInfo(manager), popupObject.getMenu());
      assertSame(popupObject.getMenu().getPolicy(), popupObject.getPolicy());
    }
    // check IToolBarManager
    {
      ToolBarManagerInfo manager = part.getChildren(ToolBarManagerInfo.class).get(0);
      Rectangle bounds = manager.getBounds();
      assertThat(bounds.x).isGreaterThan(200);
      assertThat(bounds.y).isGreaterThan(0).isLessThan(10);
      assertThat(bounds.width).isGreaterThan(100).isLessThan(200);
      assertThat(bounds.height).isGreaterThan(20).isLessThan(30);
    }
  }

  /**
   * Test for {@link IViewSite} implementation.
   */
  public void test_IViewSite() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public class Test extends ViewPart {",
            "  public static final String ID = 'some.view.Identifier';",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "  public void setFocus() {",
            "  }",
            "}");
    part.refresh();
    assertHierarchy(
        "{this: org.eclipse.ui.part.ViewPart} {this} {}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    //
    Object viewSite = ReflectionUtils.invokeMethod(part.getObject(), "getViewSite()");
    try {
      ReflectionUtils.invokeMethod(viewSite, "getShell()");
      fail();
    } catch (NotImplementedException e) {
    }
    assertEquals("IViewSite_stub", viewSite.toString());
    assertEquals(0, viewSite.hashCode());
    assertEquals("some.view.Identifier", ReflectionUtils.invokeMethod(viewSite, "getId()"));
    assertEquals(null, ReflectionUtils.invokeMethod(viewSite, "getSecondaryId()"));
    {
      Object window = ReflectionUtils.invokeMethod(viewSite, "getWorkbenchWindow()");
      assertSame(DesignerPlugin.getActiveWorkbenchWindow(), window);
    }
  }

  /**
   * Test for {@link ViewPartInfo} without {@link ContributionManagerInfo}.
   */
  public void test_noContributionManegers() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.ui.*;",
        "import org.eclipse.ui.part.*;",
        "public class Test extends ViewPart {",
        "  public Test() {",
        "  }",
        "  public void createPartControl(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "  }",
        "  public void setFocus() {",
        "  }",
        "  public void init(IViewSite site) throws PartInitException {",
        "    super.init(site);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.ui.part.ViewPart} {this} {}",
        "  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
        "    {implicit-layout: org.eclipse.swt.layout.FillLayout} {implicit-layout} {}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
  }

  /**
   * Test for "normal" {@link GenericProperty}'s.
   */
  public void test_normalProperties() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "  public void setFocus() {",
            "  }",
            "  public void init(IViewSite site) throws PartInitException {",
            "    super.init(site);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "  }",
            "}");
    // try to set "partName" and  "contentDescription"
    part.getPropertyByTitle("partName").setValue("The name");
    part.getPropertyByTitle("contentDescription").setValue("The description");
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.ui.*;",
        "import org.eclipse.ui.part.*;",
        "public class Test extends ViewPart {",
        "  public Test() {",
        "  }",
        "  public void createPartControl(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "  }",
        "  public void setFocus() {",
        "  }",
        "  public void init(IViewSite site) throws PartInitException {",
        "    super.init(site);",
        "    setContentDescription('The description');",
        "    setPartName('The name');",
        "    createActions();",
        "  }",
        "  private void createActions() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Extension properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Current project is not a plugin project, so no "Extension" property.
   */
  public void test_extensionProperties_notPlugin() throws Exception {
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public abstract class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "}");
    assertNull(part.getPropertyByTitle("Extension"));
  }

  /**
   * No "view" extension for this {@link ViewPart} class, so no "Extension" property.
   */
  public void test_extensionProperties_noExtension() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
    AbstractPdeTest.createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // parse
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public abstract class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "}");
    assertNull(part.getPropertyByTitle("Extension"));
  }

  /**
   * Valid "view" extension for this {@link ViewPart} class, so we have "Extension" property and its
   * sub-properties.
   */
  public void test_extensionProperties_hasExtension() throws Exception {
    do_projectDispose();
    do_projectCreate();
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
    AbstractPdeTest.createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' name='name 1' icon='icons/false.gif' class='test.Test'"
            + " category='category_1'/>",
        "  </extension>",
        "</plugin>"});
    // parse
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.ui.*;",
            "import org.eclipse.ui.part.*;",
            "public abstract class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "}");
    // "Extension" property
    Property extensionProperty = part.getPropertyByTitle("Extension");
    assertNotNull(extensionProperty);
    assertTrue(extensionProperty.getCategory().isSystem());
    // sub-properties
    Property[] subProperties = getSubProperties(extensionProperty);
    assertThat(subProperties).hasSize(3);
    {
      Property nameProperty = subProperties[0];
      assertEquals("name", nameProperty.getTitle());
      assertTrue(nameProperty.isModified());
      assertEquals("name 1", nameProperty.getValue());
    }
    {
      Property iconProperty = subProperties[1];
      assertEquals("icon", iconProperty.getTitle());
      assertTrue(iconProperty.isModified());
      assertEquals("icons/false.gif", iconProperty.getValue());
    }
    {
      Property categoryProperty = subProperties[2];
      assertEquals("category", categoryProperty.getTitle());
      assertTrue(categoryProperty.isModified());
      assertEquals("category_1", categoryProperty.getValue());
    }
    // when we set value for some "Extension" sub-property, refresh() should be performed
    {
      final boolean[] refreshed = new boolean[]{false};
      part.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed() throws Exception {
          refreshed[0] = true;
        }
      });
      Property nameProperty = subProperties[0];
      nameProperty.setValue("New name");
      assertTrue(refreshed[0]);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ViewCategory_PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ViewCategoryPropertyEditor}.
   */
  public void test_ViewCategory_PropertyEditor() throws Exception {
    do_projectDispose();
    do_projectCreate();
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
    AbstractPdeTest.createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' class='test.Test' category='category_1'/>",
        "  </extension>",
        "</plugin>"});
    // parse
    ViewPartInfo part =
        parseJavaInfo(
            "import org.eclipse.ui.part.*;",
            "public abstract class Test extends ViewPart {",
            "  public Test() {",
            "  }",
            "  public void createPartControl(Composite parent) {",
            "    Composite container = new Composite(parent, SWT.NULL);",
            "  }",
            "}");
    // "category" property
    Property extensionProperty = part.getPropertyByTitle("Extension");
    Property categoryProperty = getSubProperties(extensionProperty)[2];
    assertEquals("category", categoryProperty.getTitle());
    assertTrue(categoryProperty.isModified());
    assertEquals("category_1", categoryProperty.getValue());
    // ViewCategory_PropertyEditor
    assertSame(ViewCategoryPropertyEditor.INSTANCE, categoryProperty.getEditor());
    assertEquals("category_1", getPropertyText(categoryProperty));
  }
}