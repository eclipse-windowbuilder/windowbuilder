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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.ISubParser;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * {@link ObserveType} container: create {@link IObserveInfo}'s and parse observable's.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class ObserveTypeContainer implements ISubParser {
  private final ObserveType m_observeType;
  private final boolean m_isTarget;
  private final boolean m_isModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveTypeContainer(ObserveType observeType, boolean isTarget, boolean isModel) {
    m_observeType = observeType;
    m_isTarget = isTarget;
    m_isModel = isModel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize(IDatabindingsProvider provider) throws Exception {
    // No actions necessary
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accept
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this container can work with given {@link JavaInfo}.
   */
  public boolean accept(JavaInfo javaInfoRoot) {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ObserveType} association with this container.
   */
  public final ObserveType getObserveType() {
    return m_observeType;
  }

  /**
   * @return <code>true</code> if page for hosted {@link ObserveType} is started on "target" mode.
   */
  public final boolean isTargetStartType() {
    return m_isTarget;
  }

  /**
   * @return <code>true</code> if page for hosted {@link ObserveType} is started on "model" mode.
   */
  public final boolean isModelStartType() {
    return m_isModel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse current compilation unit for create {@link IObserveInfo}'s.
   */
  public abstract void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all {@link IObserveInfo}'s with hosted {@link ObserveType} contains into current
   *         compilation unit.
   */
  public abstract List<IObserveInfo> getObservables();

  /**
   * Update all observes (fields, JavaInfo, etc.) maybe more elements is added or removed.
   */
  public abstract void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classpath
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configure given {@link IJavaProject} classpath and copy to project support classes or
   * libraries.
   */
  public boolean ensureDBLibraries(IJavaProject javaProject) throws Exception {
    return false;
  }

  /**
   * Copy to {@link IJavaProject} internal helper classes.
   */
  public void ensureDesignerResources(IJavaProject javaProject) throws Exception {
  }
}