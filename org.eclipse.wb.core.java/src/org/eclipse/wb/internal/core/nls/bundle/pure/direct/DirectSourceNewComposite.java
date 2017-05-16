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
package org.eclipse.wb.internal.core.nls.bundle.pure.direct;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSourceNewComposite;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.widgets.Composite;

/**
 * Composite for creating new source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class DirectSourceNewComposite extends AbstractBundleSourceNewComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
    // create GUI controls
    createPropertyGroup();
    // initialize fields
    initializePropertyGroup();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return Messages.DirectSourceNewComposite_title;
  }

  @Override
  public String getSample() {
    return "button.setText( ResourceBundle.getBundle(\"full.bundle.name\").getString(\"some.key\") );";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IEditableSource createEditableSource(Object o) {
    SourceParameters parameters = (SourceParameters) o;
    try {
      // create editable source
      IEditableSource editableSource;
      {
        // check, may be we already have such property file
        if (parameters.m_propertyFileExists) {
          // use existing property file
          DirectSource source = new DirectSource(m_root, parameters.m_propertyBundleName);
          editableSource = source.getEditable();
        } else {
          editableSource = createEmptyEditable(parameters.m_propertyBundleName);
        }
      }
      // configure editable source and return
      editableSource.setKeyGeneratorStrategy(AbstractBundleSource.KEY_GENERATOR);
      return editableSource;
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public Object createParametersObject() throws Exception {
    SourceParameters parameters = new SourceParameters();
    fillPropertyParameters(parameters);
    return parameters;
  }
}
