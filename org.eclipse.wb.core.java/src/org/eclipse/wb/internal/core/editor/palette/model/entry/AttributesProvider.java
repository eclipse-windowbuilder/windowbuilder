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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

/**
 * Provider for attributes (and may be other information) for palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public interface AttributesProvider {
  String getAttribute(String name);
}
