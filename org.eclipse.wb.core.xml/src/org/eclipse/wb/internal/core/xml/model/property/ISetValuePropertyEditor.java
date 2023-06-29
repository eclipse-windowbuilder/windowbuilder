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
package org.eclipse.wb.internal.core.xml.model.property;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Extension of {@link PropertyEditor} that can be used to set expression during
 * {@link GenericProperty#setValue(Object)} call.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public interface ISetValuePropertyEditor {
	void setValue(GenericProperty property, Object value) throws Exception;
}
