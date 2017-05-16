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

import org.eclipse.wb.core.editor.palette.model.EntryInfo;

/**
 * Marker interface for {@link EntryInfo} that should be used as default entry on palette.
 * <p>
 * We will use it for now, because I don't see that we need more flexible approach with
 * <code>default</code> attribute in palette definition, or <code>default</code> property in
 * {@link EntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public interface IDefaultEntryInfo {
}
