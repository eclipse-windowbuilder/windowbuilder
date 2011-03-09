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
package org.eclipse.wb.internal.swing.model.property.editor.alignment;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import javax.swing.JComponent;

/**
 * The {@link PropertyEditor} for {@link JComponent#setAlignmentX(float)} .
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class AlignmentXPropertyEditor extends AlignmentPropertyEditor {
  public AlignmentXPropertyEditor() {
    super(new String[]{"RIGHT_ALIGNMENT", "CENTER_ALIGNMENT", "LEFT_ALIGNMENT",}, new String[]{
        "right.gif",
        "center.gif",
        "left.gif",});
  }
}
