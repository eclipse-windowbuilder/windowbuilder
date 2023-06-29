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
package org.eclipse.wb.internal.core.xml.model.utils;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.core.xml.Messages;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jdt.core.dom.Statement;
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
 * @coverage XML.model.utils
 */
public abstract class AbstractLiveManager {
	protected final AbstractComponentInfo m_component;
	private final EditorContext m_context;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractLiveManager(AbstractComponentInfo component) {
		m_component = component;
		m_context = component.getContext();
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
			DesignerPlugin.log(e);
			return createComponentCacheEntryEx(e);
		}
	}

	private ILiveCacheEntry createCacheEntryEx() throws Exception {
		BroadcastSupport oldBroadcast = m_context.getBroadcastSupport();
		XmlObjectInfo oldActiveObject = (XmlObjectInfo) GlobalState.getActiveObject();
		oldBroadcast.getListener(DisplayEventListener.class).beforeMessagesLoop();
		//
		AbstractComponentInfo liveComponentInfo = null;
		try {
			liveComponentInfo = createLiveComponent();
			// finish edit operation
			liveComponentInfo.getRoot().endEdit();
			// fill cache
			return createComponentCacheEntry(liveComponentInfo);
		} finally {
			GlobalStateXml.activate(oldActiveObject);
			// do clean up
			cleanupLiveComponent(liveComponentInfo);
			m_context.getLiveContext().dispose();
			// we finished inner operation
			oldBroadcast.getListener(DisplayEventListener.class).afterMessagesLoop();
		}
	}

	/**
	 * @return the key for {@link ILiveCacheEntry} in cache.
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
	/**
	 * Parses given statements source in temporary type/method, but in same {@link AstEditor}, so with
	 * same {@link EditorState}, {@link ClassLoader}, etc. Ensures that new parsed
	 * {@link XmlObjectInfo} hierarchy does not interacts with "main" hierarchy.
	 *
	 * @param sourceLines
	 *          that source for {@link Statement}'s to parse.
	 *
	 * @return the root {@link XmlObjectInfo} for parsed source.
	 */
	protected final XmlObjectInfo parse(String[] sourceLines) throws Exception {
		XmlObjectInfo root = m_context.getLiveContext().parse(sourceLines);
		root.startEdit();
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
			String text = Messages.AbstractLiveManager_errorMessage;
			DrawUtils.drawTextWrap(gc, text, 0, 0, width, height);
		} finally {
			gc.dispose();
		}
		return image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cached info
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ClassMap<Map<String, ILiveCacheEntry>> CACHE = ClassMap.create();

	/**
	 * @return the cached {@link ILiveCacheEntry} for this component. If no cached entry found then it
	 *         creates one.
	 */
	protected final ILiveCacheEntry getCachedEntry() {
		// prepare creation-specific cache
		Map<String, ILiveCacheEntry> cache;
		{
			Class<?> clazz = m_component.getDescription().getComponentClass();
			cache = CACHE.get(clazz);
			if (cache == null) {
				cache = Maps.newTreeMap();
				CACHE.put(clazz, cache);
			}
		}
		// get/put entry from cache
		String key = getKey();
		ILiveCacheEntry entry = cache.get(key);
		if (entry == null) {
			entry = createCacheEntry();
			cache.put(key, entry);
		}
		return entry;
	}
}
