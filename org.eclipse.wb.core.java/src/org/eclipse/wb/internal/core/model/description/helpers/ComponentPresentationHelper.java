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
package org.eclipse.wb.internal.core.model.description.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper for accessing {@link ComponentPresentation}'s.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.model.description
 */
public final class ComponentPresentationHelper {
  public static final String PALETTE_PRELOAD_JOBS = "palette pre-load";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComponentPresentationHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, ComponentPresentationCache> m_presentationCaches =
      Maps.newTreeMap();

  /**
   * @return the {@link ComponentPresentation} for component with given {@link Class}.
   */
  public static ComponentPresentation getPresentation(AstEditor editor,
      String componentClassName,
      String creationId) throws Exception {
    String key = getKey(componentClassName, creationId);
    // try to find cached presentation
    ComponentPresentation presentation = null;
    {
      ComponentPresentationCache cache = getCache(editor);
      presentation = cache.get(key);
      if (presentation != null) {
        return presentation;
      }
    }
    // load component Class
    Class<?> componentClass;
    {
      ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
      componentClass = classLoader.loadClass(componentClassName);
    }
    // try to load "fast" presentation
    {
      presentation = getFastPresentation(editor, componentClass, creationId);
      if (presentation != null) {
        return presentation;
      }
    }
    // fall back to slow implementation
    return getSlowPresentation(editor, componentClass, key);
  }

  private static ComponentPresentation getFastPresentation(AstEditor editor,
      Class<?> componentClass,
      String creationId) throws Exception {
    EditorState state = EditorState.get(editor);
    ILoadingContext context = EditorStateLoadingContext.get(state);
    String componentClassName = componentClass.getName();
    ResourceInfo resource =
        DescriptionHelper.getComponentDescriptionResource(context, componentClass);
    if (resource != null) {
      // parse name and descriptions
      ComponentParseHelper parseHelper = parseResource(resource.getURL());
      // done, create presentation
      String desc = parseHelper.getDescription(creationId);
      String name = parseHelper.getName(componentClassName, creationId);
      String key = getKey(componentClassName, creationId);
      String toolkitId = getToolkitId(state, resource);
      Image icon = getComponentImage(componentClass, creationId, context);
      ComponentPresentation presentation =
          new ComponentPresentation(key, toolkitId, name, desc, icon);
      if (shouldCacheFast(resource.getBundle())) {
        ComponentPresentationCache cache = getCache(editor);
        cache.put(presentation);
      }
      return presentation;
    }
    return null;
  }

  private static String getToolkitId(EditorState state, ResourceInfo resource) {
    ToolkitDescription toolkit = resource.getToolkit();
    return toolkit == null ? state.getToolkitId() : toolkit.getId();
  }

  private static boolean shouldCacheFast(Bundle bundle) {
    if (bundle != null) {
      return bundle.getEntry("wbp-meta/.wbp-cache-presentations") != null;
    }
    return false;
  }

  private static Image getComponentImage(Class<?> clazz, String creationId, ILoadingContext context)
      throws Exception {
    String iconPath = getImageName(clazz.getName(), creationId);
    Image image = DescriptionHelper.getIconImage(context, iconPath);
    if (image == null) {
      // no image for this type, use super type
      return getComponentImage(clazz.getSuperclass(), null/*use default id*/, context);
    }
    return image;
  }

  private static byte[] getComponentImage(Bundle bundle,
      String componentClassName,
      String creationId) throws Exception {
    String iconPath = "/wbp-meta/" + getImageName(componentClassName, creationId);
    for (String ext : DescriptionHelper.ICON_EXTS) {
      String iconName = iconPath + ext;
      URL entry = bundle.getEntry(iconName);
      if (entry != null) {
        InputStream stream = entry.openStream();
        return IOUtils2.readBytes(stream);
      }
    }
    // not found
    return null;
  }

