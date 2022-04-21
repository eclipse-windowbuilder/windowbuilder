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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Constructor;

/**
 * Model for {@link SectionPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class SectionPartInfo extends AbstractComponentInfo
    implements
      IThisMethodParameterEvaluator,
      IWrapperInfo {
  private Shell m_shell;
  private Object m_ManagedForm;
  private Composite m_ManagedFormBody;
  private Object m_formPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SectionPartInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying {@link SectionInfo}.
   */
  public SectionInfo getSection() {
    try {
      return (SectionInfo) getWrapper().getWrappedInfo();
    } catch (Exception e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return <code>true</code> if we design this {@link SectionPart} now.
   */
  private boolean isThisSectionPart() {
    return getCreationSupport() instanceof ThisCreationSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    if (isThisSectionPart()) {
      getWrapper().configureHierarchy(getParentJava());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IThisMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.swt.widgets.Composite")) {
      prepare_hosting_ManagedForm();
      return m_ManagedFormBody;
    }
    if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.ui.forms.editor.FormPage")) {
      prepare_hosting_ManagedForm();
      prepare_hosting_FormPage();
      return m_formPage;
    }
    if (index == 2) {
      return ExpandableComposite.TITLE_BAR
          | ExpandableComposite.TWISTIE
          | ExpandableComposite.EXPANDED;
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  private void prepare_hosting_FormPage() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> class_FormPage = editorLoader.loadClass("org.eclipse.ui.forms.editor.FormPage");
    // create FormPage
    Enhancer enhancer = new Enhancer();
    enhancer.setClassLoader(editorLoader);
    enhancer.setSuperclass(class_FormPage);
    enhancer.setCallback(new MethodInterceptor() {
      @Override
      public Object intercept(Object obj,
          java.lang.reflect.Method method,
          Object[] args,
          MethodProxy proxy) throws Throwable {
        String signature = ReflectionUtils.getMethodSignature(method);
        if (signature.equals("getManagedForm()")) {
          return m_ManagedForm;
        }
        // handle in super-Class
        return proxy.invokeSuper(obj, args);
      }
    });
    m_formPage =
        enhancer.create(new Class<?>[]{String.class, String.class}, new Object[]{"id", "title"});
  }

  /**
   * Prepares hosting {@link Shell} and {@link ManagedForm}.
   */
  private void prepare_hosting_ManagedForm() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    // use same Shell and ManagedForm (so same FormToolkit instance) during life of this model
    // We should use same FormToolkit, because some default values for Color/Font properties are
    // from this FormToolkit, so will be disposed with FormToolkit,
    // so we should use IDefaultValueConverter to "copy" them, or use same instance of FormToolkit.
    if (m_shell == null) {
      m_shell = new Shell();
      m_shell.setLayout(new FillLayout());
      // prepare ManagedForm instance
      {
        Class<?> class_ManagedForm = editorLoader.loadClass("org.eclipse.ui.forms.ManagedForm");
        Constructor<?> constructor =
            ReflectionUtils.getConstructor(class_ManagedForm, Composite.class);
        m_ManagedForm = constructor.newInstance(m_shell);
      }
      // initialize ManagedForm.getForm().getBody()
      {
        Object formObject = ReflectionUtils.invokeMethod2(m_ManagedForm, "getForm");
        m_ManagedFormBody = (Composite) ReflectionUtils.invokeMethod2(formObject, "getBody");
        m_ManagedFormBody.setLayout(new FillLayout());
      }
      // schedule dispose
      addBroadcastListener(new ObjectEventListener() {
        @Override
        public void dispose() throws Exception {
          if (m_ManagedForm != null) {
            ReflectionUtils.invokeMethod2(m_ManagedForm, "dispose");
            m_ManagedForm = null;
          }
          if (m_shell != null) {
            m_shell.dispose();
            m_shell = null;
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new SectionPartTopBoundsSupport(this);
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
    return getSection().getObject();
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
    // if we have SectionPart, dispose its GUI
    if (isThisSectionPart()) {
      if (getObject() != null) {
        ReflectionUtils.invokeMethod(
            m_ManagedForm,
            "removePart(org.eclipse.ui.forms.IFormPart)",
            getObject());
        ControlSupport.dispose(getSection().getObject());
      }
    }
    // continue
    super.refresh_dispose();
  }

  @Override
  public void refresh_beforeCreate() throws Exception {
    if (isThisSectionPart()) {
      prepare_hosting_ManagedForm();
    }
    super.refresh_beforeCreate();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // associate this IFormPart (SectionPart is IFormPart) with ManagedForm
    if (isThisSectionPart()) {
      ReflectionUtils.invokeMethod(
          m_ManagedForm,
          "addPart(org.eclipse.ui.forms.IFormPart)",
          getObject());
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      @Override
      public void run() throws Exception {
        SectionPartInfo.super.refresh_fetch();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWrapperInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  private WrapperByMethod m_wrapper;

  @Override
  public WrapperByMethod getWrapper() {
    if (m_wrapper == null) {
      m_wrapper = new WrapperByMethod(this, "getSection") {
        @Override
        protected void configureHierarchy(JavaInfo parent, JavaInfo control) throws Exception {
          if (isThisSectionPart()) {
            softAddChild(m_wrapperInfo, m_wrappedInfo);
          } else {
            super.configureHierarchy(parent, control);
          }
        }
      };
    }
    return m_wrapper;
  }
}
