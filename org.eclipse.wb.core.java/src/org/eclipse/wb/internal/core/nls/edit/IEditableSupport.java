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
package org.eclipse.wb.internal.core.nls.edit;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;

import java.util.List;

/**
 * Interface that allows editing of NLS support.
 *
 * We separate this interface from {@link #EditableSupport} implementation to keep clean interface
 * for users (editors).
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IEditableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add listener.
   */
  void addListener(IEditableSupportListener listener);

  /**
   * Remove listener.
   */
  void removeListener(IEditableSupportListener listener);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root {@link JavaInfo}.
   */
  JavaInfo getRoot();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if there are "real" sources.
   */
  boolean hasExistingSources();

  /**
   * @return the {@link List} of {@link IEditableSource}'s.
   */
  List<IEditableSource> getEditableSources();

  /**
   * @return the {@link IEditableSource} for given {@link AbstractSource}.
   */
  IEditableSource getEditableSource(AbstractSource source);

  /**
   * @return the {@link AbstractSource} for given {@link IEditableSource}.
   */
  AbstractSource getSource(IEditableSource editableSource);

  /**
   * Add new source with given parameters.
   */
  void addSource(IEditableSource editableSource,
      SourceDescription sourceDescription,
      Object parameters);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of all components.
   */
  List<JavaInfo> getComponents();

  /**
   * @return the list of children of given component that should be displayed for selecting
   *         properties to externalize.
   */
  List<JavaInfo> getTreeChildren(JavaInfo component) throws Exception;

  /**
   * @return {@link StringPropertyInfo}'s that can be externalized in given component.
   */
  List<StringPropertyInfo> getProperties(JavaInfo component);

  /**
   * Check that given component or any of its children has externalizable properties.
   *
   * For example if we don't have such properties, we don't need to display this component and its
   * children in tree of externalizable properties.
   */
  boolean hasPropertiesInTree(JavaInfo component) throws Exception;

  /**
   * Externalize given property in given source.
   */
  void externalizeProperty(StringPropertyInfo propertyInfo,
      IEditableSource source,
      boolean copyToAllLocales);
}
