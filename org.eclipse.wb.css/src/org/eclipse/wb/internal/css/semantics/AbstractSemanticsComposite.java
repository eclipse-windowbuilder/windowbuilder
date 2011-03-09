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
package org.eclipse.wb.internal.css.semantics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssFactory;
import org.eclipse.wb.internal.css.model.CssRuleNode;

import java.util.List;
import java.util.Map;

/**
 * Abstract object with several {@link AbstractValue}'s or {@link AbstractSemanticsComposite}'s.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public abstract class AbstractSemanticsComposite implements IValueEventsProvider {
  private final List<AbstractSemanticsComposite> m_childComposites = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSemanticsComposite() {
  }

  public AbstractSemanticsComposite(AbstractSemanticsComposite composite) {
    composite.addValueEventsProvider(this);
    composite.m_childComposites.add(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueEventsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IValueEventsProvider> m_valueEventsProviders = Lists.newArrayList();

  protected final void addValueEventsProvider(IValueEventsProvider provider) {
    m_valueEventsProviders.add(provider);
  }

  public final void addListener(IValueListener listener) {
    for (IValueEventsProvider provider : m_valueEventsProviders) {
      provider.addListener(listener);
    }
  }

  public final void removeListener(IValueListener listener) {
    for (IValueEventsProvider provider : m_valueEventsProviders) {
      provider.removeListener(listener);
    }
  }

  public void clear() {
    for (IValueEventsProvider provider : m_valueEventsProviders) {
      provider.clear();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  public void parse(CssRuleNode rule) {
    parseDeclarations(rule);
    for (AbstractSemanticsComposite composite : m_childComposites) {
      composite.parse(rule);
    }
  }

  public void update(CssRuleNode rule) {
    updateMappedValues(rule);
    for (AbstractSemanticsComposite composite : m_childComposites) {
      composite.update(rule);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, AbstractValue> m_valuesMap;

  /**
   * Creates new {@link SimpleValue} as holder of property value with given name.
   */
  protected static final SimpleValue mapSimpleProperty(AbstractSemanticsComposite composite,
      String property) {
    SimpleValue value = new SimpleValue(composite);
    mapProperty(composite, property, value);
    return value;
  }

  /**
   * Creates new {@link LengthValue} as holder of property value with given name.
   */
  protected static final LengthValue mapLengthProperty(AbstractSemanticsComposite composite,
      String property) {
    LengthValue value = new LengthValue(composite);
    mapProperty(composite, property, value);
    return value;
  }

  /**
   * Adds given {@link AbstractValue} as holder of property value with given name.
   */
  private static void mapProperty(AbstractSemanticsComposite composite,
      String property,
      AbstractValue value) {
    if (composite.m_valuesMap == null) {
      composite.m_valuesMap = Maps.newHashMap();
    }
    composite.m_valuesMap.put(property, value);
  }

  /**
   * Sends individual {@link CssDeclarationNode}'s of given rule to
   * {@link #parseDeclaration(String, String)}.
   */
  protected final void parseDeclarations(CssRuleNode rule) {
    if (m_valuesMap != null) {
      for (AbstractValue value : m_valuesMap.values()) {
        value.set(null);
      }
    }
    for (CssDeclarationNode declaration : rule.getDeclarations()) {
      String property = declaration.getProperty().getValue();
      String value = declaration.getValue().getValue();
      parseDeclaration(property, value);
    }
  }

  /**
   * Subclasses should override this method to handle individual {@link CssDeclarationNode}.
   */
  protected void parseDeclaration(String property, String value) {
    if (m_valuesMap != null) {
      AbstractValue abstractValue = m_valuesMap.get(property);
      if (abstractValue != null) {
        abstractValue.set(value);
      }
    }
  }

  /**
   * Creates {@link CssDeclarationNode}'s for values inside of mapped {@link AbstractValue}'s.
   */
  protected final void updateMappedValues(CssRuleNode rule) {
    removeDeclarations(rule, new ICssDeclarationPredicate() {
      public boolean evaluate(CssDeclarationNode declaration) {
        String property = declaration.getProperty().getValue();
        return m_valuesMap.containsKey(property);
      }
    });
    for (Map.Entry<String, AbstractValue> entry : m_valuesMap.entrySet()) {
      String property = entry.getKey();
      AbstractValue value = entry.getValue();
      String source = value.get();
      if (source != null) {
        addDeclaration(rule, property, null, source);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Predicate for analyzing {@link CssDeclarationNode}
   */
  protected interface ICssDeclarationPredicate {
    boolean evaluate(CssDeclarationNode declaration);
  }

  /**
   * Removes {@link CssDeclarationNode}'s that satisfy to the given predicate.
   */
  protected static final void removeDeclarations(CssRuleNode rule,
      ICssDeclarationPredicate predicate) {
    List<CssDeclarationNode> removeDeclarations = Lists.newArrayList();
    for (CssDeclarationNode declaration : rule.getDeclarations()) {
      if (predicate.evaluate(declaration)) {
        removeDeclarations.add(declaration);
      }
    }
    for (CssDeclarationNode declaration : removeDeclarations) {
      declaration.remove();
    }
  }

  /**
   * Removes {@link CssDeclarationNode}'s with given property name.
   */
  protected static final void removeDeclarationsByName(CssRuleNode rule, final String property) {
    removeDeclarations(rule, new ICssDeclarationPredicate() {
      public boolean evaluate(CssDeclarationNode declaration) {
        return declaration.getProperty().getValue().equals(property);
      }
    });
  }

  /**
   * Adds {@link CssDeclarationNode} with given property and value.
   */
  protected static final void addDeclaration(CssRuleNode rule,
      String prefix,
      String suffix,
      String value) {
    String property = suffix != null ? prefix + "-" + suffix : prefix;
    rule.addDeclaration(CssFactory.newDeclaration(property, value));
  }
}
