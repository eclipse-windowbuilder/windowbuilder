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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.core.utils.ui.TabFolderDecorator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Model for any {@link WorkbenchPart}-like component.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class WorkbenchPartLikeInfo extends AbstractComponentInfo
    implements
      IJavaInfoRendering {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WorkbenchPartLikeInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    WidgetInfo.createExposedChildren(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private Shell m_shell;
  protected CTabFolder m_tabFolder;

  public void render() throws Exception {
    m_shell = new Shell();
    m_shell.setLayout(new FillLayout());
    // prepare gradient workbench-like CTabFolder
    m_tabFolder = new CTabFolder(m_shell, SWT.BORDER | SWT.CLOSE);
    m_tabFolder.setSimple(false);
    m_tabFolder.setMinimizeVisible(true);
    m_tabFolder.setMaximizeVisible(true);
    TabFolderDecorator.setActiveTabColors(true, m_tabFolder);
    // prepare "tabItem" that looks according to concrete model
    CTabItem tabItem = new CTabItem(m_tabFolder, SWT.NONE);
    m_tabFolder.setSelection(tabItem);
    configureTabItem(tabItem);
    // prepare "container" for hosting WorkbenchPart control
    Composite container = new Composite(m_tabFolder, SWT.NONE);
    container.setLayout(new FillLayout());
    tabItem.setControl(container);
    // OK, we prepared everything, now render GUI
    ReflectionUtils.invokeMethod(getObject(), getGUIMethodName()
        + "(org.eclipse.swt.widgets.Composite)", container);
  }

  /**
   * @return the name of method that creates GUI in sub-class of {@link WorkbenchPartLikeInfo}.
   */
  protected abstract String getGUIMethodName();

  /**
   * Configures given {@link CTabItem} in concrete subtype of {@link WorkbenchPartLikeInfo}.
   */
  protected abstract void configureTabItem(CTabItem tabItem) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new WorkbenchPartTopBoundsSupport(this);
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
    return m_tabFolder;
  }

  /**
   * @return the top level {@link Shell}.
   */
  public Shell getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
    }
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      public void run() throws Exception {
        WorkbenchPartLikeInfo.super.refresh_fetch();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Utility method to use in {@link #configureTabItem(CTabItem)} for components that have
   * "Extension" property, i.e. contributed from <code>plugin.xml</code> file.
   */
  protected final void configureTabItem_fromExtension(CTabItem tabItem, String defaultText)
      throws Exception {
    // at parsing time it is dangerous to ask properties
    if (GlobalState.isParsing()) {
      return;
    }
    ComplexProperty extensionProperty = (ComplexProperty) getPropertyByTitle("Extension");
    // text
    {
      String text = defaultText;
      // try to get "name" from extension
      if (extensionProperty != null) {
        Property nameProperty = extensionProperty.getProperties()[0];
        if (nameProperty.isModified()) {
          text = (String) nameProperty.getValue();
        }
      }
      // OK, set text
      tabItem.setText(text);
    }
    // icon
    {
      Image icon = getDescription().getIcon();
      // try to get "icon" from extension
      if (extensionProperty != null) {
        Property iconProperty = extensionProperty.getProperties()[1];
        if (iconProperty.isModified()) {
          String iconPath = (String) iconProperty.getValue();
          IProject project = getEditor().getJavaProject().getProject();
          IFile iconFile = project.getFile(new Path(iconPath));
          if (iconFile.exists()) {
            String iconLocation = iconFile.getLocation().toOSString();
            icon = SwtResourceManager.getImage(iconLocation);
          }
        }
      }
      // OK, set icon
      tabItem.setImage(icon);
    }
  }

  /**
   * @return the ID from <code>ID</code> field.
   */
  protected final String getID() {
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(this);
    FieldDeclaration[] fields = typeDeclaration.getFields();
    for (FieldDeclaration field : fields) {
      for (VariableDeclarationFragment fragment : DomGenerics.fragments(field)) {
        if (fragment.getName().getIdentifier().equals("ID")) {
          Expression initializer = fragment.getInitializer();
          if (initializer instanceof StringLiteral) {
            return ((StringLiteral) initializer).getLiteralValue();
          }
        }
      }
    }
    return null;
  }
}
