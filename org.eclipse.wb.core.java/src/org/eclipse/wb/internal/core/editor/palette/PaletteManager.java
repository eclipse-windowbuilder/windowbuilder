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
package org.eclipse.wb.internal.core.editor.palette;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryEditCommand;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentEditCommand;
import org.eclipse.wb.internal.core.editor.palette.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryEditCommand;
import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProvider;
import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProviders;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.Pair;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Manager for {@link PaletteInfo} for some GUI toolkit.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class PaletteManager {
  private final JavaInfo m_rootJavaInfo;
  private final ClassLoader m_classLoader;
  private final IJavaProject m_javaProject;
  private final IProject m_project;
  private final String m_toolkitId;
  private PaletteInfo m_paletteInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PaletteManager(JavaInfo rootJavaInfo, String toolkitId) {
    m_rootJavaInfo = rootJavaInfo;
    m_classLoader = JavaInfoUtils.getClassLoader(m_rootJavaInfo);
    m_javaProject = rootJavaInfo.getEditor().getJavaProject();
    m_project = m_javaProject.getProject();
    m_toolkitId = toolkitId;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getToolkitId() {
    return m_toolkitId;
  }

  /**
   * @return the current palette.
   */
  public PaletteInfo getPalette() {
    return m_paletteInfo;
  }

  /**
   * @return the copy of current palette.
   */
  public PaletteInfo getPaletteCopy() {
    PaletteManager manager = new PaletteManager(m_rootJavaInfo, m_toolkitId);
    manager.reloadPalette();
    return manager.getPalette();
  }

  /**
   * Loads new base palette, applies commands.
   */
  public void reloadPalette() {
    m_paletteInfo = new PaletteInfo();
    if (m_toolkitId != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          parseExtensionPalette();
          parseCustomPalette();
          processReorderRequests();
          // apply commands
          commands_apply();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Import/export
  //
  ////////////////////////////////////////////////////////////////////////////
  public void exportTo(String path) throws Exception {
    File commandsFile = commands_getFile();
    FileUtils.copyFile(commandsFile, new File(path));
  }

  public void importFrom(String path) throws Exception {
    File commandsFile = commands_getFile();
    FileUtils.copyFile(new File(path), commandsFile);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final List<Class<? extends Command>> m_commandClasses = Lists.newArrayList();
  static {
    m_commandClasses.add(ElementVisibilityCommand.class);
    m_commandClasses.add(CategoryAddCommand.class);
    m_commandClasses.add(CategoryEditCommand.class);
    m_commandClasses.add(CategoryMoveCommand.class);
    m_commandClasses.add(CategoryRemoveCommand.class);
    m_commandClasses.add(EntryMoveCommand.class);
    m_commandClasses.add(EntryRemoveCommand.class);
    m_commandClasses.add(ComponentAddCommand.class);
    m_commandClasses.add(ComponentEditCommand.class);
    m_commandClasses.add(FactoryAddCommand.class);
    m_commandClasses.add(FactoryEditCommand.class);
  }
  private static final Map<String, Class<? extends Command>> m_idToCommandClass = Maps.newTreeMap();
  private List<Command> m_commands;

  /**
   * Applies commands for modifying palette.
   */
  private void commands_apply() throws Exception {
    // prepare mapping: id -> command class
    if (m_idToCommandClass.isEmpty()) {
      for (Class<? extends Command> commandClass : m_commandClasses) {
        String id = (String) commandClass.getField("ID").get(null);
        m_idToCommandClass.put(id, commandClass);
      }
    }
    // read commands
    commandsRead();
  }

  private void commandsRead() throws Exception {
    m_commands = Lists.newArrayList();
    // read-only "wbp-meta" from classpath, for example from jar's
    {
      String commandsPath = "wbp-meta/" + m_toolkitId + ".wbp-palette-commands.xml";
      Enumeration<URL> resources = m_classLoader.getResources(commandsPath);
      while (resources.hasMoreElements()) {
        InputStream inputStream = resources.nextElement().openStream();
        commandsRead_fromStream(inputStream);
      }
    }
    // from read-write file
    File commandsFile = commands_getFile();
    if (commandsFile.exists()) {
      FileInputStream inputStream = new FileInputStream(commandsFile);
      commandsRead_fromStream(inputStream);
    }
  }

  private void commandsRead_fromStream(final InputStream inputStream) throws Exception {
    try {
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          commandsRead_fromStream0(inputStream);
        }
      });
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private void commandsRead_fromStream0(InputStream inputStream) throws Exception {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    parser.parse(inputStream, new DefaultHandler() {
      @Override
      public void startElement(String uri,
          String localName,
          final String name,
          final Attributes attributes) {
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            commandsRead_singleCommand(name, attributes);
          }
        });
      }
    });
  }

  private void commandsRead_singleCommand(String name, Attributes attributes) throws Exception {
    // prepare command class
    Class<? extends Command> commandClass;
    {
      commandClass = m_idToCommandClass.get(name);
      if (commandClass == null) {
        return;
      }
    }
    // create command
    Command command;
    {
      Constructor<? extends Command> constructor =
          commandClass.getConstructor(new Class[]{Attributes.class});
      command = constructor.newInstance(new Object[]{attributes});
    }
    // add command
    commands_add(command);
  }

  /**
   * Removes all saved commands, so rollbacks palette into initial state.
   */
  public void commands_clear() {
    m_commands.clear();
  }

  /**
   * Adds given {@link Command} to the list (and executes it).
   */
  public void commands_add(final Command command) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        command.execute(m_paletteInfo);
        command.addToCommandList(m_commands);
      }
    });
  }

  /**
   * Stores current {@link Command}'s {@link List}.
   */
  public void commands_write() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        File commandsFile = commands_getFile();
        PrintWriter writer = new PrintWriter(new FileOutputStream(commandsFile));
        try {
          writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          writer.println("<commands>");
          // write separate commands
          for (Command command : m_commands) {
            writer.println(command.toString());
          }
          // close
          writer.println("</commands>");
        } finally {
          writer.close();
        }
        // we may be saved something in "wbp-meta", so refresh it
        m_project.getFolder("wbp-meta").refreshLocal(IResource.DEPTH_INFINITE, null);
      }
    });
  }

  /**
   * @return the {@link File} with {@link Command}'s for current GUI toolkit.
   */
  private File commands_getFile() {
    // check for palette commands file in project "wbp-meta"
    {
      Path commandsFilePath = new Path("wbp-meta/" + m_toolkitId + ".wbp-palette-commands.xml");
      IFile commandsFile = m_project.getFile(commandsFilePath);
      if (commandsFile.exists()) {
        return commandsFile.getLocation().toFile();
      }
    }
    // save commands in plugin state
    File stateDirectory = DesignerPlugin.getDefault().getStateLocation().toFile();
    File palettesDirectory = new File(stateDirectory, "palettes");
    if (!palettesDirectory.exists() && !palettesDirectory.mkdirs()) {
      throw new Error("Unable to create palettes directory.");
    }
    return new File(palettesDirectory, m_toolkitId + ".commands");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Extensions parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Set<Pair<String, String>> m_categoryReorderRequests = Sets.newHashSet();

  /**
   * Fills {@link #m_paletteInfo} using contributed extensions.
   */
  private void parseExtensionPalette() {
    // prepare <toolkit>/<palette> elements
    List<IConfigurationElement> paletteElements = Lists.newArrayList();
    for (IConfigurationElement toolkitElement : DescriptionHelper.getToolkitElements(m_toolkitId)) {
      for (IConfigurationElement paletteElement : toolkitElement.getChildren("palette")) {
        if (isConditionTrue(paletteElement)) {
          paletteElements.add(paletteElement);
        }
      }
    }
    // add all categories first, order of contributions is undetermined
    for (IConfigurationElement paletteElement : paletteElements) {
      IConfigurationElement[] categoryElements = paletteElement.getChildren("category");
      for (IConfigurationElement categoryElement : categoryElements) {
        if (isConditionTrue(categoryElement)) {
          processCategory(categoryElement);
        }
      }
    }
    // add other elements
    for (IConfigurationElement paletteElement : paletteElements) {
      processPaletteChildren(m_paletteInfo, null, paletteElement);
    }
  }

  /**
   * Process single category contribution.
   */
  private void processCategory(IConfigurationElement categoryElement) {
    AttributesProvider attributesProvider = AttributesProviders.get(categoryElement);
    // may be there was already category with such id
    CategoryInfo category;
    {
      String id = attributesProvider.getAttribute("id");
      category = m_paletteInfo.getCategory(id);
    }
    // new category, usual case
    if (category == null) {
      category = new CategoryInfo(attributesProvider);
      m_paletteInfo.addCategory(category);
      addReorderRequest(category, attributesProvider);
    }
    // process children
    processPaletteChildren(m_paletteInfo, category, categoryElement);
  }

  /**
   * Check if given {@link CategoryInfo} has "next" attribute to put it before some other category.
   */
  private void addReorderRequest(CategoryInfo categoryInfo, AttributesProvider attributesProvider) {
    String nextCategoryId = attributesProvider.getAttribute("next");
    if (nextCategoryId != null) {
      m_categoryReorderRequests.add(Pair.create(categoryInfo.getId(), nextCategoryId));
    }
  }

  /**
   * Executes delayed {@link #m_categoryReorderRequests}.
   */
  private void processReorderRequests() {
    for (Pair<String, String> reorder : m_categoryReorderRequests) {
      m_paletteInfo.moveCategory(reorder.getLeft(), reorder.getRight());
    }
  }

  /**
   * Process all contributions in given parent {@link IConfigurationElement} for "palette".
   */
  private void processPaletteChildren(PaletteInfo paletteInfo,
      CategoryInfo categoryInfo,
      IConfigurationElement parentElement) {
    for (IConfigurationElement element : parentElement.getChildren()) {
      if (!element.getName().equals("category")) {
        processCategoryChild(paletteInfo, categoryInfo, element);
      }
    }
  }

  /**
   * Process single child of category contribution.
   */
  private void processCategoryChild(final PaletteInfo paletteInfo,
      final CategoryInfo _categoryInfo,
      final IConfigurationElement element) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // prepare category
        CategoryInfo categoryInfo = _categoryInfo;
        if (categoryInfo == null) {
          String categoryId = element.getAttribute("category");
          Assert.isNotNull(
              categoryId,
              "Element defined outside of category, so requires 'category' attribute.");
          categoryInfo = paletteInfo.getCategory(categoryId);
          Assert.isNotNull(categoryInfo, "No category with id '" + categoryId + "' found.");
        }
        // generic entry
        if ("entry".equals(element.getName())) {
          EntryInfo entryInfo = (EntryInfo) element.createExecutableExtension("class");
          // set id
          {
            String id = element.getAttribute("id");
            if (id != null) {
              entryInfo.setId(id);
            }
          }
          // set optional name
          {
            String name = element.getAttribute("name");
            if (name != null) {
              entryInfo.setName(name);
            }
          }
          // add
          categoryInfo.addEntry(entryInfo);
        }
        // component entry
        if ("component".equals(element.getName())) {
          if (isConditionTrue(element)) {
            EntryInfo entryInfo = new ComponentEntryInfo(categoryInfo, element);
            categoryInfo.addEntry(entryInfo);
          }
        }
        // static factory
        if ("static-factory".equals(element.getName())) {
          String factoryClassName = ExternalFactoriesHelper.getRequiredAttribute(element, "class");
          for (IConfigurationElement methodElement : element.getChildren("method")) {
            AttributesProvider attributes = AttributesProviders.get(methodElement);
            StaticFactoryEntryInfo entryInfo =
                new StaticFactoryEntryInfo(categoryInfo, factoryClassName, attributes);
            categoryInfo.addEntry(entryInfo);
          }
        }
        // instance factory
        if ("instance-factory".equals(element.getName())) {
          String factoryClassName = ExternalFactoriesHelper.getRequiredAttribute(element, "class");
          for (IConfigurationElement methodElement : element.getChildren("method")) {
            AttributesProvider attributes = AttributesProviders.get(methodElement);
            InstanceFactoryEntryInfo entryInfo =
                new InstanceFactoryEntryInfo(categoryInfo, factoryClassName, attributes);
            categoryInfo.addEntry(entryInfo);
          }
        }
      }
    });
  }

  /**
   * @return <code>true</code> if "condition" attribute is empty or evaluates to <code>true</code>.
   */
  private boolean isConditionTrue(IConfigurationElement element) {
    String condition = element.getAttribute("condition");
    // no condition
    if (condition == null) {
      return true;
    }
    // evaluate condition
    Map<String, Object> variables = Maps.newHashMap();
    variables.put("rootModel", m_rootJavaInfo);
    variables.putAll(JavaInfoUtils.getState(m_rootJavaInfo).getVersions());
    Object result = ScriptUtils.evaluate(condition, variables);
    return result instanceof Boolean ? (Boolean) result : false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "wbp-meta" parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses custom palettes in "wbp-meta".
   */
  private void parseCustomPalette() throws Exception {
    String palettePath = "wbp-meta/" + m_toolkitId + ".wbp-palette.xml";
    // parse "wbp-meta" from classpath, for example from jar's
    {
      Enumeration<URL> resources = m_classLoader.getResources(palettePath);
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        parseCustomPalette(url.openStream(), url.toString());
      }
    }
    // parse "wbp-meta" from IJavaProject's
    {
      List<IFile> files = ProjectUtils.findFiles(m_javaProject, palettePath);
      for (IFile file : files) {
        String sourceDescription = file.toString();
        try {
          parseCustomPalette(file.getContents(true), sourceDescription);
        } catch (Throwable e) {
          List<EditorWarning> warnings = JavaInfoUtils.getState(m_rootJavaInfo).getWarnings();
          warnings.add(new EditorWarning("Can not parse " + sourceDescription, e));
        }
      }
    }
  }

  /**
   * Parses single custom palette contribution.
   *
   * @param inputStream
   *          the {@link InputStream} with palette XML.
   * @param sourceDescription
   *          the textual description to show for user, that can help in detecting palette XML in
   *          case of any error.
   */
  private void parseCustomPalette(InputStream inputStream, String sourceDescription)
      throws Exception {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(inputStream, new DefaultHandler() {
        private CategoryInfo m_category;
        private String m_factoryClassName;
        private boolean m_factoryStatic;

        @Override
        public void startElement(String uri, String localTag, String tag, Attributes attributes)
            throws SAXException {
          if ("category".equals(tag)) {
            AttributesProvider attributesProvider = AttributesProviders.get(attributes);
            // may be there is already category with this ID
            {
              String id = attributesProvider.getAttribute("id");
              m_category = m_paletteInfo.getCategory(id);
            }
            // new category, usual case
            if (m_category == null) {
              m_category = new CategoryInfo(attributesProvider);
              m_paletteInfo.addCategory(m_category);
              addReorderRequest(m_category, attributesProvider);
            }
          } else if ("component".equals(tag)) {
            CategoryInfo category = getTargetCategory(attributes);
            AttributesProvider attributesProvider = AttributesProviders.get(attributes);
            ComponentEntryInfo component = new ComponentEntryInfo(category, attributesProvider);
            category.addEntry(component);
          } else if ("static-factory".equals(tag)) {
            m_factoryClassName = getRequiredString(attributes, "class");
            m_factoryStatic = true;
          } else if ("instance-factory".equals(tag)) {
            m_factoryClassName = getRequiredString(attributes, "class");
            m_factoryStatic = false;
          } else if ("method".equals(tag)) {
            CategoryInfo category = getTargetCategory(attributes);
            FactoryEntryInfo entry = createFactoryEntry(category, attributes);
            category.addEntry(entry);
          }
        }

        private CategoryInfo getTargetCategory(Attributes attributes) {
          CategoryInfo category = m_category;
          if (category == null) {
            String categoryId = attributes.getValue("category");
            Assert.isNotNull(
                categoryId,
                "Element defined outside of category, so requires 'category' attribute.");
            category = m_paletteInfo.getCategory(categoryId);
            Assert.isNotNull(category, "No category with id '" + categoryId + "' found.");
          }
          return category;
        }

        private FactoryEntryInfo createFactoryEntry(CategoryInfo category, Attributes attributes) {
          Assert.isNotNull(m_factoryClassName, "Attempt to use 'method' "
              + "outside of <static-factory> or <instance-factory> tags.");
          AttributesProvider attributesProvider = AttributesProviders.get(attributes);
          if (m_factoryStatic) {
            return new StaticFactoryEntryInfo(category, m_factoryClassName, attributesProvider);
          } else {
            return new InstanceFactoryEntryInfo(category, m_factoryClassName, attributesProvider);
          }
        }

        @Override
        public void endElement(String uri, String localName, String tag) throws SAXException {
          if ("category".equals(tag)) {
            m_category = null;
          } else if ("static-factory".equals(tag) || "instance-factory".equals(tag)) {
            m_factoryClassName = null;
          }
        }

        /**
         * @return the value of attribute with given name, throws exception if no such attribute
         *         found.
         */
        private String getRequiredString(Attributes attributes, String name) {
          String value = attributes.getValue(name);
          Assert.isNotNull(value, "No value for attribute '" + name + "'.");
          return value;
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log("Exception during loading project palette: " + sourceDescription, e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }
}
