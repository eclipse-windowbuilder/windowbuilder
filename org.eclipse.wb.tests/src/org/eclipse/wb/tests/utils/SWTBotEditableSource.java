/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;

import java.util.Set;

/**
 * Wrapped for the {@link IEditableSource} to allow access when not in the UI
 * thread.
 */
public class SWTBotEditableSource extends SWTBot {
	private final IEditableSource editableSource;

	public SWTBotEditableSource(IEditableSource editableSource) {
		this.editableSource = editableSource;
	}

	/**
	 * Return all key's of this source.
	 */
	public Set<String> getKeys() {
		return UIThreadRunnable.syncExec(editableSource::getKeys);
	}

	/**
	 * Get value for given key and locale.
	 */
	public String getValue(LocaleInfo locale, String key) {
		return UIThreadRunnable.syncExec(() -> editableSource.getValue(locale, key));
	}

	/**
	 * Replace key in all locales.
	 */
	public void renameKey(String oldKey, String newKey) {
		UIThreadRunnable.asyncExec(() -> editableSource.renameKey(oldKey, newKey));
	}

	/**
	 * Mark passed property as externalized.
	 */
	public void externalize(StringPropertyInfo propertyInfo, boolean copyToAllLocales) {
		UIThreadRunnable.asyncExec(() -> editableSource.externalize(propertyInfo, copyToAllLocales));
	}
}
