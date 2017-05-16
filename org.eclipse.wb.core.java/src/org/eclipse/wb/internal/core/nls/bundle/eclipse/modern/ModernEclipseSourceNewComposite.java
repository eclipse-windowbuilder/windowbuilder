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
package org.eclipse.wb.internal.core.nls.bundle.eclipse.modern;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.AbstractAccessorSourceNewComposite;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

import org.eclipse.swt.widgets.Composite;

/**
 * Composite for creating new source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class ModernEclipseSourceNewComposite extends AbstractAccessorSourceNewComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModernEclipseSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
    // create GUI controls
    createAccessorGroup();
    createPropertyGroup();
    // initialize fields
    {
      initializeAccessorGroup();
      initializePropertyGroup();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return Messages.ModernEclipseSourceNewComposite_title;
  }

  @Override
  public String getSample() {
    return "button.setText( ApplicationMessages.button_text );";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IEditableSource createEditableSource(Object o) throws Exception {
    SourceParameters parameters = (SourceParameters) o;
    // create editable source
    IEditableSource editableSource;
    {
      // check, may be we already have such accessor or property file
      if (parameters.m_accessorExists) {
        // use existing accessor
        ModernEclipseSource source =
            new ModernEclipseSource(m_root, parameters.m_accessorFullClassName, null);
        editableSource = source.getEditable();
      } else if (parameters.m_propertyFileExists) {
        // use existing property file
        ModernEclipseSource source =
            new ModernEclipseSource(m_root, null, parameters.m_propertyBundleName);
        editableSource = source.getEditable();
      } else {
        editableSource = createEmptyEditable(parameters.m_propertyBundleName);
      }
    }
    // configure editable source and return
    editableSource.setKeyGeneratorStrategy(ModernEclipseSource.MODERN_KEY_GENERATOR);
    return editableSource;
  }

  @Override
  public Object createParametersObject() throws Exception {
    SourceParameters parameters = new SourceParameters();
    fillAccessorParameters(parameters);
    fillPropertyParameters(parameters);
    return parameters;
  }
}
