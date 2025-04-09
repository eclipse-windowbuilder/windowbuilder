/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
