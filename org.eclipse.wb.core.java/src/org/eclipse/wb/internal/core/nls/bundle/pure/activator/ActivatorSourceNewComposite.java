/*******************************************************************************
 * Copyright (c) 2019-2020
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    bergert - modified to implement Activator.getPluginBundle().getString()
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.bundle.pure.activator;

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
 * @author bergert
 * @coverage core.nls.ui
 */
public final class ActivatorSourceNewComposite extends AbstractBundleSourceNewComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActivatorSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
    // create GUI controls
    createPropertyGroup();
    // initialize fields
    initializePropertyGroupActivator();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return Messages.ActivatorSourceNewComposite_title;
  }

  @Override
  public String getSample() {
    return "button.setText( Activator.getPluginBundle().getString(\"some.key\") );\r\n"
        + "\r\n"
        + "public class Activator { // extends AbstractUIPlugin \r\n"
        + "   private static ResourceBundle pluginBundle = ResourceBundle.getBundle(\"plugin\");\r\n"
        + "   public static ResourceBundle getPluginBundle() {\r\n"
        + "      return pluginBundle;\r\n"
        + "   }\r\n"
        + "}";
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
          ActivatorSource source = new ActivatorSource(m_root, parameters.m_propertyBundleName);
          editableSource = source.getEditable();
        } else {
          editableSource = createEmptyEditable(parameters.m_propertyBundleName);
        }
      }
      // configure editable source and return
      editableSource.setKeyGeneratorStrategy(AbstractBundleSource.KEY_GENERATOR);
      return editableSource;
    } catch (Exception e) {
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
