/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.ui.dialogs;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for editing array of {@link String}'s.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class StringsDialog extends TextDialog {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringsDialog(Shell parentShell,
			AbstractUIPlugin plugin,
			String titleText,
			String headerText,
			String footerText) {
		super(parentShell, plugin, titleText, headerText, footerText);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Items
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the items to edit.
	 */
	public void setItems(String[] items) {
		setText(StringUtils.join(items, "\n"));
	}

	/**
	 * @return the edited items.
	 */
	public String[] getItems() {
		return ExecutionUtils.runObjectLog(() -> {
			List<String> strings = new ArrayList<>();
			BufferedReader br = new BufferedReader(new StringReader(getText()));
			while (true) {
				String s = br.readLine();
				if (s == null) {
					break;
				}
				strings.add(s);
			}
			return strings.toArray(new String[strings.size()]);
		}, ArrayUtils.EMPTY_STRING_ARRAY);
	}
}
