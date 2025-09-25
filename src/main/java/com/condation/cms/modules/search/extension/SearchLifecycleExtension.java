package com.condation.cms.modules.search.extension;

/*-
 * #%L
 * thymeleaf-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.eventbus.events.TemplateChangedEvent;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.modules.search.SearchEngine;
import com.condation.modules.api.ModuleLifeCycleExtension;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(ModuleLifeCycleExtension.class)
public class SearchLifecycleExtension extends ModuleLifeCycleExtension<SiteModuleContext, SiteRequestContext> {

	static SearchEngine searchEngine;

	@Override
	public void init() {
	}

	private String getLanguage () {
		String language = (String) getContext().get(SitePropertiesFeature.class).siteProperties().get("language");
		if (language == null) {
			language = "standard";
		}
		return language;
	}
	
	@Override
	public void activate() {
		searchEngine = new SearchEngine();
		try {
			searchEngine.open(configuration.getDataDir().toPath().resolve("index"), getLanguage());

			// stat reindexing
			Thread.ofVirtual().start(() -> {

				reindexContext();
			});
		} catch (IOException e) {
			log.error("error opening serach engine", e);
			throw new RuntimeException(e);
		}
		
		getContext().get(EventBusFeature.class).eventBus().register(ContentChangedEvent.class, (event) -> {
			reindexContext();
		});
		getContext().get(EventBusFeature.class).eventBus().register(TemplateChangedEvent.class, (event) -> {
			reindexContext();
		});
	}

	protected void reindexContext() {
		var contentPath = getContext().get(DBFeature.class).db().getFileSystem().resolve("content");
		try {
			searchEngine.clear();
			Files.walkFileTree(contentPath, new FileIndexingVisitor(
					contentPath, 
					SearchLifecycleExtension.searchEngine, 
					getContext()
			));
			searchEngine.commit();
		} catch (IOException e) {
			log.error(null, e);
		}
	}

	@Override
	public void deactivate() {
		try {
			searchEngine.close();
		} catch (Exception e) {
			log.error("error closing serach engine", e);
			throw new RuntimeException(e);
		}
	}
}
