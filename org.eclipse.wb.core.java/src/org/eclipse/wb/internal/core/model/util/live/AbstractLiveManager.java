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
package org.eclipse.wb.internal.core.model.util.live;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import java.util.Map;

/**
 * Helper for accessing "live" information for {@link AbstractComponentInfo} during adding it on
 * some container. Information can be just anything - image, style, default values of properties,
 * etc.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.model.util.live
 */
public abstract class AbstractLiveManager {
  protected final AbstractComponentInfo m_component;
  protected final AstEditor m_editor;
  protected final EditorState m_editorState;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractLiveManager(AbstractComponentInfo component) {
    m_component = component;
    m_editor = component.getEditor();
    m_editorState = EditorState.get(m_component.getEditor());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the filled {@link ILiveCacheEntry} instance. Creates "live" component, fill cache entry
   *         and removes "live" component.
   */
  protected final ILiveCacheEntry createCacheEntry() {
    try {
      return createCacheEntryEx();
    } catch (Throwable e) {
      e = DesignerExceptionUtils.getDesignerCause(e);
      DesignerPlugin.log(e);
      return createComponentCacheEntryEx(e);
    }
  }

  private ILiveCacheEntry createCacheEntryEx() throws Exception {
    BroadcastSupport oldBroadcast = m_editorState.getBroadcast();
    JavaInfoResolver oldResolver = m_editorState.getJavaInfoResolver();
    JavaInfo oldActiveObject = (JavaInfo) GlobalState.getActiveObject();
    oldBroadcast.getListener(DisplayEventListener.class).beforeMessagesLoop();
    //
    ExecutionFlowDescription old_flowDescription = m_editorState.getFlowDescription();
    AbstractComponentInfo liveComponentInfo = null;
    try {
      m_editorState.setBroadcastSupport(new BroadcastSupport());
      m_editorState.setLiveComponent(true);
      m_editor.setResolveImports(false);
      // create component model
      liveComponentInfo = createLiveComponent();
      JavaInfo root = liveComponentInfo.getRootJava();
      // finish edit operation
      root.endEdit();
      // fill cache
      return createComponentCacheEntry(liveComponentInfo);
    } finally {
      // do clean up
      cleanupLiveComponent(liveComponentInfo);
      m_editorState.setLiveComponent(false);
      m_editorState.setBroadcastSupport(oldBroadcast);
      m_editorState.setJavaInfoResolver(oldResolver);
      m_editorState.setFlowDescription(old_flowDescription);
      GlobalStateJava.activate(oldActiveObject);
      m_editor.setResolveImports(true);
      if (m_tmpType != null) {
        m_editor.removeBodyDeclaration(m_tmpType);
        m_editor.commitChanges();
      }
      // we finished inner operation
      oldBroadcast.getListener(DisplayEventListener.class).afterMessagesLoop();
    }
  }

  /**
   * @return the key for storing cached {@link Object}.
   */
  protected String getKey() {
    return m_component.getCreationSupport().toString();
  }

  /**
   * @return the {@link AbstractComponentInfo} of "live component" after parsing.
   */
  protected abstract AbstractComponentInfo createLiveComponent() throws Exception;

  /**
   * Does some clean up for "live" component created earlier.
   */
  protected void cleanupLiveComponent(AbstractComponentInfo liveComponentInfo) throws Exception {
    if (liveComponentInfo != null) {
      liveComponentInfo.getRoot().refresh_dispose();
    }
  }

  /**
   * Creates {@link ILiveCacheEntry} instance and fills it with required data.
   */
  protected abstract ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo liveComponentInfo);

  /**
   * Creates {@link ILiveCacheEntry} instance when exception happens.
   */
  protected abstract ILiveCacheEntry createComponentCacheEntryEx(Throwable e);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private TypeDeclaration m_tmpType;
  private MethodDeclaration m_method;

