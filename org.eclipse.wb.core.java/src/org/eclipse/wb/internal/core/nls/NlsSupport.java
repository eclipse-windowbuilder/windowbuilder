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
package org.eclipse.wb.internal.core.nls;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.nls.commands.AbstractCommand;
import org.eclipse.wb.internal.core.nls.commands.AddKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.AddLocaleCommand;
import org.eclipse.wb.internal.core.nls.commands.CreateSourceCommand;
import org.eclipse.wb.internal.core.nls.commands.ExternalizePropertyCommand;
import org.eclipse.wb.internal.core.nls.commands.InternalizeKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.RemoveLocaleCommand;
import org.eclipse.wb.internal.core.nls.commands.RenameKeyCommand;
import org.eclipse.wb.internal.core.nls.commands.SetValuesCommand;
import org.eclipse.wb.internal.core.nls.edit.EditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyRenameStrategy;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Helper for NLS support.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class NlsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String NLS_SUPPORT = "NLS_SUPPORT";

  /**
   * @return the {@link NlsSupport} for component of {@link JavaInfo} hierarchy.
   */
  public static NlsSupport get(JavaInfo component) throws Exception {
    AstEditor editor = component.getEditor();
    NlsSupport support = (NlsSupport) editor.getGlobalValue(NLS_SUPPORT);
    if (support == null) {
      support = new NlsSupport(component.getRootJava());
      editor.putGlobalValue(NLS_SUPPORT, support);
    }
    return support;
  }

  /**
   * Supports conversion of {@link Expression} into NLS value during parsing, i.e. when no root
   * {@link JavaInfo} is known.
   *
   * @return the {@link String} value or <code>null</code> if given {@link Expression} does not
   *         represent any known NLS pattern.
   */
  public static String getValue(JavaInfo component, Expression expression) throws Exception {
    for (SourceDescription sourceDescription : getSourceDescriptions(component)) {
      try {
        List<AbstractSource> sources = Lists.newArrayList();
        AbstractSource source = sourceDescription.getSource(component, null, expression, sources);
        if (source != null) {
          setSource(expression, source);
          return source.getValue(expression);
        }
      } catch (Throwable e) {
      }
    }
    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // SourceDescription's
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_NLS_SOURCES = "org.eclipse.wb.core.nlsSources";
  private static Map<String, SourceDescription> m_idToDescription = Maps.newTreeMap();
  private static Map<String, SourceDescription[]> m_toolkitToDescriptions = Maps.newTreeMap();

  /**
   * @return the {@link SourceDescription} with given id.
   */
  private static SourceDescription getSourceDescription(String id) throws Exception {
    SourceDescription description = m_idToDescription.get(id);
    if (description == null) {
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_NLS_SOURCES, "description");
      for (IConfigurationElement element : elements) {
        // check id
        if (id.equals(element.getAttribute("id"))) {
          // prepare classes
          Bundle bundle = ExternalFactoriesHelper.getExtensionBundle(element);
          Class<?> sourceClass = bundle.loadClass(element.getAttribute("source"));
          Class<?> compositeClass = bundle.loadClass(element.getAttribute("composite"));
          // create description
          description = new SourceDescription(sourceClass, compositeClass);
          m_idToDescription.put(id, description);
        }
      }
    }
    //
    return description;
  }

  /**
   * @return array of {@link SourceDescription}'s for given {@link JavaInfo}.
   */
  public static SourceDescription[] getSourceDescriptions(JavaInfo component) throws Exception {
    // prepare descriptions for toolkit
    String toolkitId = component.getDescription().getToolkit().getId();
    SourceDescription[] descriptions = m_toolkitToDescriptions.get(toolkitId);
    if (descriptions == null) {
      List<SourceDescription> descriptionList = Lists.newArrayList();
      // check all binding's
      List<IConfigurationElement> elements =
          ExternalFactoriesHelper.getElements(POINT_NLS_SOURCES, "binding");
      for (IConfigurationElement element : elements) {
        // check for toolkit
        if (toolkitId.equals(element.getAttribute("toolkit"))) {
          String descriptionId = element.getAttribute("description");
          SourceDescription description = getSourceDescription(descriptionId);
          if (description != null) {
            descriptionList.add(description);
          }
        }
      }
      descriptions = descriptionList.toArray(new SourceDescription[descriptionList.size()]);
    }
    //
    return descriptions;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final JavaInfo m_root;
  private final SourceDescription[] m_sourceDescriptions;
  private final List<AbstractSource> m_sources = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private NlsSupport(JavaInfo root) throws Exception {
    m_root = root;
    m_sourceDescriptions = getSourceDescriptions(m_root);
    prepareSources(m_root);
    setDefaultLocaleDuringRefresh();
    renameKeysOnVariableNameChange();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast based operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If user chosen {@link Locale}, then set it as default during refresh.
   */
  private void setDefaultLocaleDuringRefresh() {
    m_root.addBroadcastListener(new ObjectEventListener() {
      private Locale m_oldDefaultLocale;

      @Override
      public void refreshBeforeCreate() throws Exception {
        LocaleInfo localeInfo = AbstractSource.getLocaleInfo(m_root);
        if (localeInfo != null) {
          Locale locale = localeInfo.getLocale();
          if (locale != null) {
            m_oldDefaultLocale = Locale.getDefault();
            Locale.setDefault(locale);
          }
        }
      }

      @Override
      public void refreshFinallyRefresh() {
        if (m_oldDefaultLocale != null) {
          Locale.setDefault(m_oldDefaultLocale);
          m_oldDefaultLocale = null;
        }
      }
    });
  }

  /**
   * When user renames component, we should rename associated NLS keys.
   */
  private void renameKeysOnVariableNameChange() {
    m_root.addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_setName(AbstractNamedVariableSupport variableSupport,
          String oldName,
          String newName) throws Exception {
        IPreferenceStore preferences = m_root.getDescription().getToolkit().getPreferences();
        if (oldName != null
            && newName != null
            && preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_RENAME_WITH_VARIABLE)) {
          IEditableSupport editable = getEditable();
          for (AbstractSource source : getSources()) {
            IKeyRenameStrategy keyRenameStrategy = source.getKeyRenameStrategy();
            IEditableSource editableSource = editable.getEditableSource(source);
            Set<String> keys = source.getKeys();
            for (String oldKey : keys) {
              String newKey = keyRenameStrategy.getNewKey(oldName, newName, oldKey);
              if (!keys.contains(newKey)) {
                editableSource.renameKey(oldKey, newKey);
              }
            }
          }
          applyEditable(editable);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Property} is a string property.
   */
  public static boolean isStringProperty(Property property) {
    return property instanceof GenericProperty
        && property.getEditor() instanceof StringPropertyEditor;
  }

  /**
   * Performs automatic externalize, if applicable and enabled.
   */
  public static void autoExternalize(GenericProperty property) throws Exception {
    if (isStringProperty(property)) {
      JavaInfo javaInfo = property.getJavaInfo();
      IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
      if (preferences.getBoolean(IPreferenceConstants.P_NLS_AUTO_EXTERNALIZE)) {
        NlsSupport support = NlsSupport.get(javaInfo);
        if (support.getSources() != null) {
          support.externalize(javaInfo, property);
        }
      }
    }
  }

  /**
   * Visits all components starting from given one and creates {@link AbstractSource} for each
   * expression of string {@link Property}.
   */
  private void prepareSources(JavaInfo component) throws Exception {
    // analyze properties of current component
    Property[] properties = component.getProperties();
    for (Property property : properties) {
      if (NlsSupport.isStringProperty(property)) {
        GenericProperty stringProperty = (GenericProperty) property;
        Expression expression = stringProperty.getExpression();
        // if property has expression, try to find NLS source
        if (expression != null) {
          for (SourceDescription sourceDescription : m_sourceDescriptions) {
            // try to find source
            try {
              AbstractSource source =
                  sourceDescription.getSource(component, stringProperty, expression, m_sources);
              if (source != null) {
                // save source in expression
                setSource(expression, source);
                // add new source
                if (!m_sources.contains(source)) {
                  m_sources.add(source);
                }
                // OK, we found source
                break;
              }
            } catch (Throwable e) {
              markAsBadExpression(expression);
            }
          }
        }
      }
    }
    // visit children
    for (JavaInfo child : component.getChildrenJava()) {
      prepareSources(child);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root component.
   */
  public JavaInfo getRoot() {
    return m_root;
  }

  /**
   * @return the array of all {@link AbstractSource}'s in this this {@link NlsSupport}.
   */
  public AbstractSource[] getSources() {
    return m_sources.toArray(new AbstractSource[m_sources.size()]);
  }

  /**
   * @return <code>true</code> if given {@link Expression} is externalized.
   */
  public boolean isExternalized(Expression expression) {
    return expression != null && getSource(expression) != null;
  }

  /**
   * @return the {@link AbstractSource} that has value for given key.
   */
  public AbstractSource getKeySource(String key) throws Exception {
    for (AbstractSource source : getSources()) {
      Set<String> keys = source.getKeys();
      if (keys.contains(key)) {
        return source;
      }
    }
    return null;
  }

  /**
   * @return the {@link String} value of given {@link Expression}.
   */
  public String getValue(Expression expression) throws Exception {
    AbstractSource source = getSource(expression);
    if (source != null) {
      return source.getValue(expression);
    }
    return null;
  }

  /**
   * Changes value of given externalized {@link Expression} using its {@link AbstractSource}.
   */
  public void setValue(Expression expression, String value) throws Exception {
    AbstractSource source = getSource(expression);
    source.setValue(expression, value);
  }

  /**
   * Externalizes given {@link GenericProperty}. We use this method for auto-externalizing new
   * properties.
   */
  public void externalize(JavaInfo component, GenericProperty property) throws Exception {
    // if we don't have sources, we can not do auto-externalizing
    if (!m_sources.isEmpty()) {
      // prepare source
      AbstractSource source = m_sources.get(0);
      String value = (String) property.getValue();
      // change code to use externalized value
      source.externalize(component, property, value);
      // remember externalized value (for example in *.properties file)
      source.setValue(property.getExpression(), value);
    }
  }

  /**
   * @return <code>true</code> if NLS strings in one or more {@link AbstractSource}'s were
   *         externally changed. For example, if user changed .properties file in other editor.
   */
  public boolean isExternallyChanged() throws Exception {
    for (AbstractSource source : m_sources) {
      if (source.isExternallyChanged()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the array of all {@link LocaleInfo}'s in all source.
   */
  public LocaleInfo[] getLocales() throws Exception {
    Set<LocaleInfo> locales = Sets.newHashSet();
    for (AbstractSource source : m_sources) {
      Collections.addAll(locales, source.getLocales());
    }
    addAlwaysVisibleLocales(locales);
    return locales.toArray(new LocaleInfo[locales.size()]);
  }

  /**
   * Adds {@link LocaleInfo} which should be always displayed to user.
   */
  private void addAlwaysVisibleLocales(Set<LocaleInfo> locales) {
    IPreferenceStore preferences = m_root.getDescription().getToolkit().getPreferences();
    String localesString = preferences.getString(IPreferenceConstants.P_NLS_ALWAYS_VISIBLE_LOCALES);
    for (String localeString : StringUtils.split(localesString, ", ")) {
      try {
        LocaleInfo locale = LocaleInfo.create(localeString, localeString);
        locales.add(locale);
      } catch (Throwable e) {
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new {@link IEditableSupport} for editing this {@link NlsSupport}.
   */
  public IEditableSupport getEditable() throws Exception {
    return new EditableSupport(this);
  }

  /**
   * Applies changes in given {@link IEditableSupport} into this {@link NlsSupport} - executes
   * prepared {@link AbstractCommand}'s.
   */
  public void applyEditable(IEditableSupport editableSupport) throws Exception {
    EditableSupport editable = (EditableSupport) editableSupport;
    // apply commands
    List<AbstractCommand> commands = editable.getCommands();
    for (AbstractCommand command : commands) {
      IEditableSource editableSource = command.getEditableSource();
      // create source
      if (command instanceof CreateSourceCommand) {
        // create new source
        CreateSourceCommand cmd = (CreateSourceCommand) command;
        AbstractSource newSource =
            cmd.getSourceDescription().createNewSource(editableSource, m_root, cmd.getParameters());
        // add new source in containers
        editable.addEditableToSourceMapping(editableSource, newSource);
        m_sources.add(newSource);
        // continue, because other commands require source, but we don't need it already
        continue;
      }
      // prepare source for editable source
      AbstractSource source = getAttachedSource(editable, editableSource);
      // set values
      if (command instanceof SetValuesCommand) {
        SetValuesCommand cmd = (SetValuesCommand) command;
        source.apply_setValues(cmd.getLocale(), cmd.getValues());
      }
      // rename key
      if (command instanceof RenameKeyCommand) {
        RenameKeyCommand cmd = (RenameKeyCommand) command;
        source.apply_renameKeys(cmd.getOldToNewMap());
      }
      // add key
      if (command instanceof AddKeyCommand) {
        AddKeyCommand cmd = (AddKeyCommand) command;
        source.apply_addKey(cmd.getKey());
      }
      // externalize property
      if (command instanceof ExternalizePropertyCommand) {
        ExternalizePropertyCommand cmd = (ExternalizePropertyCommand) command;
        source.apply_externalizeProperty(cmd.getProperty(), cmd.getKey());
      }
      // internalize keys
      if (command instanceof InternalizeKeyCommand) {
        InternalizeKeyCommand cmd = (InternalizeKeyCommand) command;
        source.apply_internalizeKeys(cmd.getKeys());
      }
      // add locale
      if (command instanceof AddLocaleCommand) {
        AddLocaleCommand cmd = (AddLocaleCommand) command;
        source.apply_addLocale(cmd.getLocale(), cmd.getValues());
      }
      // remove locale
      if (command instanceof RemoveLocaleCommand) {
        RemoveLocaleCommand cmd = (RemoveLocaleCommand) command;
        source.apply_removeLocale(cmd.getLocale());
      }
    }
    // save, because many NLS operations change compilation unit and external files,
    // so we should save them to keep consistent state
    JavaInfoUtils.scheduleSave(m_root);
  }

  /**
   * @return the {@link AbstractSource} that corresponds to {@link IEditableSource}. It ensures also
   *         that if this {@link IEditableSource} does not exists/attached in this compilation unit,
   *         it becomes attached.
   */
  public AbstractSource getAttachedSource(IEditableSupport editable, IEditableSource editableSource)
      throws Exception {
    EditableSupport editableImpl = (EditableSupport) editable;
    // first try to get "real" source
    AbstractSource source = editableImpl.getSource(editableSource);
    // if there are no "real" source, try "possible" one
    if (source == null) {
      source = editableImpl.getPossibleSource(editableSource);
      source.attachPossible();
      // add new source in containers
      editableImpl.addEditableToSourceMapping(editableSource, source);
      m_sources.add(source);
    }
    return source;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression source access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String NLS_SOURCE = "NLS_SOURCE";

  /**
   * Mark given {@link Expression} as externalized using given {@link AbstractSource}.
   */
  public static void setSource(Expression expression, AbstractSource source) {
    expression.setProperty(NLS_SOURCE, source);
  }

  /**
   * @return the {@link AbstractSource} for given {@link Expression}.
   */
  public static AbstractSource getSource(Expression expression) {
    return (AbstractSource) expression.getProperty(NLS_SOURCE);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bad expression
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String NLS_BAD_EXPRESSION = "NLS_BAD_EXPRESSION";

  /**
   * Mark given {@link Expression} is bad, i.e. when we tried to create source, we found exception.
   */
  private static void markAsBadExpression(Expression expression) {
    expression.setProperty(NLS_BAD_EXPRESSION, expression);
  }

  /**
   * @return <code>true</code> if given {@link Expression} is bad, i.e. when we tried to create
   *         source, we found exception.
   */
  public static boolean isBadExpression(Expression expression) {
    return expression.getProperty(NLS_BAD_EXPRESSION) != null;
  }
}
