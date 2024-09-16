package com.condation.cms.modules.search;

/*-
 * #%L
 * search-module
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
import com.condation.cms.api.utils.PathUtil;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class FileIndexingVisitorTest {

	public FileIndexingVisitorTest() {
	}

	@Test
	public void testSomeMethod() {
		var contentPath = Path.of("src/test/resources").resolve("content");
		try {
			Files.walkFileTree(contentPath, new TestFileVisitor(contentPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequiredArgsConstructor
	public class TestFileVisitor extends SimpleFileVisitor<Path> {

		private final Path contentBase;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Files.isDirectory(file)) {
				return FileVisitResult.CONTINUE;
			}
			try {
				var uri = PathUtil.toRelativeFile(file, contentBase);
				System.out.println(uri);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.getFileName().toString().startsWith(".")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}
		
		

	}

}