  /**
   * Parses given statements source in temporary type/method, but in same {@link AstEditor}, so with
   * same {@link EditorState}, {@link ClassLoader}, etc. Ensures that new parsed {@link JavaInfo}
   * hierarchy does not interacts with "main" hierarchy.
   *
   * @param sourceLines
   *          that source for {@link Statement}'s to parse.
   *
   * @return the root {@link JavaInfo} for parsed source.
   */
  protected final JavaInfo parse(String[] sourceLines) throws Exception {
    // prepare target type
    TypeDeclaration typeDeclaration = (TypeDeclaration) m_editor.getAstUnit().types().get(0);
    // create method
    m_tmpType =
        m_editor.addTypeDeclaration(
            ImmutableList.of("private static class __Tmp {", "}"),
            new BodyDeclarationTarget(typeDeclaration, false));
    m_method =
        m_editor.addMethodDeclaration(
            "private static void __tmp()",
            ImmutableList.copyOf(sourceLines),
            new BodyDeclarationTarget(m_tmpType, false));
    m_editorState.setFlowDescription(new ExecutionFlowDescription(m_method));
    // parse created method
    JavaInfo root = JavaInfoParser.parse(m_editor, m_method);
    root.startEdit();
    root.putArbitraryValue(JavaInfo.FLAG_DONT_COMMIT_EDITOR, Boolean.TRUE);
    return root;
  }

  /**
   * @return the {@link Image} to use as "live" for given {@link Throwable}.
   */
  protected static Image createImageForException(Throwable e) {
    int width = 200;
    int height = 50;
    Image image = new Image(null, width, height);
    GC gc = new GC(image);
    try {
      gc.setBackground(SwtResourceManager.getColor(255, 220, 220));
      gc.fillRectangle(0, 0, width, height);
      String text = ModelMessages.AbstractLiveManager_errorMessage;
      DrawUtils.drawTextWrap(gc, text, 0, 0, width, height);
    } finally {
      gc.dispose();
    }
    return image;
  }

  /**
   * @return the {@link JavaInfo} cloned from original component.
   */
  protected final <T extends JavaInfo> T createClone() throws Exception {
    CreationSupport creationSupport =
        ((ILiveCreationSupport) m_component.getCreationSupport()).getLiveComponentCreation();
    @SuppressWarnings("unchecked")
    T clone =
        (T) JavaInfoUtils.createJavaInfo(m_editor, m_component.getDescription(), creationSupport);
    // copy generics arguments
    clone.putTemplateArguments(m_component.getTemplateArguments());
    // clone ready
    return clone;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cached info
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the cached {@link ILiveCacheEntry} for this component. If no cached entry found then it
   *         creates one.
   */
  protected final ILiveCacheEntry getCachedEntry() {
    // check for static cache (only for constructor creation)
    if (m_component.getDescription().isCached()
        && m_component.getCreationSupport() instanceof ConstructorCreationSupport) {
      Map<String, ILiveCacheEntry> cache = getStaticCache();
      return getCachedEntry(cache, false);
    }
    // use for editor cache
    {
      Map<String, ILiveCacheEntry> cache = getEditorCache();
      return getCachedEntry(cache, true);
    }
  }

  /**
   * @return the cached {@link ILiveCacheEntry} for this component using specified cache. If no
   *         cached entry found then it creates one.
   */
  private ILiveCacheEntry getCachedEntry(Map<String, ILiveCacheEntry> cache,
      boolean disposeWithEditor) {
    String key = getKey();
    // get/put entry from cache
    ILiveCacheEntry entry = cache.get(key);
    if (entry == null) {
      entry = createCacheEntry();
      cache.put(key, entry);
      // schedule for dispose if needed
      if (disposeWithEditor) {
        EditorState.get(m_editor).addDisposable(entry);
      }
    }
    return entry;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, ILiveCacheEntry> m_staticCache = Maps.newHashMap();
  private static final String EDITOR_CACHE_KEY = "LIVE_CACHE";

  /**
   * @return the static level cache {@link Map}.
   */
  protected final Map<String, ILiveCacheEntry> getStaticCache() {
    return m_staticCache;
  }

  /**
   * @return the {@link AstEditor} level cache {@link Map}.
   */
  @SuppressWarnings("unchecked")
  protected final Map<String, ILiveCacheEntry> getEditorCache() {
    Map<String, ILiveCacheEntry> cache =
        (Map<String, ILiveCacheEntry>) m_component.getEditor().getGlobalValue(EDITOR_CACHE_KEY);
    if (cache == null) {
      cache = Maps.newHashMap();
      m_component.getEditor().putGlobalValue(EDITOR_CACHE_KEY, cache);
    }
    return cache;
  }
}
