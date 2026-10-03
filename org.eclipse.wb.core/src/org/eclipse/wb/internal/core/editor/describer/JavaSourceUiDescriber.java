/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.editor.describer;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ITextContentDescriber} that understands GUI source.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class JavaSourceUiDescriber extends TextContentDescriber {
	private static final String POINT_ID = DesignerPlugin.PLUGIN_ID + ".designerContentPatterns";

	private record Patterns(Pattern include, Pattern exclude, List<IConfigurationElement> includeElements,
			int includeCount, List<IConfigurationElement> excludeElements, int excludeCount) {
	}

	private static volatile Patterns cachedPatterns;

	////////////////////////////////////////////////////////////////////////////
	//
	// ITextContentDescriber
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException {
		try (BufferedReader reader = new BufferedReader(contents)) {
			return isGUISource(reader);
		}
	}

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(contents))) {
			return isGUISource(reader);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link #VALID} if given source code contains GUI for one of the
	 *         supported GUI toolkits.
	 */
	private static int isGUISource(BufferedReader reader) throws IOException {
		if (DesignerPlugin.getDefault() == null) {
			return INVALID;
		}
		if (!DesignerPlugin.getPreferences().getBoolean(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI)) {
			return INVALID;
		}

		Patterns patterns = getPatterns();
		Pattern includePatterns = patterns.include();
		Pattern excludePatterns = patterns.exclude();

		int description = INDETERMINATE;

		String currentLine = reader.readLine();
		while (currentLine != null) {
			// should have "include" pattern
			if (hasPattern(includePatterns, currentLine)) {
				description = VALID;
				if (excludePatterns == null) {
					break;
				}
			}
			// should not have "exclude" pattern
			if (hasPattern(excludePatterns, currentLine)) {
				description = INVALID;
				break;
			}
			// stop once we reach type definition
			if (currentLine.contains("{")) {
				break;
			}
			currentLine = reader.readLine();
		}
		// OK, this is GUI
		return description;
	}

	private static boolean hasPattern(Pattern pattern, String line) {
		return pattern != null && pattern.matcher(line).find();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the contributed "include" and "exclude" patterns, compiled once
	 *         until the contributions change.
	 */
	private static Patterns getPatterns() {
		// ExternalFactoriesHelper returns a new list once the contributions change
		List<IConfigurationElement> includeElements = ExternalFactoriesHelper.getElements(POINT_ID, "includePattern");
		List<IConfigurationElement> excludeElements = ExternalFactoriesHelper.getElements(POINT_ID, "excludePattern");
		Patterns result = cachedPatterns;
		if (result == null || result.includeElements() != includeElements
				|| result.includeCount() != includeElements.size() || result.excludeElements() != excludeElements
				|| result.excludeCount() != excludeElements.size()) {
			result = new Patterns(compile(includeElements), compile(excludeElements), includeElements,
					includeElements.size(), excludeElements, excludeElements.size());
			cachedPatterns = result;
		}
		return result;
	}

	private static Pattern compile(List<IConfigurationElement> elements) {
		List<String> patterns = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			String pattern = element.getValue();
			patterns.add(pattern);
		}
		if (patterns.isEmpty()) {
			return null;
		}
		String regex = patterns.stream().map(Pattern::quote).collect(Collectors.joining("|"));
		return Pattern.compile(regex);
	}
}
