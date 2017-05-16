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
package org.eclipse.wb.core.controls.jface.preference;

import com.google.common.collect.Lists;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.List;

/**
 * A special abstract preference page to host field editors and any other controls with flexible
 * layout.
 * <p>
 * Subclasses must implement the <code>createPageContents</code>.
 * </p>
 *
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 *
 * @version $Revision: 1.5 $
 * @author scheglov_ke
 * @coverage core.control
 */
public abstract class FieldLayoutPreferencePage extends PreferencePage
    implements
      IPropertyChangeListener {
  /**
   * The field editors.
   */
  private final List<FieldEditor> fields = Lists.newArrayList();
  /**
   * The first invalid field editor, or <code>null</code> if all field editors are valid.
   */
  private FieldEditor invalidFieldEditor = null;

  /**
   * Creates a new field editor preference page with an empty title, and no image.
   */
  protected FieldLayoutPreferencePage() {
    // Create a new field editor preference page with an empty title, and no image
  }

  /**
   * Creates a new field editor preference page with the given title, but no image.
   *
   * @param title
   *          the title of this preference page
   */
  protected FieldLayoutPreferencePage(String title) {
    super(title);
  }

  /**
   * Creates a new field editor preference page with the given image, and style.
   *
   * @param title
   *          the title of this preference page
   * @param image
   *          the image for this preference page, or <code>null</code> if none
   */
  protected FieldLayoutPreferencePage(String title, ImageDescriptor image) {
    super(title, image);
  }

  /**
   * Adds the given field editor to this page.
   *
   * @param editor
   *          the field editor
   */
  protected void addField(FieldEditor editor) {
    fields.add(editor);
  }

  /**
   * Recomputes the page's error state by calling <code>isValid</code> for every field editor.
   */
  protected void checkState() {
    boolean valid = true;
    invalidFieldEditor = null;
    // The state can only be set to true if all field editors contain a valid value.
    // So we must check them all.
    for (FieldEditor fieldEditor : fields) {
      valid = valid && fieldEditor.isValid();
      if (!valid) {
        invalidFieldEditor = fieldEditor;
        break;
      }
    }
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contens = createPageContents(parent);
    initialize();
    checkState();
    return contens;
  }

  /**
   * Creates and returns the SWT control for the customized body of this preference page under the
   * given parent composite.
   * <p>
   * This framework method must be implemented by concrete subclasses. Any subclass returning a
   * <code>Composite</code> object whose <code>Layout</code> has default margins (for example, a
   * <code>GridLayout</code>) are expected to set the margins of this <code>Layout</code> to 0
   * pixels.
   * </p>
   *
   * @param parent
   *          the parent composite
   * @return the new control
   */
  protected abstract Control createPageContents(Composite parent);

  /**
   * The field editor preference page implementation of an <code>IDialogPage</code> method disposes
   * of this page's controls and images. Subclasses may override to release their own allocated SWT
   * resources, but must call <code>super.dispose</code>.
   */
  @Override
  public void dispose() {
    super.dispose();
    for (FieldEditor fieldEditor : fields) {
      fieldEditor.setPage(null);
      fieldEditor.setPropertyChangeListener(null);
      fieldEditor.setPreferenceStore(null);
    }
  }

  /**
   * Initializes all field editors.
   */
  protected void initialize() {
    for (FieldEditor fieldEditor : fields) {
      fieldEditor.setPage(null);
      fieldEditor.setPropertyChangeListener(this);
      fieldEditor.setPreferenceStore(getPreferenceStore());
      fieldEditor.load();
    }
  }

  /**
   * The field editor preference page implementation of a <code>PreferencePage</code> method loads
   * all the field editors with their default values.
   */
  @Override
  protected void performDefaults() {
    for (FieldEditor fieldEditor : fields) {
      fieldEditor.loadDefault();
    }
    // Force a recalculation of my error state.
    checkState();
    super.performDefaults();
  }

  /**
   * The field editor preference page implementation of this <code>PreferencePage</code> method
   * saves all field editors by calling <code>FieldEditor.store</code>. Note that this method does
   * not save the preference store itself; it just stores the values back into the preference store.
   *
   * @see FieldEditor#store()
   */
  @Override
  public boolean performOk() {
    for (FieldEditor fieldEditor : fields) {
      fieldEditor.store();
    }
    return true;
  }

  /**
   * The field editor preference page implementation of this <code>IPreferencePage</code> (and
   * <code>IPropertyChangeListener</code>) method intercepts <code>IS_VALID</code> events but passes
   * other events on to its superclass.
   */
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getProperty().equals(FieldEditor.IS_VALID)) {
      boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
      // If the new value is true then we must check all field editors.
      // If it is false, then the page is invalid in any case.
      if (newValue) {
        checkState();
      } else {
        invalidFieldEditor = (FieldEditor) event.getSource();
      }
      setValid(newValue);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible && invalidFieldEditor != null) {
      invalidFieldEditor.setFocus();
    }
  }
}