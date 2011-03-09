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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;

import java.util.List;

/**
 * Abstract model for all <code>org.eclipse.core.databinding.observable.IObservable</code> objects.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public abstract class ObservableInfo extends AstObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link BindableInfo} observable object model.
   */
  public abstract BindableInfo getBindableObject();

  /**
   * @return {@link BindableInfo} observable property model.
   */
  public abstract BindableInfo getBindableProperty();

  /**
   * @return <code>true</code> if this model may be use for more bindings.
   */
  public boolean canShared() {
    return false;
  }

  /**
   * @return {@code true} if bindable object not supported Java Bean model.
   */
  public final boolean isPojoBindable() {
    BindableInfo parent = (BindableInfo) getBindableProperty().getParent();
    if (parent != null) {
      return isPojoBean(parent.getObjectType());
    }
    return isPojoBean(getBindableObject().getObjectType());
  }

  public static boolean isPojoBean(Class<?> beanClass) {
    return isNotExistMethod(
        beanClass,
        "addPropertyChangeListener(java.beans.PropertyChangeListener)")
        && isNotExistMethod(
            beanClass,
            "addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)");
  }

  private static boolean isNotExistMethod(Class<?> beanClass, String signature) {
    return ReflectionUtils.getMethodBySignature(beanClass, signature) == null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return getBindableObject().getPresentation().getTextForBinding()
        + "."
        + getBindableProperty().getPresentation().getTextForBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public void createContentProviders(List<IUiContentProvider> providers,
      BindingUiContentProviderContext context,
      DatabindingsProvider provider) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected ObservableCodeSupport m_codeSupport;

  /**
   * XXX
   */
  public final void setCodeSupport(ObservableCodeSupport codeSupport) {
    m_codeSupport = codeSupport;
  }

  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    if (m_codeSupport == null) {
      super.addSourceCode(lines, generationSupport);
    } else {
      m_codeSupport.addSourceCode(this, lines, generationSupport);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    if (m_codeSupport != null) {
      m_codeSupport.accept(visitor);
    }
  }
}