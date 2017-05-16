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
package org.eclipse.wb.internal.core.editor.describer;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.ITextContentDescriber;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

/**
 * Implementation of {@link ITextContentDescriber} that understands GUI source.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class JavaSourceUiDescriber extends TextContentDescriber {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ITextContentDescriber
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int describe(Reader contents,
      org.eclipse.core.runtime.content.IContentDescription description) throws IOException {
    String source = IOUtils.toString(contents);
    return isGUISource(source) ? VALID : INVALID;
  }

  @Override
  public int describe(InputStream contents,
      org.eclipse.core.runtime.content.IContentDescription description) throws IOException {
    String source = IOUtils.toString(contents);
    return isGUISource(source) ? VALID : INVALID;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given source code contains GUI for one of the supported GUI
   *         toolkits.
   */
  private static boolean isGUISource(String source) {
    if (DesignerPlugin.getDefault() == null) {
      return false;
    }
    if (!DesignerPlugin.getPreferences().getBoolean(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI)) {
      return false;
    }
    // should have "include" pattern
    if (!hasIncludePattern(source)) {
      return false;
    }
    // should not have "exclude" pattern
    if (hasExcludePattern(source)) {
      return false;
    }
    // OK, this is GUI
    return true;
  }

  private static boolean hasIncludePattern(String source) {
    for (String pattern : getIncludePatterns()) {
      if (source.indexOf(pattern) != -1) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasExcludePattern(String source) {
    for (String pattern : getExcludePatterns()) {
      if (source.indexOf(pattern) != -1) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the contributed "include" patterns. If has one of it - consider source as GUI.
   */
  private static List<String> getIncludePatterns() {
    return getPatterns("includePattern");
  }

  /**
   * @return the contributed "exclude" patterns. If has one of it - consider source as <em>not</em>
   *         GUI.
   */
  private static List<String> getExcludePatterns() {
    return getPatterns("excludePattern");
  }

  private static List<String> getPatterns(String elementName) {
    List<String> patterns = Lists.newArrayList();
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(
            "org.eclipse.wb.core.designerContentPatterns",
            elementName);
    for (IConfigurationElement element : elements) {
      String pattern = element.getValue();
      patterns.add(pattern);
    }
    return patterns;
  }
}
