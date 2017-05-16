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
package org.eclipse.wb.internal.core.nls.bundle.eclipse.old;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;
import org.eclipse.wb.internal.core.nls.bundle.eclipse.AbstractAccessorSourceNewComposite;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite for creating new source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class EclipseSourceNewComposite extends AbstractAccessorSourceNewComposite {
  private SelectionButtonDialogFieldGroup m_buttonsGroup;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EclipseSourceNewComposite(Composite parent, int style, JavaInfo root) {
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
  // Accessor group
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createAdditionalAccessorFields(Composite parent) {
    m_buttonsGroup =
        new SelectionButtonDialogFieldGroup(SWT.CHECK,
            new String[]{Messages.EclipseSourceNewComposite_useDefaultNames},
            3);
    m_buttonsGroup.doFillIntoGrid(parent, 3);
  }

  @Override
  protected void validateAccessorFields() {
    super.validateAccessorFields();
    try {
      SourceParameters parameters = (SourceParameters) createParametersObject();
      ClassLoader editorLoader = EditorState.get(m_editor).getEditorLoader();
      Class<?> accessorClass = editorLoader.loadClass(parameters.m_accessorFullClassName);
      try {
        accessorClass.getDeclaredField("BUNDLE_NAME");
      } catch (Throwable e) {
        setInvalid(KEY_ACCESSOR_CLASS, Messages.EclipseSourceNewComposite_validateNoBundleNameField);
      }
    } catch (Throwable e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return Messages.EclipseSourceNewComposite_title;
  }

  @Override
  public String getSample() {
    return "button.setText( ApplicationMessages.getString(\"some.key\", \"Optional default value\") );";
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
        // check, may be we already have such accessor or property file
        if (parameters.m_accessorExists) {
          // use existing accessor
          EclipseSource source =
              new EclipseSource(m_root, parameters.m_accessorFullClassName, null);
          editableSource = source.getEditable();
        } else if (parameters.m_propertyFileExists) {
          // use existing property file
          EclipseSource source = new EclipseSource(m_root, null, parameters.m_propertyBundleName);
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
    parameters.m_withDefaultValue = m_buttonsGroup.isSelected(0);
    fillAccessorParameters(parameters);
    fillPropertyParameters(parameters);
    return parameters;
  }
}
