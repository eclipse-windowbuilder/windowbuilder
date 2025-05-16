/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.alignment;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jface.resource.ImageDescriptor;

import javax.swing.JComponent;

/**
 * The {@link PropertyEditor} for {@link JComponent#setAlignmentX(float)} .
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class AlignmentXPropertyEditor extends AlignmentPropertyEditor {
	public AlignmentXPropertyEditor() {
		super(new String[] { "LEFT_ALIGNMENT", "CENTER_ALIGNMENT", "RIGHT_ALIGNMENT", }, new ImageDescriptor[] {
				CoreImages.ALIGNMENT_H_SMALL_LEFT,
				CoreImages.ALIGNMENT_H_SMALL_CENTER,
				CoreImages.ALIGNMENT_H_SMALL_RIGHT});
	}
}
