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
package org.eclipse.wb.internal.core.nls;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.ui.AbstractSourceNewComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Descriptor for {@link AbstractSource}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class SourceDescription {
  private final Class<?> m_sourceClass;
  private final Class<?> m_newCompositeClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SourceDescription(Class<?> sourceClass, Class<?> newCompositeClass) {
    m_sourceClass = sourceClass;
    m_newCompositeClass = newCompositeClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the class of the NLS source - {@link AbstractSource}
   */
  public Class<?> getSourceClass() {
    return m_sourceClass;
  }

  /**
   * @return the class of the new source composite - {@link AbstractSourceNewComposite}
   */
  public Class<?> getNewCompositeClass() {
    return m_newCompositeClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractSource} for given component, {@link GenericProperty} and
   *         {@link Expression}.<br>
   *         Can return <code>null</code> if this source does not recognize such expression.
   */
  public AbstractSource getSource(JavaInfo component,
      GenericProperty property,
      Expression expression,
      List<AbstractSource> sources) throws Exception {
    Method getSourceMethod =
        m_sourceClass.getMethod("get", new Class[]{
            JavaInfo.class,
            GenericProperty.class,
            Expression.class,
            List.class});
    return (AbstractSource) getSourceMethod.invoke(null, new Object[]{
        component,
        property,
        expression,
        sources});
  }

  /**
   * @return the {@link List} of {@link AbstractSource}'s existing in given package.
   */
  @SuppressWarnings("unchecked")
  public List<AbstractSource> getPossibleSources(JavaInfo root, IPackageFragment pkg)
      throws Exception {
    Method getPossibleSourcesMethod =
        m_sourceClass.getMethod("getPossibleSources", new Class[]{
            JavaInfo.class,
            IPackageFragment.class});
    return (List<AbstractSource>) getPossibleSourcesMethod.invoke(null, new Object[]{root, pkg});
  }

  /**
   * @return the result of static method "getTitle()" in "new source composite" class. Result is
   *         title that should be displayed for user.
   */
  public String getTitle() {
    return (String) ReflectionUtils.invokeMethodEx(m_newCompositeClass, "getTitle()");
  }

  /**
   * @return the new {@link AbstractSourceNewComposite} for requesting new source parameters.
   */
  public AbstractSourceNewComposite createNewComposite(Composite parent, JavaInfo root)
      throws Exception {
    Constructor<?> constructor =
        m_newCompositeClass.getConstructor(new Class[]{Composite.class, int.class, JavaInfo.class});
    return (AbstractSourceNewComposite) constructor.newInstance(new Object[]{parent, SWT.NONE, root});
  }

  /**
   * @return the new {@link AbstractSource} for given root and parameters.
   */
  public AbstractSource createNewSource(IEditableSource editableSource,
      JavaInfo root,
      Object parameters) throws Exception {
    Method createMethod =
        m_sourceClass.getMethod("apply_create", new Class[]{
            IEditableSource.class,
            JavaInfo.class,
            Object.class});
    return (AbstractSource) createMethod.invoke(
        null,
        new Object[]{editableSource, root, parameters});
  }
}
