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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionRootProcessor;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionManagerActionCreationSupport;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.CoolBarManagerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.GroupMarkerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.IActionIconProvider;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;
import org.eclipse.wb.internal.rcp.palette.ActionExternalEntryInfo;
import org.eclipse.wb.internal.rcp.palette.ActionNewEntryInfo;
import org.eclipse.wb.internal.rcp.palette.ActionUseEntryInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.Image;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ActionInfo} and {@link ActionContainerInfo}.
 * 
 * @author scheglov_ke
 */
public class ActionTest extends RcpModelTest {
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
  // ActionContainerInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ActionContainerInfo}.
   */
  public void test_container() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "}");
    // initially no container
    assertHierarchy("{this: org.eclipse.jface.window.ApplicationWindow} {this} {}");
    // ...so, no Action's
    assertThat(ActionContainerInfo.getActions(window)).isEmpty();
    // ask container
    ActionContainerInfo container = ActionContainerInfo.get(window);
    assertNotNull(container);
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}");
    // it always will return same container
    assertSame(container, ActionContainerInfo.get(window));
    // still no Action's
    assertThat(ActionContainerInfo.getActions(window)).isEmpty();
    // check presentation
    {
      IObjectPresentation presentation = container.getPresentation();
      assertEquals("(actions)", presentation.getText());
      assertNotNull(presentation.getIcon());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Just parsing for some {@link ApplicationWindow}.
   */
  public void test_0() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action('The text') {",
            "        public void run() {",
            "        }",
            "      };",
            "    }",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: m_action} {/new Action('The text')/}");
    assertNotNull(ActionContainerInfo.get(window));
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    // check refresh
    window.refresh();
    assertEquals("The text", ReflectionUtils.invokeMethod2(action.getObject(), "getText"));
    // check "action" presentation
    {
      IObjectPresentation presentation = action.getPresentation();
      assertEquals("m_action", presentation.getText());
      assertNotNull(presentation.getIcon());
    }
  }

  /**
   * Presentation for {@link ActionInfo} should use image from {@link ImageDescriptor}.
   */
  public void test_iconImage_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action(null, null) {",
            "      };",
            "    }",
            "  }",
            "}");
    window.refresh();
    // prepare "action"
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    IObjectPresentation presentation = action.getPresentation();
    // initially generic icon of Action
    Image genericActionIcon =
        ComponentDescriptionHelper.getDescription(
            m_lastEditor,
            m_lastLoader.loadClass("org.eclipse.jface.action.Action")).getIcon();
    assertTrue(UiUtils.equals(genericActionIcon, presentation.getIcon()));
    // add ResourceManager and set ImageDescriptor
    {
      ManagerUtils.ensure_ResourceManager(window);
      ClassInstanceCreation actionCreation =
          (ClassInstanceCreation) action.getCreationSupport().getNode();
      Expression imageDescriptionExpression = (Expression) actionCreation.arguments().get(1);
      m_lastEditor.replaceExpression(imageDescriptionExpression, ImmutableList.of(
          "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(",
          "getClass(),",
          "\"/icons/full/etool16/delete_edit.gif\")"));
    }
    // now icon is got from ImageDescriptor, well at least not default one
    window.refresh();
    assertFalse(UiUtils.equals(genericActionIcon, presentation.getIcon()));
  }

  /**
   * Presentation for {@link ActionInfo} should also try to use {@link IActionIconProvider}.
   */
  public void test_iconImage_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action(null, null) {",
            "      };",
            "    }",
            "  }",
            "}");
    window.refresh();
    // prepare "action"
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    IObjectPresentation presentation = action.getPresentation();
    // initially generic icon of Action
    {
      Image genericActionIcon =
          ComponentDescriptionHelper.getDescription(
              m_lastEditor,
              m_lastLoader.loadClass("org.eclipse.jface.action.Action")).getIcon();
      assertTrue(UiUtils.equals(genericActionIcon, presentation.getIcon()));
    }
    // set CreationSupport with IActionIconProvider
    final Image expectedImage = DesignerPlugin.getImage("test.png");
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(CreationSupport.class);
      enhancer.setInterfaces(new Class<?>[]{IActionIconProvider.class});
      enhancer.setClassLoader(IActionIconProvider.class.getClassLoader());
      enhancer.setCallback(new MethodInterceptor() {
        public Object intercept(Object obj,
            java.lang.reflect.Method method,
            Object[] args,
            MethodProxy proxy) throws Throwable {
          if (method.getName().equals("getActionIcon")) {
            return expectedImage;
          }
          return proxy.invokeSuper(obj, args);
        }
      });
      // set new CreationSupport for "action"
      CreationSupport creationSupport = (CreationSupport) enhancer.create();
      action.setCreationSupport(creationSupport);
    }
    // now "expectedImage"
    assertSame(expectedImage, presentation.getIcon());
  }

  /**
   * Parameters of constructor for {@link Action} have bindings on properties.
   */
  public void test_boundProperties() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      String text = null;",
            "      ImageDescriptor imageDescriptor = null;",
            "      m_action = new Action(text, imageDescriptor) {",
            "      };",
            "    }",
            "  }",
            "}");
    window.refresh();
    // check "action"
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    ((GenericProperty) action.getPropertyByTitle("text")).setValue("The text");
    ((GenericProperty) action.getPropertyByTitle("imageDescriptor")).setExpression(
        "null",
        Property.UNKNOWN_VALUE);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction m_action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "  }",
        "  private void createActions() {",
        "    {",
        "      String text = null;",
        "      ImageDescriptor imageDescriptor = null;",
        "      m_action = new Action('The text', null) {",
        "      };",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No {@link ContributionManagerInfo} - no "Actions" palette category.
   */
  public void test_palette_noManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "  }",
            "}");
    PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
    List<CategoryInfo> categories = Lists.newArrayList();
    listener.categories(categories);
    assertThat(categories).isEmpty();
  }

  /**
   * When there is {@link ToolBarManager}, "Actions" palette category should be added. No
   * {@link ToolBarManager} entry, because no {@link CoolBarManager}. No {@link MenuManager},
   * because no "root" {@link MenuManager}.
   */
  public void test_palette_hasToolBarManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      String text = null;",
            "      ImageDescriptor imageDescriptor = null;",
            "      m_action = new Action(text, imageDescriptor) {",
            "      };",
            "    }",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    // check for "Actions" category
    PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
    List<CategoryInfo> categories = Lists.newArrayList();
    listener.categories(categories);
    assertThat(categories).hasSize(1);
    // check "Actions" category
    CategoryInfo category = categories.get(0);
    List<EntryInfo> entries = category.getEntries();
    assertThat(entries).hasSize(3);
    assertThat(entries.get(0)).isInstanceOf(ActionNewEntryInfo.class);
    assertThat(entries.get(1)).isInstanceOf(ActionExternalEntryInfo.class);
    {
      ComponentEntryInfo entry = (ComponentEntryInfo) entries.get(2);
      assertEquals("org.eclipse.jface.action.Separator", entry.getClassName());
    }
    // ask for dynamic entries
    listener.entries(category, entries);
    assertThat(entries).hasSize(4);
    assertThat(entries.get(3)).isInstanceOf(ActionUseEntryInfo.class);
  }

  /**
   * Users confused over how to deal with JFace {@link ApplicationWindow} and menus. So, we put
   * "JFace Actions" before "Menu" category.
   */
  public void test_palette_actionsCategory_beforeMenu() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    // prepare categories
    List<CategoryInfo> categories;
    {
      categories = Lists.newArrayList();
      // add "Menu" category
      categories.add(new CategoryInfo("org.eclipse.wb.rcp.menu"));
      // ask for categories
      PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
      listener.categories(categories);
      assertThat(categories).hasSize(2);
    }
    // "Actions" category should be before "Menu"
    assertEquals(ActionRootProcessor.ACTIONS_CATEGORY_ID, categories.get(0).getId());
  }

  /**
   * When there is {@link CoolBarManager}, "Actions" palette category should be added. It has
   * {@link ToolBarManager} entry, because of presence of {@link CoolBarManager}. No
   * {@link MenuManager}, because no "root" {@link MenuManager}.
   */
  public void test_palette_hasCoolBarManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      String text = null;",
            "      ImageDescriptor imageDescriptor = null;",
            "      m_action = new Action(text, imageDescriptor) {",
            "      };",
            "    }",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolbarManager = super.createCoolBarManager(style);",
            "    return coolbarManager;",
            "  }",
            "}");
    // check for "Actions" category
    PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
    List<CategoryInfo> categories = Lists.newArrayList();
    listener.categories(categories);
    assertThat(categories).hasSize(1);
    // check "Actions" category
    CategoryInfo category = categories.get(0);
    List<EntryInfo> entries = category.getEntries();
    assertThat(entries).hasSize(4);
    assertThat(entries.get(0)).isInstanceOf(ActionNewEntryInfo.class);
    assertThat(entries.get(1)).isInstanceOf(ActionExternalEntryInfo.class);
    {
      ComponentEntryInfo entry = (ComponentEntryInfo) entries.get(2);
      assertEquals("org.eclipse.jface.action.Separator", entry.getClassName());
    }
    {
      ComponentEntryInfo entry = (ComponentEntryInfo) entries.get(3);
      assertEquals("org.eclipse.jface.action.ToolBarManager", entry.getClassName());
    }
    // ask for dynamic entries
    listener.entries(category, entries);
    assertThat(entries).hasSize(5);
    assertThat(entries.get(4)).isInstanceOf(ActionUseEntryInfo.class);
  }

  /**
   * When there is {@link MenuManager}, "Actions" palette category should be added. It has no
   * {@link ToolBarManager} entry, because no {@link CoolBarManager}. It has {@link MenuManager},
   * because of "root" {@link MenuManager} presence.
   */
  public void test_palette_hasMenuBarManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      String text = null;",
            "      ImageDescriptor imageDescriptor = null;",
            "      m_action = new Action(text, imageDescriptor) {",
            "      };",
            "    }",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "}");
    // check for "Actions" category
    PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
    List<CategoryInfo> categories = Lists.newArrayList();
    listener.categories(categories);
    assertThat(categories).hasSize(1);
    // check "Actions" category
    CategoryInfo category = categories.get(0);
    List<EntryInfo> entries = category.getEntries();
    assertThat(entries).hasSize(4);
    assertThat(entries.get(0)).isInstanceOf(ActionNewEntryInfo.class);
    assertThat(entries.get(1)).isInstanceOf(ActionExternalEntryInfo.class);
    {
      ComponentEntryInfo entry = (ComponentEntryInfo) entries.get(2);
      assertEquals("org.eclipse.jface.action.Separator", entry.getClassName());
    }
    {
      ComponentEntryInfo entry = (ComponentEntryInfo) entries.get(3);
      assertEquals("org.eclipse.jface.action.MenuManager", entry.getClassName());
    }
    // ask for dynamic entries
    listener.entries(category, entries);
    assertThat(entries).hasSize(5);
    assertThat(entries.get(4)).isInstanceOf(ActionUseEntryInfo.class);
  }

  /**
   * When {@link ActionInfo} has {@link EmptyVariableSupport}, we still should have some name.
   */
  public void test_palette_ActionUse_EntryInfo_EmptyVariable() throws Exception {
    setFileContentSrc(
        "test/MyAction.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "import org.eclipse.jface.action.*;",
            "public class MyAction extends Action {",
            "}"));
    waitForAutoBuild();
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new MyAction());",
            "    return toolbarManager;",
            "  }",
            "}");
    // prepare ActionUse_EntryInfo
    ActionUseEntryInfo entry;
    {
      PaletteEventListener listener = window.getBroadcast(PaletteEventListener.class);
      CategoryInfo category = new CategoryInfo("org.eclipse.wb.rcp.jface.actions");
      List<EntryInfo> entries = Lists.newArrayList();
      listener.entries(category, entries);
      entry = (ActionUseEntryInfo) entries.get(0);
    }
    // check properties
    assertThat(entry.getId()).isNotNull();
    assertThat(entry.getName()).isNotNull();
    assertThat(entry.getDescription()).isNotNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IContributionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link IContributionManager#add(IAction)} is used, it creates internally some
   * {@link IContributionItem}.
   */
  public void test_IContributionItem_void() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action('Some text') {",
            "      };",
            "    }",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(m_action);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addToolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createToolBarManager(style)} {local-unique: toolbarManager} {/super.createToolBarManager(style)/ /toolbarManager.add(m_action)/ /toolbarManager/}",
        "    {void} {void} {/toolbarManager.add(m_action)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: m_action} {/new Action('Some text')/ /toolbarManager.add(m_action)/}");
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // "contributionItem"
    ContributionItemInfo contributionItem;
    {
      contributionItem = (ContributionItemInfo) toolBarManager.getChildrenJava().get(0);
      assertInstanceOf(
          ContributionManagerActionCreationSupport.class,
          contributionItem.getCreationSupport());
      assertInstanceOf(VoidInvocationVariableSupport.class, contributionItem.getVariableSupport());
    }
    // refresh(), check "contributionItem"
    window.refresh();
    assertThat(contributionItem.getBounds().width).isGreaterThan(50);
    assertThat(contributionItem.getBounds().height).isGreaterThan(20);
    // "contributionItem" should have same properties as "action"
    {
      Property[] itemProperties = contributionItem.getProperties();
      Property[] actionProperties = action.getProperties();
      assertEquals(actionProperties.length, itemProperties.length);
    }
    // "contributionItem" has same presentation as "action"
    {
      IObjectPresentation actionPresentation = action.getPresentation();
      IObjectPresentation itemPresentation = contributionItem.getPresentation();
      assertSame(actionPresentation.getIcon(), itemPresentation.getIcon());
      assertEquals(actionPresentation.getText(), itemPresentation.getText());
    }
    // delete "contributionItem"
    assertTrue(contributionItem.canDelete());
    contributionItem.delete();
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction m_action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      m_action = new Action('Some text') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * When {@link IContributionManager#add(IAction)} is used, it creates internally some
   * {@link IContributionItem}.
   * <p>
   * When we delete {@link ActionInfo}, its {@link ActionContributionItemInfo} also should be
   * deleted.
   */
  public void test_IContributionItem_deleteAction() throws Exception {
    parseJavaInfo(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction m_action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      m_action = new Action('Some text') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(m_action);",
        "    return toolbarManager;",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addToolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createToolBarManager(style)} {local-unique: toolbarManager} {/super.createToolBarManager(style)/ /toolbarManager.add(m_action)/ /toolbarManager/}",
        "    {void} {void} {/toolbarManager.add(m_action)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: m_action} {/new Action('Some text')/ /toolbarManager.add(m_action)/}");
    ActionInfo action = getJavaInfoByName("m_action");
    // delete "action"
    action.delete();
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    return toolbarManager;",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addToolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createToolBarManager(style)} {local-unique: toolbarManager} {/super.createToolBarManager(style)/ /toolbarManager/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}");
  }

  /**
   * When {@link IContributionManager#add(IAction)} is used, it creates internally some
   * {@link IContributionItem}.
   */
  public void test_IContributionItem_explicit() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action('Some text') {",
            "      };",
            "    }",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new ActionContributionItem(m_action));",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addToolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createToolBarManager(style)} {local-unique: toolbarManager} {/super.createToolBarManager(style)/ /toolbarManager.add(new ActionContributionItem(m_action))/ /toolbarManager/}",
        "    {new: org.eclipse.jface.action.ActionContributionItem} {empty} {/toolbarManager.add(new ActionContributionItem(m_action))/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: m_action} {/new Action('Some text')/ /new ActionContributionItem(m_action)/}");
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // "contributionItem"
    ContributionItemInfo contributionItem;
    {
      contributionItem = (ContributionItemInfo) toolBarManager.getChildrenJava().get(0);
      assertInstanceOf(ConstructorCreationSupport.class, contributionItem.getCreationSupport());
      assertInstanceOf(EmptyVariableSupport.class, contributionItem.getVariableSupport());
    }
    // "contributionItem" has same icon as "action"
    {
      IObjectPresentation actionPresentation = action.getPresentation();
      IObjectPresentation itemPresentation = contributionItem.getPresentation();
      assertSame(actionPresentation.getIcon(), itemPresentation.getIcon());
      assertEquals("(no variable)", itemPresentation.getText());
    }
    // refresh(), check "contributionItem"
    window.refresh();
    assertThat(contributionItem.getBounds().width).isGreaterThan(50);
    assertThat(contributionItem.getBounds().height).isGreaterThan(20);
    // "contributionItem" has just a handful of properties
    {
      Property[] itemProperties = contributionItem.getProperties();
      assertThat(itemProperties.length).isLessThan(6);
    }
  }

  /**
   * Even if we use same {@link ActionInfo} for two {@link ActionContributionItemInfo}, they should
   * have different objects.
   */
  public void test_IContributionItem_sameAction() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    m_action = new Action() {",
            "    };",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(m_action);",
            "    toolbarManager.add(m_action);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    ContributionItemInfo item_1 = (ContributionItemInfo) toolBarManager.getItems().get(0);
    ContributionItemInfo item_2 = (ContributionItemInfo) toolBarManager.getItems().get(1);
    assertNotSame(item_1.getObject(), item_2.getObject());
    assertThat(item_1.getBounds()).isNotEqualTo(item_2.getBounds());
  }

  /**
   * {@link ContributionManager}'s don't like {@link Separator}'s without other items, so sometimes
   * don't create them. However in design mode we still want to see them.
   */
  public void test_IContributionItem_danglingSeparator() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    m_action = new Action() {",
            "    };",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new Separator());",
            "    toolbarManager.add(new Separator());",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    ContributionItemInfo item_1 = (ContributionItemInfo) toolBarManager.getItems().get(0);
    ContributionItemInfo item_2 = (ContributionItemInfo) toolBarManager.getItems().get(1);
    assertNotSame(item_1.getObject(), item_2.getObject());
    assertNotNull(item_1.getBounds());
    assertNotNull(item_2.getBounds());
    assertThat(item_1.getBounds()).isNotEqualTo(item_2.getBounds());
  }

  /**
   * {@link GroupMarker}'s are not visible, but we still want to show them at design canvas.
   */
  public void test_GroupMarker() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new GroupMarker('Some long GroupMarker id'));",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    GroupMarkerInfo groupMarker = (GroupMarkerInfo) toolBarManager.getItems().get(0);
    // check bounds
    {
      Rectangle bounds = groupMarker.getBounds();
      assertNotNull(bounds);
      assertThat(bounds.width).isGreaterThan(135);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContributionManagerInfo#command_CREATE(ActionInfo, ContributionItemInfo)}. <br>
   * Add as last/only item.
   */
  public void test_CREATE_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action('Some text') {",
            "      };",
            "    }",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // perform command
    toolBarManager.command_CREATE(action, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction m_action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      m_action = new Action('Some text') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(m_action);",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link ContributionManagerInfo#command_CREATE(ActionInfo, ContributionItemInfo)}. <br>
   * Add before existing item.
   */
  public void test_CREATE_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new Action('Some text') {",
            "      };",
            "    }",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new Separator());",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    ContributionItemInfo separator = (ContributionItemInfo) toolBarManager.getItems().get(0);
    // perform command
    toolBarManager.command_CREATE(action, separator);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction m_action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      m_action = new Action('Some text') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(m_action);",
        "    toolbarManager.add(new Separator());",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link ContributionManagerInfo#command_CREATE(ContributionItemInfo, ContributionItemInfo)}. <br>
   * Add {@link Separator}.
   */
  public void test_CREATE_3() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // perform command
    ContributionItemInfo separator =
        (ContributionItemInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("org.eclipse.jface.action.Separator"),
            new ConstructorCreationSupport());
    toolBarManager.command_CREATE(separator, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(new Separator());",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE new/external
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for
   * {@link ContributionManagerInfo#command_CREATE(ContributionItemInfo, ContributionItemInfo)}. <br>
   * Add external {@link ActionInfo}.
   */
  public void test_CREATE_externalAction_1() throws Exception {
    setFileContentSrc(
        "test/MyAction.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.jface.action.*;",
            "public class MyAction extends Action {",
            "}"));
    waitForAutoBuild();
    // parse
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // perform command
    ActionInfo action =
        (ActionInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.MyAction"),
            new ConstructorCreationSupport());
    toolBarManager.command_CREATE(action, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private MyAction myAction;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      myAction = new MyAction();",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(myAction);",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link ContributionManagerInfo#command_CREATE(ContributionItemInfo, ContributionItemInfo)}. <br>
   * Add new {@link ActionInfo}.
   */
  public void test_CREATE_newAction_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    window.refresh();
    assertNoErrors(window);
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // initially no Action's
    assertThat(ActionContainerInfo.getActions(window)).isEmpty();
    // perform command
    ActionInfo action = ActionContainerInfo.createNew(window);
    toolBarManager.command_CREATE(action, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private Action action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      action = new Action('New Action') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(action);",
        "    return toolbarManager;",
        "  }",
        "}");
    // single (new Action) expected
    assertThat(ActionContainerInfo.getActions(window)).containsOnly(action);
    // refresh, to ensure that action can be rendered
    window.refresh();
    assertNoErrors(window);
  }

  /**
   * Test for
   * {@link ContributionManagerInfo#command_CREATE(ContributionItemInfo, ContributionItemInfo)}. <br>
   * Add new {@link ActionInfo}.<br>
   * In addition also creates <code>createActions()</code> method.
   */
  public void test_CREATE_newAction_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    // perform command
    ActionInfo action = ActionContainerInfo.createNew(window);
    toolBarManager.command_CREATE(action, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private Action action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      action = new Action('New Action') {",
        "      };",
        "    }",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(action);",
        "    return toolbarManager;",
        "  }",
        "}");
    // refresh, to ensure that action can be rendered
    window.refresh();
    assertNoErrors(window);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for
   * {@link ContributionManagerInfo#command_MOVE(ContributionItemInfo, ContributionItemInfo)}.
   */
  public void test_MOVE_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(new Separator('0'));",
            "    toolbarManager.add(new Separator('1'));",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    ContributionItemInfo separator_0 = (ContributionItemInfo) toolBarManager.getItems().get(0);
    ContributionItemInfo separator_1 = (ContributionItemInfo) toolBarManager.getItems().get(1);
    // perform command
    toolBarManager.command_MOVE(separator_1, separator_0);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(new Separator('1'));",
        "    toolbarManager.add(new Separator('0'));",
        "    return toolbarManager;",
        "  }",
        "}");
    // move second time
    toolBarManager.command_MOVE(separator_1, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(new Separator('0'));",
        "    toolbarManager.add(new Separator('1'));",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link ContributionManagerInfo#command_MOVE(ContributionItemInfo, ContributionItemInfo)}.
   */
  public void test_MOVE_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action_1;",
            "  private IAction action_2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addToolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    action_1 = new Action() {",
            "    };",
            "    action_2 = new Action() {",
            "    };",
            "  }",
            "  protected ToolBarManager createToolBarManager(int style) {",
            "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
            "    toolbarManager.add(action_1);",
            "    toolbarManager.add(action_2);",
            "    return toolbarManager;",
            "  }",
            "}");
    assertNoErrors(window);
    window.refresh();
    // prepare components
    ToolBarManagerInfo toolBarManager = window.getChildren(ToolBarManagerInfo.class).get(0);
    ContributionItemInfo item_1 = (ContributionItemInfo) toolBarManager.getItems().get(0);
    ContributionItemInfo item_2 = (ContributionItemInfo) toolBarManager.getItems().get(1);
    // perform command
    toolBarManager.command_MOVE(item_2, item_1);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction action_1;",
        "  private IAction action_2;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addToolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    action_1 = new Action() {",
        "    };",
        "    action_2 = new Action() {",
        "    };",
        "  }",
        "  protected ToolBarManager createToolBarManager(int style) {",
        "    ToolBarManager toolbarManager = super.createToolBarManager(style);",
        "    toolbarManager.add(action_2);",
        "    toolbarManager.add(action_1);",
        "    return toolbarManager;",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link ContributionManagerInfo#command_MOVE(AbstractComponentInfo,AbstractComponentInfo)}.<br>
   * Move {@link ActionContributionItemInfo} from one {@link ToolBarManagerInfo} into other.
   */
  public void test_IMenuInfo_MOVE_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addCoolBar(SWT.FLAT);",
            "  }",
            "  private void createActions() {",
            "    action = new Action() {",
            "    };",
            "  }",
            "  protected CoolBarManager createCoolBarManager(int style) {",
            "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
            "    {",
            "      ToolBarManager toolBarManager_1 = new ToolBarManager();",
            "      coolBarManager.add(toolBarManager_1);",
            "      toolBarManager_1.add(action);",
            "    }",
            "    {",
            "      ToolBarManager toolBarManager_2 = new ToolBarManager();",
            "      coolBarManager.add(toolBarManager_2);",
            "    }",
            "    return coolBarManager;",
            "  }",
            "}");
    window.refresh();
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addCoolBar(SWT.FLAT)/}",
        "  {superInvocation: super.createCoolBarManager(style)} {local-unique: coolBarManager} {/super.createCoolBarManager(style)/ /coolBarManager.add(toolBarManager_1)/ /coolBarManager.add(toolBarManager_2)/ /coolBarManager/}",
        "    {new: org.eclipse.jface.action.ToolBarManager} {local-unique: toolBarManager_1} {/new ToolBarManager()/ /coolBarManager.add(toolBarManager_1)/ /toolBarManager_1.add(action)/}",
        "      {void} {void} {/toolBarManager_1.add(action)/}",
        "    {new: org.eclipse.jface.action.ToolBarManager} {local-unique: toolBarManager_2} {/new ToolBarManager()/ /coolBarManager.add(toolBarManager_2)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: action} {/new Action()/ /toolBarManager_1.add(action)/}");
    // prepare components
    CoolBarManagerInfo coolBar = window.getChildren(CoolBarManagerInfo.class).get(0);
    ToolBarManagerInfo toolBar_1 = coolBar.getToolBarManagers().get(0);
    ToolBarManagerInfo toolBar_2 = coolBar.getToolBarManagers().get(1);
    ActionContributionItemInfo itemInfo = (ActionContributionItemInfo) toolBar_1.getItems().get(0);
    // check permissions
    {
      assertTrue(itemInfo.getCreationSupport().canReorder());
      assertTrue(itemInfo.getCreationSupport().canReparent());
      assertTrue(itemInfo.getCreationSupport().canUseParent(toolBar_2));
    }
    // do move
    toolBar_2.command_MOVE(itemInfo, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addCoolBar(SWT.FLAT);",
        "  }",
        "  private void createActions() {",
        "    action = new Action() {",
        "    };",
        "  }",
        "  protected CoolBarManager createCoolBarManager(int style) {",
        "    CoolBarManager coolBarManager = super.createCoolBarManager(style);",
        "    {",
        "      ToolBarManager toolBarManager_1 = new ToolBarManager();",
        "      coolBarManager.add(toolBarManager_1);",
        "    }",
        "    {",
        "      ToolBarManager toolBarManager_2 = new ToolBarManager();",
        "      coolBarManager.add(toolBarManager_2);",
        "      toolBarManager_2.add(action);",
        "    }",
        "    return coolBarManager;",
        "  }",
        "}");
    // check new association
    {
      InvocationVoidAssociation association = (InvocationVoidAssociation) itemInfo.getAssociation();
      assertEquals("toolBarManager_2.add(action)", association.getSource());
      assertSame(m_lastEditor.getAstUnit(), association.getInvocation().getRoot());
    }
    // do refresh()
    window.refresh();
    assertNoErrors(window);
  }
}