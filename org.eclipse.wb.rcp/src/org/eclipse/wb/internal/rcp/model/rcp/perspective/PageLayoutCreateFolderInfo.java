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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.util.List;
import java.util.Map;

/**
 * Model for {@link IPageLayout#createFolder(String, int, float, String)}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutCreateFolderInfo extends AbstractPartInfo {
  private final PageLayoutInfo m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageLayoutCreateFolderInfo(PageLayoutInfo page, MethodInvocation invocation)
      throws Exception {
    super(page.getEditor(), getFolderDescription(page), new PageLayoutAddCreationSupport(page,
        invocation));
    m_page = page;
    ObjectInfoUtils.setNewId(this);
    setVariableSupport(new EmptyPureVariableSupport(this));
    setAssociation(new InvocationVoidAssociation());
    page.addChild(this);
  }

  public PageLayoutCreateFolderInfo(PageLayoutInfo page, CreationSupport creationSupport)
      throws Exception {
    super(page.getEditor(), getFolderDescription(page), creationSupport);
    m_page = page;
    ObjectInfoUtils.setNewId(this);
  }

  /**
   * @return the {@link ComponentDescription} for {@link IFolderLayout}.
   */
  private static ComponentDescription getFolderDescription(JavaInfo host) throws Exception {
    AstEditor editor = host.getEditor();
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(host);
    Class<?> folderClass = editorLoader.loadClass("org.eclipse.ui.IFolderLayout");
    return ComponentDescriptionHelper.getDescription(editor, folderClass);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link FolderViewInfo} children.
   */
  public List<FolderViewInfo> getViews() {
    return getChildren(FolderViewInfo.class);
  }

  @Override
  protected void registerLayoutControls(Map<String, Control> idToControl) {
    super.registerLayoutControls(idToControl);
    Control folderControl = (Control) getComponentObject();
    for (FolderViewInfo view : getViews()) {
      idToControl.put(view.getId(), folderControl);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Property m_placeholderProperty = new JavaProperty(this, "placeholder",
      BooleanPropertyEditor.INSTANCE) {
    @Override
    public boolean isModified() throws Exception {
      return true;
    }

    @Override
    public Object getValue() throws Exception {
      return isPlaceholder2();
    }

    @Override
    public void setValue(final Object value) throws Exception {
      ExecutionUtils.run(m_page, new RunnableEx() {
        public void run() throws Exception {
          setPlaceholder((Boolean) value);
        }
      });
    }
  };

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(m_placeholderProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this folder is place-holder, i.e. created using
   *         {@link IPageLayout#createPlaceholderFolder(String, int, float, String)}.
   */
  public boolean isPlaceholder2() {
    String signature = getInvocationSignature();
    return signature.equals("createPlaceholderFolder(java.lang.String,int,float,java.lang.String)");
  }

  /**
   * If given <code>makePlaceholder</code> argument is <code>true</code>,converts creation to use
   * {@link IPageLayout#createPlaceholderFolder(String, int, float, String)}.
   */
  public void setPlaceholder(final boolean makePlaceholder) throws Exception {
    AstEditor editor = getEditor();
    boolean wasPlaceholder = isPlaceholder2();
    if (makePlaceholder && !wasPlaceholder) {
      // replace variable type to IPlaceholderFolderLayout
      if (getVariableSupport() instanceof AbstractSimpleVariableSupport) {
        AbstractSimpleVariableSupport variableSupport =
            (AbstractSimpleVariableSupport) getVariableSupport();
        variableSupport.setType("org.eclipse.ui.IPlaceholderFolderLayout");
      }
      // replace methods for folder and its views
      editor.replaceInvocationName(getInvocation(), "createPlaceholderFolder");
      setViewsMethodName("addPlaceholder");
    } else if (!makePlaceholder && wasPlaceholder) {
      // replace variable type to IFolderLayout
      if (getVariableSupport() instanceof AbstractSimpleVariableSupport) {
        AbstractSimpleVariableSupport variableSupport =
            (AbstractSimpleVariableSupport) getVariableSupport();
        variableSupport.setType("org.eclipse.ui.IFolderLayout");
      }
      // replace methods for folder and its views
      editor.replaceInvocationName(getInvocation(), "createFolder");
      setViewsMethodName("addView");
    }
  }

  /**
   * Replaces name of method between <code>"addView"</code> and <code>"addPlaceholder"</code>.
   */
  private void setViewsMethodName(String methodName) throws Exception {
    AstEditor editor = getEditor();
    for (FolderViewInfo view : getViews()) {
      MethodInvocation invocation = view.getInvocation();
      editor.replaceInvocationName(invocation, methodName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getPresentationIcon() {
    return Activator.getImage("info/perspective/folder.gif");
  }

  @Override
  protected String getPresentationText() {
    return getId();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int VIEWS_CONTAINER_MARGIN = 8;
  private CTabFolder m_folder;
  private Composite m_viewsComposite;

  @Override
  public Object getComponentObject() {
    return m_folder;
  }

  public Object render() throws Exception {
    m_folder = PageLayoutInfo.createPartFolder(m_page.getPartsComposite());
    // create views container, where we will add view icons
    m_viewsComposite = new Composite(m_folder.getParent(), SWT.NONE);
    {
      {
        RowLayout rowLayout = new RowLayout();
        rowLayout.justify = true;
        m_viewsComposite.setLayout(rowLayout);
      }
      // view container should be always above client area of folder
      m_viewsComposite.moveAbove(m_folder);
      m_folder.addControlListener(new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
          relocateViewsComposite();
        }

        private void relocateViewsComposite() {
          Rectangle bounds = m_folder.getBounds();
          Rectangle clientArea = m_folder.getClientArea();
          // change margins
          clientArea.x += VIEWS_CONTAINER_MARGIN;
          clientArea.width -= 2 * VIEWS_CONTAINER_MARGIN;
          clientArea.y += VIEWS_CONTAINER_MARGIN;
          clientArea.height -= 2 * VIEWS_CONTAINER_MARGIN;
          // real parent for views composite is parent of folder, so change bounds
          clientArea.x += bounds.x;
          clientArea.y += bounds.y;
          //
          m_viewsComposite.setBounds(clientArea);
        }
      });
    }
    // return mock as object
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setClassLoader(JavaInfoUtils.getClassLoader(this));
      enhancer.setSuperclass(getDescription().getComponentClass());
      enhancer.setCallback(new MethodInterceptor() {
        public Object intercept(Object obj,
            java.lang.reflect.Method method,
            Object[] args,
            MethodProxy proxy) throws Throwable {
          // return "null", CreationSupport will use render()
          return null;
        }
      });
      return enhancer.create();
    }
  }

  /**
   * @return the {@link CTabFolder} widget, to add {@link CTabItem}'s for {@link FolderViewInfo}.
   */
  CTabFolder getFolder() {
    return m_folder;
  }

  /**
   * @return the {@link Composite}, to add {@link Control}'s for {@link FolderViewInfo}.
   */
  Composite getViewsComposite() {
    return m_viewsComposite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // select first item
    m_folder.setSelection(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link FolderViewInfo}.
   * 
   * @return the created {@link FolderViewInfo}.
   */
  public FolderViewInfo command_CREATE(String viewId, FolderViewInfo nextView) throws Exception {
    // add new MethodInvocation
    MethodInvocation newInvocation;
    {
      StatementTarget target = JavaInfoUtils.getTarget(this, nextView);
      String source =
          TemplateUtils.format("{0}.{1}({2})", this, isPlaceholder2()
              ? "addPlaceholder"
              : "addView", StringConverter.INSTANCE.toJavaSource(this, viewId));
      newInvocation = (MethodInvocation) addExpressionStatement(target, source);
    }
    // create new FolderView_Info
    FolderViewInfo newView = new FolderViewInfo(this, newInvocation);
    moveChild(newView, nextView);
    // add related nodes
    {
      newView.bindToExpression(newInvocation);
      newView.addRelatedNodes(newInvocation);
      addRelatedNodes(newInvocation);
    }
    // OK, we have new view
    return newView;
  }

  /**
   * Moves existing {@link FolderViewInfo} inside or into this folder.
   */
  public FolderViewInfo command_MOVE(FolderViewInfo view, FolderViewInfo nextView) throws Exception {
    if (view.getParent() == this) {
      JavaInfoUtils.move(view, null, this, nextView);
      return view;
    } else {
      FolderViewInfo newView = command_CREATE(view.getId(), nextView);
      view.delete();
      return newView;
    }
  }

  /**
   * Creates new {@link FolderViewInfo} using existing {@link PageLayoutAddViewInfo}.
   * 
   * @return the created {@link FolderViewInfo}.
   */
  public FolderViewInfo command_MOVE(PageLayoutAddViewInfo topView, FolderViewInfo nextView)
      throws Exception {
    FolderViewInfo newView = command_CREATE(topView.getId(), nextView);
    topView.delete();
    return newView;
  }
}
