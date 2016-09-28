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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.core.editor.palette.model.entry.ChooseComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;

import org.eclipse.swt.graphics.Color;

/**
 * Contains various preference constants.
 *
 * @author scheglov_ke
 * @coverage core.preferences
 */
public interface IPreferenceConstants {
  String TOOLKIT_ID = "org.eclipse.wb.core";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Common
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code>, we should add {@link ComponentEntryInfo} for any component chosen using
   * {@link ChooseComponentEntryInfo}.
   */
  String P_COMMON_PALETTE_ADD_CHOSEN = "common.addChosenComponentsToPalette";
  /**
   * When <code>true</code>, we should accept drop <i>non-visual beans</i> to design canvas.
   */
  String P_COMMON_ACCEPT_NON_VISUAL_BEANS = "common.acceptNonVisualBeans";
  /**
   * When <code>true</code>, we can show debug information on console.
   */
  String P_COMMON_SHOW_DEBUG_INFO = "common.showDebugInfo";
  /**
   * When <code>true</code>, we should show warning if version of Eclipse does not corresponds to
   * the version of Designer.
   */
  String P_COMMON_SHOW_VERSION_WARNING = "common.showVersionWarning";
  /**
   * Linux only. Troubleshooting option. When enabled, all of the flickering prevention tricks we do
   * would be disabled.
   */
  String P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS =
      "common.linux.disableScreenshotWorkarounds";
  ////////////////////////////////////////////////////////////////////////////
  //
  // General
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code>, we should highlight containers without borders.
   */
  String P_GENERAL_HIGHLIGHT_CONTAINERS = "general.highlightContainersWithoutBorders";
  /**
   * When <code>true</code>, we should add "text" property value as suffix for presentation text.
   */
  String P_GENERAL_TEXT_SUFFIX = "general.showTextPropertySuffix";
  /**
   * When <code>true</code>, we should show property table with important properties after adding
   * new component.
   */
  String P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD = "general.importantPropertiesAfterAdd";
  /**
   * When <code>true</code>, we automatically activate direct edit after adding new component.
   */
  String P_GENERAL_DIRECT_EDIT_AFTER_ADD = "general.directEditAfterAdd";
  /**
   * The default width for top level component.
   */
  String P_GENERAL_DEFAULT_TOP_WIDTH = "general.topBoundsDefaultWidth";
  /**
   * The default height for top level component.
   */
  String P_GENERAL_DEFAULT_TOP_HEIGHT = "general.topBoundsDefaultHeight";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Style of editor layout.
   */
  String P_EDITOR_LAYOUT = "editor.layout";
  /**
   * Pages mode, "Source" first.
   */
  int V_EDITOR_LAYOUT_PAGES_SOURCE = 0;
  /**
   * Pages mode, "Design" first.
   */
  int V_EDITOR_LAYOUT_PAGES_DESIGN = 1;
  /**
   * Split mode, above each other, "Design" first.
   */
  int V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN = 2;
  /**
   * Split mode, side by side, "Design" first.
   */
  int V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_DESIGN = 3;
  /**
   * Split mode, above each other, "Source" first.
   */
  int V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE = 4;
  /**
   * Split mode, side by side, "Source" first.
   */
  int V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_SOURCE = 5;
  /**
   * Delay (in milliseconds) to refresh design canvas on source change in split mode.
   */
  String P_EDITOR_LAYOUT_SYNC_DELAY = "editor.layout.syncDelay";
  /**
   * When <code>true</code>, Designer will try to determine if Java file contains GUI source code.
   */
  String P_EDITOR_RECOGNIZE_GUI = "editor.recognizeSourceGUI";
  /**
   * When <code>true</code>, editor should be maximized when switch to "Design" page.
   */
  String P_EDITOR_MAX_DESIGN = "editor.maximizeOnDesignPage";
  /**
   * When <code>true</code>, source code should be formatted on editor save.
   */
  String P_EDITOR_FORMAT_ON_SAVE = "editor.formatOnSave";
  /**
   * When <code>true</code>, automatically go to the component definition in source on component
   * selection.
   */
  String P_EDITOR_GOTO_DEFINITION_ON_SELECTION = "editor.gotoComponentDefinitionOnSelection";
  /**
   * Action to be performed by double-clicking on widget in widget tree.
   */
  String P_EDITOR_TREE_DBL_CLICK_ACTION = "editor.dblClickOnWidgetInTreeAction";
  /**
   * Option of action to be performed by double-clicking on widget in widget tree. Open editor for
   * selected widget at line of widget creation.
   */
  int V_EDITOR_TREE_OPEN_WIDGET_IN_EDITOR = 0;
  /**
   * Option of action to be performed by double-clicking on widget in widget tree. Creates
   * selection/action listener for selected widget if applicable.
   */
  int V_EDITOR_TREE_CREATE_LISTENER = 1;
  /**
   * Option of action to be performed by double-clicking on widget in widget tree. Initiate variable
   * rename of widget.
   */
  int V_EDITOR_TREE_INITIATE_RENAME = 2;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Highlight visited lines
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code>, editor will highlight visited/executed lines.
   */
  String P_HIGHLIGHT_VISITED = "editor.highlightVisitedLines";
  /**
   * The {@link Color} for highlighting visited/executed lines.
   */
  String P_HIGHLIGHT_VISITED_COLOR = "editor.highlightVisitedLines.color";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Code parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Comment that begins hidden/ignored code block.
   */
  String P_CODE_HIDE_BEGIN = "hiddenCode.begin";
  /**
   * Comment that ends hidden/ignored code block.
   */
  String P_CODE_HIDE_END = "hiddenCode.end";
  /**
   * Comment that specifies that this line of code should be ignored.
   */
  String P_CODE_HIDE_LINE = "hiddenCode.singleLine";
  /**
   * Strict evaluation mode - if some required parameter is missing, exceptions will be shown. If
   * not strict, then we will silently use default value.
   */
  String P_CODE_STRICT_EVALUATE = "evaluation.strictMode";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Variables: auto rename
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The mode that describes how variable should be renamed on "text" property change.
   */
  String P_VARIABLE_TEXT_MODE = "textVariable.mode";
  /**
   * Always rename variable on "text" property change.
   */
  int V_VARIABLE_TEXT_MODE_ALWAYS = 0;
  /**
   * Rename variable on "text" property change only if variable has default name.
   */
  int V_VARIABLE_TEXT_MODE_DEFAULT = 1;
  /**
   * Never rename variable on "text" property change.
   */
  int V_VARIABLE_TEXT_MODE_NEVER = 2;
  /**
   * The the template for variable name on "text" property change.
   */
  String P_VARIABLE_TEXT_TEMPLATE = "textVariable.template";
  /**
   * The maximal count of words from "text" property that should be used in template.
   */
  String P_VARIABLE_TEXT_WORDS_LIMIT = "textVariable.wordsLimit";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Variables: type specific
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The id of preferences for type specific variables information.
   */
  String P_VARIABLE_TYPE_SPECIFIC = "typeSpecificVariable.store";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Variables: mark component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If this preference is <code>true</code>, variable name should be placed in component, in GUI
   * toolkit specific way, if supported at all.
   */
  String P_VARIABLE_IN_COMPONENT = "putNameIntoComponent";
  ////////////////////////////////////////////////////////////////////////////
  //
  // NLS
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code> and there are existing {@link AbstractSource}, then when we set value
   * for {@link String} property, it should be automatically externalized.
   */
  String P_NLS_AUTO_EXTERNALIZE = "nls.automaticallyExternalize";
  /**
   * When <code>true</code>, then the string's value should be used ONLY for generating a key,
   * ignoring any other set format on it.
   */
  String P_NLS_KEY_AS_STRING_VALUE_ONLY = "nls.useStringValueForKeyOnly";
  /**
   * When <code>true</code>, then qualified type name should be used for generating key.
   */
  String P_NLS_KEY_QUALIFIED_TYPE_NAME = "nls.useQualifiedTypeNameForKey";
  /**
   * When <code>true</code>, then the string's value should be appended in the end of the key.
   */
  String P_NLS_KEY_HAS_STRING_VALUE = "nls.useStringValueForKey";
  /**
   * Use can enter into property value with this prefix, this should mean that NLS key from some
   * source should be used instead.
   */
  String P_NLS_KEY_AS_VALUE_PREFIX = "nls.prefixOfPropertyValueToSpecifyThatItIsKey";
  /**
   * When <code>true</code>, then changes of variable name will cause rename of NLS keys which have
   * this name inside.
   */
  String P_NLS_KEY_RENAME_WITH_VARIABLE = "nls.renameKeysWithVariables";
  /**
   * Comma separated list of locale names, such as "en, de, ru_RU", which should be always displayed
   * in NLS drop-down.
   */
  String P_NLS_ALWAYS_VISIBLE_LOCALES = "nls.alwaysVisibleLocales";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The id of default {@link LayoutDescription}.
   */
  String P_LAYOUT_DEFAULT = "layout.default";
  /**
   * If this preference is <code>true</code>, newly dropped container (if it supports layout) will
   * use layout manager of its parent.
   */
  String P_LAYOUT_OF_PARENT = "layout.inheritLayoutOfParent";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code>, we use separate sub-menu for each sub-property (select, enum, macro).
   * When <code>false</code>, we generate plain "Style" menu with separators between sub-property
   * items.
   */
  String P_STYLE_PROPERTY_CASCADE_POPUP = "styleProperty.cascadePopup";
}
