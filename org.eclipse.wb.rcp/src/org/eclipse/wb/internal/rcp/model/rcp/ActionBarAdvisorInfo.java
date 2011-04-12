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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.SelectionToolEntryInfo;
import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionRootProcessor;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.palette.ActionFactoryNewEntryInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import org.apache.commons.lang.NotImplementedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;

/**
 * Model for {@link ActionBarAdvisor}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ActionBarAdvisorInfo extends AbstractComponentInfo
    implements
      IThisMethodParameterEvaluator,
      IJavaInfoRendering {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionBarAdvisorInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
    addPaletteListener();
    // bind IContributionManager's to ActionBarAdvisor
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        ClassLoader editorLoader = JavaInfoUtils.getClassLoader(ActionBarAdvisorInfo.this);
        Class<?> contributionManagerClass =
            editorLoader.loadClass("org.eclipse.jface.action.IContributionManager");
        for (JavaInfo component : components) {
          if (component.getParent() == null
              && contributionManagerClass.isAssignableFrom(component.getDescription().getComponentClass())) {
            addChild(component);
            component.setAssociation(new EmptyAssociation());
          }
        }
      }
    });
    // after adding new Action_Info, register() it
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child instanceof ActionInfo) {
          ActionInfo action = (ActionInfo) child;
          String source = TemplateUtils.format("register({0})", action);
          Expression expression = action.addExpressionStatement(source);
          addRelatedNodes(expression);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link PageLayoutInfo} uses toolkit of RCP, but it does not need SWT, JFace components on
   * palette. It needs only views and perspectives.
   */
  private void addPaletteListener() {
    addBroadcastListener(new PaletteEventListener() {
      @Override
      public void categories2(List<CategoryInfo> categories) throws Exception {
        // remove all except "Actions" category
        for (Iterator<CategoryInfo> I = categories.iterator(); I.hasNext();) {
          CategoryInfo category = I.next();
          if (!category.getId().equals(ActionRootProcessor.ACTIONS_CATEGORY_ID)) {
            I.remove();
          }
        }
        // create "System" category
        {
          CategoryInfo category = new CategoryInfo("system");
          category.setName(ModelMessages.ActionBarAdvisorInfo_systemCategoryName);
          category.setDescription(ModelMessages.ActionBarAdvisorInfo_systemCategoryDescription);
          category.setOpen(true);
          categories.add(0, category);
          // add entries
          {
            SelectionToolEntryInfo entry = new SelectionToolEntryInfo();
            entry.setId("system.selection");
            category.addEntry(entry);
          }
        }
        // create "ActionFactory" category
        {
          CategoryInfo category = new CategoryInfo("ActionFactory");
          category.setName(ModelMessages.ActionBarAdvisorInfo_factoryCategoryName);
          category.setDescription(ModelMessages.ActionBarAdvisorInfo_factoryCategoryDescription);
          category.setOpen(true);
          categories.add(category);
          // add entries
          for (Field field : ActionFactory.class.getFields()) {
            if (ActionFactory.class.isAssignableFrom(field.getType())) {
              try {
                EntryInfo entry = new ActionFactoryNewEntryInfo(field.getName());
                category.addEntry(entry);
              } catch (Throwable e) {
              }
            }
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuManagerInfo}, expected that it exists.
   */
  public MenuManagerInfo getMenuManager() {
    return getChildren(MenuManagerInfo.class).get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IThisMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object m_IActionBarConfigurer;
  private Object m_IWorkbenchWindowConfigurer;
  private Object m_IWorkbenchWindow;

  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(parameter);
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.application.IActionBarConfigurer")) {
      if (m_IActionBarConfigurer == null) {
        prepare_IActionBarConfigurer();
      }
      return m_IActionBarConfigurer;
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * Prepares {@link IActionBarConfigurer} implementation in {@link #m_IActionBarConfigurer}.
   */
  private void prepare_IActionBarConfigurer() throws ClassNotFoundException {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> class_IActionBarConfigurer =
        editorLoader.loadClass("org.eclipse.ui.application.IActionBarConfigurer");
    Class<?> class_IWorkbenchWindowConfigurer =
        editorLoader.loadClass("org.eclipse.ui.application.IWorkbenchWindowConfigurer");
    m_IActionBarConfigurer =
        Proxy.newProxyInstance(
            editorLoader,
            new Class<?>[]{class_IActionBarConfigurer},
            new InvocationHandler() {
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodSignature = ReflectionUtils.getMethodSignature(method);
                if (methodSignature.equals("getWindowConfigurer()")) {
                  return m_IWorkbenchWindowConfigurer;
                }
                if (methodSignature.equals("getCoolBarManager()")) {
                  return m_coolBarManager;
                }
                if (methodSignature.equals("getMenuManager()")) {
                  return m_menuManager;
                }
                throw new NotImplementedException(methodSignature);
              }
            });
    m_IWorkbenchWindowConfigurer =
        Proxy.newProxyInstance(
            editorLoader,
            new Class<?>[]{class_IWorkbenchWindowConfigurer},
            new InvocationHandler() {
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodSignature = ReflectionUtils.getMethodSignature(method);
                if (methodSignature.equals("getWindow()")) {
                  return m_IWorkbenchWindow;
                }
                throw new NotImplementedException(methodSignature);
              }
            });
    m_IWorkbenchWindow = DesignerPlugin.getActiveWorkbenchWindow();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private Shell m_shell;
  private CoolBar m_coolBar;
  private Object m_menuManager;
  private Object m_coolBarManager;

  public void render() throws Exception {
    m_shell = new Shell();
    GridLayoutFactory.create(m_shell).noMargins();
    {
      m_coolBar = new CoolBar(m_shell, SWT.FLAT);
      GridDataFactory.create(m_coolBar).grabH().fillH();
    }
    // prepare Class'es
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> menuManagerClass = editorLoader.loadClass("org.eclipse.jface.action.MenuManager");
    Class<?> coolBarManagerClass =
        editorLoader.loadClass("org.eclipse.jface.action.CoolBarManager");
    // create managers
    m_menuManager = menuManagerClass.newInstance();
    m_coolBarManager = coolBarManagerClass.getConstructor(CoolBar.class).newInstance(m_coolBar);
    // OK, we prepared everything, now add actions/items/managers
    {
      int flags = ActionBarAdvisor.FILL_MENU_BAR | ActionBarAdvisor.FILL_COOL_BAR;
      ReflectionUtils.invokeMethod(getObject(), "fillActionBars(int)", flags);
    }
    // render items
    ReflectionUtils.invokeMethod(m_menuManager, "update(boolean)", true);
    ReflectionUtils.invokeMethod(m_coolBarManager, "update(boolean)", true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new ActionBarAdvisorTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  @Override
  public Object getComponentObject() {
    return m_shell;
  }

  /**
   * @return the top level {@link Shell}.
   */
  Shell getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_shell.dispose();
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    {
      Menu menuBar =
          (Menu) ReflectionUtils.invokeMethod(
              m_menuManager,
              "createMenuBar(org.eclipse.swt.widgets.Decorations)",
              m_shell);
      m_shell.setMenuBar(menuBar);
    }
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      public void run() throws Exception {
        ActionBarAdvisorInfo.super.refresh_fetch();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final WorkbenchWindowAdvisorPropertiesProvider m_windowAdvisorPropertiesProvider =
      new WorkbenchWindowAdvisorPropertiesProvider(this);

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.addAll(m_windowAdvisorPropertiesProvider.getProperties());
    return properties;
  }
}