  private static String getImageName(String componentClassName, String creationId) {
    String imageName = componentClassName.replace('.', '/');
    return creationId == null ? imageName : imageName + "_" + creationId;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Slow way
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ComponentPresentation getSlowPresentation(AstEditor editor,
      Class<?> componentClass,
      String requiredKey) throws Exception {
    ComponentPresentation presentationForKey = null;
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(editor, componentClass);
    for (CreationDescription creationDescription : componentDescription.getCreations()) {
      ComponentPresentation presentation =
          createPresentation(componentDescription, creationDescription);
      // cache if allowed
      if (componentDescription.isPresentationCached()) {
        ComponentPresentationCache cache = getCache(editor);
        cache.put(presentation);
      }
      // check for required presentation
      if (requiredKey.equals(presentation.getKey())) {
        presentationForKey = presentation;
      }
    }
    // done
    return presentationForKey;
  }

  private static ComponentPresentation createPresentation(ComponentDescription componentDescription,
      CreationDescription creationDescription) {
    String componentKey = componentDescription.getComponentClass().getName();
    String key = componentKey + " " + creationDescription.getId();
    String toolkitId = componentDescription.getToolkit().getId();
    String name = creationDescription.getName();
    String description = creationDescription.getDescription();
    Image icon = UiUtils.getCopy(creationDescription.getIcon());
    return new ComponentPresentation(key, toolkitId, name, description, icon);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing component-related resources
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ComponentParseHelper parseResource(URL url) throws Exception {
    String xml = IOUtils2.readString(url.openStream());
    ComponentParseHelper parseHelper = new ComponentParseHelper(xml);
    QParser.parse(new StringReader(xml), parseHelper);
    return parseHelper;
  }

  private static final class ComponentParseHelper extends QHandlerAdapter {
    private String m_currentId = null;
    private final Map<String, String> m_descriptions = Maps.newHashMap();
    private final Map<String, String> m_names = Maps.newHashMap();
    //
    private final String m_xml;
    private int m_descStart;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ComponentParseHelper(String xml) {
      m_xml = xml;
    }

    @Override
    public void startElement(int offset,
        int length,
        String name,
        Map<String, String> attributes,
        List<QAttribute> attrList,
        boolean closed) throws Exception {
      if ("description".equals(name)) {
        m_descStart = offset + length;
      }
      if ("creation".equals(name)) {
        String id = attributes.get("id");
        m_currentId = id;
        String compName = attributes.get("name");
        m_names.put(id, compName);
      }
    }

    @Override
    public void endElement(int offset, int endOffset, String name) throws Exception {
      if ("description".equals(name)) {
        m_descriptions.put(m_currentId, m_xml.substring(m_descStart, offset));
      }
    }

    public String getDescription(String creationId) {
      String desc = m_descriptions.get(creationId);
      if (desc == null) {
        return "";
      }
      return desc;
    }

    public String getName(String className, String creationId) {
      String name = m_names.get(creationId);
      if (name == null) {
        name = CodeUtils.getShortClass(className);
      }
      return name;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cache
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String getKey(String componentClassName, String creationId) {
    return componentClassName + " " + creationId;
  }

  private static ComponentPresentationCache getCache(AstEditor editor) throws Exception {
    String toolkitId = EditorState.get(editor).getToolkitId();
    return getCache(toolkitId);
  }

  private static ComponentPresentationCache getCache(String toolkitId) throws Exception {
    synchronized (m_presentationCaches) {
      ComponentPresentationCache cache = m_presentationCaches.get(toolkitId);
      if (cache == null) {
        cache = new ComponentPresentationCache(toolkitId);
        m_presentationCaches.put(toolkitId, cache);
        cache.load();
      }
      return cache;
    }
  }

  /**
   * Cache for {@link ComponentPresentation}s.
   */
  private static class ComponentPresentationCache {
    private final String m_toolkitId;
    private final List<Bundle> m_bundles = Lists.newArrayList();
    private final Set<String> m_bundleCheckSums = Sets.newHashSet();
    private final File m_cacheFile;
    private final Map<String, ComponentPresentation> m_presentations = Maps.newTreeMap();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ComponentPresentationCache(String toolkitId) throws Exception {
      m_toolkitId = toolkitId;
      m_cacheFile = getCacheFile();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public ComponentPresentation get(String key) {
      return m_presentations.get(key);
    }

    public void put(ComponentPresentation presentation) throws Exception {
      m_presentations.put(presentation.getKey(), presentation);
      writeCacheEntry(presentation);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Loading
    //
    ////////////////////////////////////////////////////////////////////////////
    public void load() {
      try {
        prepareBundles();
        prepareBundleCheckSums();
        ensureCacheExistsAndNotStale();
        loadFromCacheFile();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        try {
          m_cacheFile.delete();
        } catch (Throwable e2) {
        }
      }
    }

    private void loadFromCacheFile() throws Exception {
      InputStream stream = new FileInputStream(m_cacheFile);
      try {
        loadFromStream(stream);
      } finally {
        IOUtils.closeQuietly(stream);
      }
    }

    private void loadFromStream(InputStream inputStream) throws Exception {
      inputStream = new BufferedInputStream(inputStream);
      DataInputStream dataInput = new DataInputStream(inputStream);
      // skip check sums
      readCheckSums(dataInput);
      // read records
      while (true) {
        try {
          String key = dataInput.readUTF();
          String toolkitId = dataInput.readUTF();
          String name = dataInput.readUTF();
          String description = dataInput.readUTF();
          byte[] iconBytes;
          {
            int bytesLength = dataInput.readInt();
            iconBytes = new byte[bytesLength];
            dataInput.readFully(iconBytes);
          }
          ComponentPresentation presentation =
              new ComponentPresentation(key, toolkitId, name, description, iconBytes);
          m_presentations.put(key, presentation);
        } catch (EOFException e) {
          break;
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Writing
    //
    ////////////////////////////////////////////////////////////////////////////
    private void writeCacheEntry(ComponentPresentation presentation) throws Exception {
      RandomAccessFile randomAccessFile = new RandomAccessFile(m_cacheFile, "rw");
      try {
        randomAccessFile.seek(randomAccessFile.length());
        randomAccessFile.writeUTF(presentation.getKey());
        randomAccessFile.writeUTF(presentation.getToolkitId());
        randomAccessFile.writeUTF(presentation.getName());
        randomAccessFile.writeUTF(presentation.getDescription());
        {
          byte[] bytes = ImageUtils.getBytesPNG(presentation.getIcon());
          randomAccessFile.writeInt(bytes.length);
          randomAccessFile.write(bytes);
        }
      } finally {
        randomAccessFile.close();
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Bundles
    //
    ////////////////////////////////////////////////////////////////////////////
    private void prepareBundles() {
      for (IConfigurationElement toolkitElement : DescriptionHelper.getToolkitElements(m_toolkitId)) {
        Bundle bundle = ExternalFactoriesHelper.getExtensionBundle(toolkitElement);
        m_bundles.add(bundle);
      }
    }

    private void prepareBundleCheckSums() throws Exception {
      for (Bundle bundle : m_bundles) {
        URL presentationsResource = bundle.getEntry("wbp-meta/.wbp-cache-presentations");
        if (presentationsResource != null) {
          String checkSum = IOUtils2.readString(presentationsResource.openStream());
          m_bundleCheckSums.add(checkSum);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Cache file management
    //
    ////////////////////////////////////////////////////////////////////////////
    private File getCacheFile() throws Exception {
      File stateDirectory = DesignerPlugin.getDefault().getStateLocation().toFile();
      File descriptionsDirectory = new File(stateDirectory, "descriptions");
      File cacheFile = new File(descriptionsDirectory, m_toolkitId + ".cached-presentations.dat");
      Files.createParentDirs(cacheFile);
      return cacheFile;
    }

    private boolean cacheExistsAndNotStale() {
      try {
        if (m_cacheFile.exists()) {
          // check that not stale
          Set<String> cacheCheckSums = getCacheCheckSums();
          if (m_bundleCheckSums.equals(cacheCheckSums)) {
            return true;
          }
        }
      } catch (Throwable e) {
      }
      return false;
    }

    private void ensureCacheExistsAndNotStale() throws Exception {
      if (!cacheExistsAndNotStale()) {
        try {
          m_cacheFile.delete();
        } catch (Throwable e2) {
        }
      }
      // create new
      if (!m_cacheFile.exists()) {
        createNewCacheFile();
      }
    }

    private void createNewCacheFile() throws Exception {
      OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(m_cacheFile));
      DataOutputStream dataOutput = new DataOutputStream(outputStream);
      try {
        dataOutput.writeInt(m_bundleCheckSums.size());
        for (String checkSum : m_bundleCheckSums) {
          dataOutput.writeUTF(checkSum);
        }
      } finally {
        IOUtils.closeQuietly(dataOutput);
      }
    }

    private Set<String> getCacheCheckSums() throws Exception {
      InputStream inputStream = new FileInputStream(m_cacheFile);
      try {
        inputStream = new BufferedInputStream(inputStream);
        DataInputStream dataInput = new DataInputStream(inputStream);
        return readCheckSums(dataInput);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    }

    private static Set<String> readCheckSums(DataInputStream dataInput) throws IOException {
      Set<String> checkSums = Sets.newHashSet();
      int sumCount = dataInput.readInt();
      for (int i = 0; i < sumCount; i++) {
        String checkSum = dataInput.readUTF();
        checkSums.add(checkSum);
      }
      return checkSums;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Perform light scanning and fill cache for all available entries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates and schedules the job for pre-loading the cache of presentations based on toolkit name.
   */
  public static void scheduleFillingPresentationCache(final String toolkitId) {
    Job job = new Job("Updating WindowBuilder palette...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          // ensure cache
          ComponentPresentationCache cache = getCache(toolkitId);
          if (monitor.isCanceled()) {
            // it OK
            return Status.OK_STATUS;
          }
          // proceed with presentations
          fillPresentations(cache, toolkitId, monitor);
        } catch (Exception e) {
          DesignerPlugin.log(e);
        }
        return Status.OK_STATUS;
      }

      @Override
      public boolean belongsTo(Object family) {
        return PALETTE_PRELOAD_JOBS.equals(family);
      }
    };
    job.schedule();
  }

  private static void fillPresentations(ComponentPresentationCache cache,
      String toolkitId,
      IProgressMonitor monitor) throws Exception {
    Map<Bundle, List<IConfigurationElement>> bundles = Maps.newHashMap();
    // prepare bundles and entries
    for (IConfigurationElement toolkitElement : DescriptionHelper.getToolkitElements(toolkitId)) {
      IConfigurationElement[] elements = toolkitElement.getChildren("palette");
      List<IConfigurationElement> paletteElements = Lists.newArrayList();
      Collections.addAll(paletteElements, elements);
      bundles.put(ExternalFactoriesHelper.getExtensionBundle(toolkitElement), paletteElements);
    }
    // traverse and fetch presentations
    for (Map.Entry<Bundle, List<IConfigurationElement>> entry : bundles.entrySet()) {
      Bundle bundle = entry.getKey();
      for (IConfigurationElement paletteElement : entry.getValue()) {
        for (IConfigurationElement categoryElement : paletteElement.getChildren("category")) {
          for (IConfigurationElement element : categoryElement.getChildren()) {
            // get 'component's only
            if ("component".equals(element.getName())) {
              String className = ExternalFactoriesHelper.getRequiredAttribute(element, "class");
              String creationId = element.getAttribute("creationId");
              if (monitor.isCanceled()) {
                return;
              }
              fetchLightPresentation(bundle, cache, toolkitId, className, creationId);
            }
          }
        }
      }
    }
  }

  /**
   * Finds and parses *.wbp-component.xml files. If none found or an icon is absent then does
   * nothing.
   */
  private static void fetchLightPresentation(Bundle bundle,
      ComponentPresentationCache cache,
      String toolkitId,
      String componentClassName,
      String creationId) throws Exception {
    if (cache.get(getKey(componentClassName, creationId)) != null) {
      // already in cache, don't parse
      return;
    }
    // parse name and descriptions
    String resourceName =
        "/wbp-meta/" + componentClassName.replace('.', '/') + ".wbp-component.xml";
    URL resourceEntry = bundle.getEntry(resourceName);
    if (resourceEntry != null) {
      // parse
      ComponentParseHelper parseHelper = parseResource(resourceEntry);
      // done, create presentation
      String desc = parseHelper.getDescription(creationId);
      String name = parseHelper.getName(componentClassName, creationId);
      String key = getKey(componentClassName, creationId);
      byte[] iconBytes = getComponentImage(bundle, componentClassName, creationId);
      if (iconBytes == null) {
        // no image -- no presentation ;)
        return;
      }
      ComponentPresentation presentation =
          new ComponentPresentation(key, toolkitId, name, desc, iconBytes);
      cache.put(presentation);
    }
  }
}
