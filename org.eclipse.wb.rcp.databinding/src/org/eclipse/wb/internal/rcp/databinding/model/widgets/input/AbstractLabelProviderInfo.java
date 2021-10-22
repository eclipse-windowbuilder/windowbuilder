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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

import java.util.List;

/**
 * Abstract model for <code>JFace<code> viewer label provider.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class AbstractLabelProviderInfo extends SimpleClassObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractLabelProviderInfo(String className) {
    super(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public abstract void createContentProviders(List<IUiContentProvider> providers,
      DatabindingsProvider provider,
      boolean useClear);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Generate source code association with this object.
   */
  public abstract String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception;
}