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
package org.eclipse.wb.internal.core.xml.model.broadcast;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;

import java.util.List;

/**
 * Listener for {@link XmlObjectInfo} events.
 * <p>
 * Gives subscribers possibility to participate in copy to clipboard process. For example container
 * can add command for installing layout, layout can commands for creating children, etc.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface XmlObjectClipboardCopy {
	/**
	 * @param object
	 *          the {@link XmlObjectInfo} that is in process of copying.
	 * @param commands
	 *          the {@link List} of {@link ClipboardCommand}'s to add new commands.
	 */
	void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception;
}