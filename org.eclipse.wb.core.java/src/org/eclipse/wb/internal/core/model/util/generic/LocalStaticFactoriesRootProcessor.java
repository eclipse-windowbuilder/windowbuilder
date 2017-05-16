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
package org.eclipse.wb.internal.core.model.util.generic;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProvider;
import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProviders;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import java.util.List;
import java.util.Map;

/**
 * Contributes palette {@link CategoryInfo} with name <code>"Local Factories"</code> to palette, so
 * allows to use them easily.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.util
 */
public final class LocalStaticFactoriesRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new LocalStaticFactoriesRootProcessor();

  private LocalStaticFactoriesRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    processRoot(root);
  }

  /**
   * Configures given parent to copy properties of children according parameters in description.
   */
  private static void processRoot(final JavaInfo rootJavaInfo) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        installPaletteBroadcastEx(rootJavaInfo);
      }
    });
  }

  private static void installPaletteBroadcastEx(JavaInfo rootJavaInfo) throws Exception {
    final Map<String, FactoryMethodDescription> descriptionsMap = getLocalFactories(rootJavaInfo);
    if (!descriptionsMap.isEmpty()) {
      rootJavaInfo.addBroadcastListener(new PaletteEventListener() {
        @Override
        public void categories(List<CategoryInfo> categories) throws Exception {
          CategoryInfo category = createLocalFactoriesCategory(descriptionsMap);
          categories.add(category);
        }
      });
    }
  }

  /**
   * @return the {@link Map} of local factories, may be empty, but not <code>null</code>.
   */
  private static Map<String, FactoryMethodDescription> getLocalFactories(JavaInfo rootJavaInfo)
      throws Exception {
    AstEditor editor = rootJavaInfo.getEditor();
    // quick check
    if (!editor.getSource().contains("@wbp.factory")) {
      return ImmutableMap.of();
    }
    // analyze
    String editorTypeName = editor.getModelUnit().findPrimaryType().getFullyQualifiedName();
    Class<?> possibleFactoryClass =
        JavaInfoUtils.getClassLoader(rootJavaInfo).loadClass(editorTypeName);
    return FactoryDescriptionHelper.getDescriptionsMap(editor, possibleFactoryClass, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  private static CategoryInfo createLocalFactoriesCategory(Map<String, FactoryMethodDescription> descriptionsMap) {
    CategoryInfo category = new CategoryInfo("wbp.Core.localFactories");
    category.setName("Local Factories");
    category.setDescription("Automatically added category with entries for local factory methods.");
    // add entries
    for (FactoryMethodDescription methodDescription : descriptionsMap.values()) {
      String factoryClassName = methodDescription.getDeclaringClass().getName();
      AttributesProvider attributes =
          AttributesProviders.get(ImmutableMap.of("signature", methodDescription.getSignature()));
      StaticFactoryEntryInfo entry =
          new StaticFactoryEntryInfo(category, factoryClassName, attributes);
      category.addEntry(entry);
    }
    //
    return category;
  }
}
