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
package org.eclipse.wb.internal.core.nls.edit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.commands.AbstractCommand;
import org.eclipse.wb.internal.core.nls.commands.CreateSourceCommand;
import org.eclipse.wb.internal.core.nls.commands.ICommandQueue;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.INlsPropertyContributor;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation for editable NLS support.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class EditableSupport implements IEditableSupport, ICommandQueue {
  private final JavaInfo m_root;
  private final List<IEditableSource> m_newEditableSources = Lists.newArrayList();
  private final Map<AbstractSource, IEditableSource> m_sourceToEditable = Maps.newHashMap();
  private final Map<IEditableSource, AbstractSource> m_editableToSource = Maps.newHashMap();
  private final MultiKeyMap/*<IEditableSource, String, List<StringPropertyInfo>>*/m_externalizedProperties =
      new MultiKeyMap();
  private final Map<JavaInfo, List<StringPropertyInfo>> m_componentToPropertyList =
      Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditableSupport(NlsSupport support) throws Exception {
    m_root = support.getRoot();
    // prepare sources
    AbstractSource[] sources = support.getSources();
    for (int i = 0; i < sources.length; i++) {
      AbstractSource source = sources[i];
      // prepare editable source
      IEditableSource editableSource = source.getEditable();
      hookEditableSource(editableSource);
      // fill mapping
      addEditableToSourceMapping(editableSource, source);
    }
    // initialize containers
    initializeProperties(support, support.getRoot());
    initializePossibleSources();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editable sources listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addEditableSourceListener(final IEditableSource editableSource) {
    editableSource.addListener(new IEditableSourceListener() {
      public void keyAdded(String key, Object o) {
        // add externalized property with current source and given key
        StringPropertyInfo propertyInfo = (StringPropertyInfo) o;
        m_externalizedProperties.put(editableSource, key, propertyInfo);
        // notify listeners
        fire_externalizedPropertiesChanged();
      }

      public void keyRemoved(String key) {
        // remove externalized properties with same source and key
        m_externalizedProperties.remove(editableSource, key);
        // notify listeners
        fire_externalizedPropertiesChanged();
      }

      public void keyRenamed(String oldKey, String newKey) {
        Object propertyInfo = m_externalizedProperties.remove(editableSource, oldKey);
        if (propertyInfo != null) {
          m_externalizedProperties.put(editableSource, newKey, propertyInfo);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization: properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Fill properties containers.
   */
  private void initializeProperties(NlsSupport support, JavaInfo component) throws Exception {
    // add list of properties for current component
    {
      List<StringPropertyInfo> componentProperties = Lists.newArrayList();
      m_componentToPropertyList.put(component, componentProperties);
      // prepare list of properties
      List<Property> properties = Lists.newArrayList(component.getProperties());
      for (Property property : ImmutableList.copyOf(properties)) {
        PropertyEditor editor = property.getEditor();
        if (editor instanceof INlsPropertyContributor) {
          ((INlsPropertyContributor) editor).contributeNlsProperties(property, properties);
        }
      }
      // fill String properties
      for (Property property : properties) {
        if (NlsSupport.isStringProperty(property) && property.isModified()) {
          GenericProperty stringProperty = (GenericProperty) property;
          StringPropertyInfo propertyInfo = new StringPropertyInfo(stringProperty);
          Expression expression = stringProperty.getExpression();
          if (expression != null) {
            if (expression instanceof StringLiteral) {
              componentProperties.add(propertyInfo);
            } else if (support.isExternalized(expression)) {
              // in any case add in externalizable properties
              componentProperties.add(propertyInfo);
              // prepare source and key
              AbstractSource source = NlsSupport.getSource(expression);
              IEditableSource editableSource = getEditableSource(source);
              String key = source.getKey(expression);
              // add externalized info
              m_externalizedProperties.put(editableSource, key, propertyInfo);
            }
          }
        }
      }
    }
    // process children
    for (JavaInfo child : getTreeChildren(component)) {
      initializeProperties(support, child);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  // "Possible" source is source that exists in current package, but is not used
  // in current unit. We show "possible" sources only if there are no "real" sources.
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<AbstractSource, IEditableSource> m_possibleSourceToEditableSource =
      Maps.newHashMap();
  private final Map<IEditableSource, AbstractSource> m_possibleEditableSourceToSource =
      Maps.newHashMap();

  /**
   * Fill possible sources containers.
   */
  private void initializePossibleSources() throws Exception {
    IPackageFragment pkg = (IPackageFragment) m_root.getEditor().getModelUnit().getParent();
    SourceDescription[] sourceDescriptions = NlsSupport.getSourceDescriptions(m_root);
    for (int i = 0; i < sourceDescriptions.length; i++) {
      SourceDescription sourceDescription = sourceDescriptions[i];
      List<AbstractSource> sources = sourceDescription.getPossibleSources(m_root, pkg);
      // fill maps for source <-> editable source
      for (AbstractSource source : sources) {
        // prepare editable source
        IEditableSource editableSource = source.getEditable();
        hookEditableSource(editableSource);
        // fill mapping
        m_possibleSourceToEditableSource.put(source, editableSource);
        m_possibleEditableSourceToSource.put(editableSource, source);
      }
    }
  }

  /**
   * @return the {@link List} of possible {@link IEditableSource}'s.
   */
  private List<IEditableSource> getPossibleEditableSources() {
    return new ArrayList<IEditableSource>(m_possibleEditableSourceToSource.keySet());
  }

  /**
   * @return "possible" {@link AbstractSource} for given {@link IEditableSource}.
   */
  public AbstractSource getPossibleSource(IEditableSource editableSource) {
    return m_possibleEditableSourceToSource.get(editableSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSupport: listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IEditableSupportListener> m_listeners = Lists.newArrayList();

  public void addListener(IEditableSupportListener listener) {
    if (!m_listeners.contains(listener)) {
      m_listeners.add(listener);
    }
  }

  public void removeListener(IEditableSupportListener listener) {
    m_listeners.remove(listener);
  }

  private void fire_sourceAdded(IEditableSource source) {
    for (IEditableSupportListener listener : m_listeners) {
      listener.sourceAdded(source);
    }
  }

  private void fire_externalizedPropertiesChanged() {
    for (IEditableSupportListener listener : m_listeners) {
      listener.externalizedPropertiesChanged();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSupport: access
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo getRoot() {
    return m_root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSupport: sources
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasExistingSources() {
    return !m_sourceToEditable.isEmpty();
  }

  public List<IEditableSource> getEditableSources() {
    List<IEditableSource> editableSources = Lists.newArrayList();
    // add existing and new sources
    editableSources.addAll(m_sourceToEditable.values());
    editableSources.addAll(m_newEditableSources);
    // if we have "real" editable sources, return them
    if (!editableSources.isEmpty()) {
      return editableSources;
    }
    // return "possible" editable sources
    return getPossibleEditableSources();
  }

  public void addSource(IEditableSource editableSource,
      SourceDescription sourceDescription,
      Object parameters) {
    CreateSourceCommand command =
        new CreateSourceCommand(editableSource, sourceDescription, parameters);
    addCommand(command);
    // configure new editable source
    m_newEditableSources.add(editableSource);
    editableSource.setCommandQueue(this);
    // configure listeners
    addEditableSourceListener(editableSource);
    fire_sourceAdded(editableSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources: access
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSource getSource(IEditableSource editableSource) {
    return m_editableToSource.get(editableSource);
  }

  /**
   * Register new mapping from {@link IEditableSource} to {@link AbstractSource}.<br>
   * We use this method in "new source" command.
   */
  public void addEditableToSourceMapping(IEditableSource editableSource, AbstractSource source) {
    m_sourceToEditable.put(source, editableSource);
    m_editableToSource.put(editableSource, source);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources: private
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IEditableSource} for given {@link AbstractSource}.
   *
   *         We can not just ask {@link IEditableSource} from {@link AbstractSource} because it
   *         creates new {@link IEditableSource} each time, but we want use only one editable for
   *         each source during single editing session.
   */
  public IEditableSource getEditableSource(AbstractSource source) {
    return m_sourceToEditable.get(source);
  }

  /**
   * Add hooks for given {@link IEditableSource} . Right now this is: set command queue and add
   * listener.
   */
  private void hookEditableSource(IEditableSource editableSource) {
    editableSource.setCommandQueue(this);
    addEditableSourceListener(editableSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditableSupport: properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<JavaInfo> getComponents() {
    return new ArrayList<JavaInfo>(m_componentToPropertyList.keySet());
  }

  public List<JavaInfo> getTreeChildren(JavaInfo component) throws Exception {
    List<JavaInfo> selectedTreeChildren = Lists.newArrayList();
    for (ObjectInfo child : component.getPresentation().getChildrenTree()) {
      if (child instanceof JavaInfo) {
        // check if child can be visited
        /*{
         INLSTreeProvider treeProvider = (INLSTreeProvider) child.getAdapter(INLSTreeProvider.class);
         if (treeProvider != null && !treeProvider.shouldVisit()) {
         continue;
         }
         }*/
        // add child
        selectedTreeChildren.add((JavaInfo) child);
      }
    }
    return selectedTreeChildren;
  }

  public List<StringPropertyInfo> getProperties(JavaInfo component) {
    List<StringPropertyInfo> externalizableProperties = Lists.newArrayList();
    // prepare all properties of component
    List<StringPropertyInfo> componentProperties = m_componentToPropertyList.get(component);
    externalizableProperties.addAll(componentProperties);
    // remove already externalized properties
    for (MapIterator I = m_externalizedProperties.mapIterator(); I.hasNext();) {
      I.next();
      StringPropertyInfo propertyInfo = (StringPropertyInfo) I.getValue();
      externalizableProperties.remove(propertyInfo);
    }
    // return rest properties
    return externalizableProperties;
  }

  public boolean hasPropertiesInTree(JavaInfo component) throws Exception {
    // check current component
    if (!getProperties(component).isEmpty()) {
      return true;
    }
    // check children
    for (JavaInfo child : getTreeChildren(component)) {
      if (hasPropertiesInTree(child)) {
        return true;
      }
    }
    // no, we don't have properties
    return false;
  }

  public void externalizeProperty(StringPropertyInfo propertyInfo,
      IEditableSource editableSource,
      boolean copyToAllLocales) {
    editableSource.externalize(propertyInfo, copyToAllLocales);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICommandQueue
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<AbstractCommand> m_commands = Lists.newArrayList();

  public void addCommand(AbstractCommand command) {
    command.addToCommandList(m_commands);
  }

  /**
   * @return the {@link List} of {@link AbstractCommand}'s that should be executed to complete
   *         editing.
   */
  public List<AbstractCommand> getCommands() {
    return m_commands;
  }
}
