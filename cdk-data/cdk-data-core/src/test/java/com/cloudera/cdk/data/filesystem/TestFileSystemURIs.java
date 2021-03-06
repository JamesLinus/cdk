/*
 * Copyright 2013 Cloudera Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.cdk.data.filesystem;

import com.cloudera.cdk.data.DatasetRepositories;
import com.cloudera.cdk.data.DatasetRepository;
import com.cloudera.cdk.data.DatasetRepositoryException;
import com.cloudera.cdk.data.MetadataProvider;
import com.cloudera.cdk.data.MiniDFSTest;
import com.cloudera.cdk.data.filesystem.impl.Loader;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.fs.LocalFileSystem;
import org.junit.BeforeClass;

public class TestFileSystemURIs extends MiniDFSTest {

  @BeforeClass
  public static void loadImpl() {
    new Loader().load();
  }

  @Test
  public void testLocalRelative() throws URISyntaxException {
    DatasetRepository repository = DatasetRepositories.open(new URI("repo:file:target/dsr-repo-test"));

    // We only do the deeper implementation checks one per combination.
    Assert.assertNotNull("Received a repository", repository);
    Assert.assertTrue("Repo is a FileSystem repo",
        repository instanceof FileSystemDatasetRepository);
    MetadataProvider provider = ((FileSystemDatasetRepository) repository)
        .getMetadataProvider();
    Assert.assertTrue("Repo is using a FileSystemMetadataProvider",
        provider instanceof FileSystemMetadataProvider);
    FileSystemMetadataProvider fsProvider = (FileSystemMetadataProvider) provider;
    Assert.assertTrue("FileSystem is a LocalFileSystem",
        fsProvider.getFileSytem() instanceof LocalFileSystem);
    Path expected = fsProvider.getFileSytem().makeQualified(
        new Path("target/dsr-repo-test"));
    Assert.assertEquals("Root directory should be the correct qualified path",
        expected, fsProvider.getRootDirectory());
  }

  @Test
  public void testLocalAbsolute() throws URISyntaxException {
    DatasetRepository repository = DatasetRepositories.open(new URI("repo:file:/tmp/dsr-repo-test"));

    FileSystemMetadataProvider provider = (FileSystemMetadataProvider)
        ((FileSystemDatasetRepository) repository).getMetadataProvider();
    Assert.assertEquals("Root directory should be the correct qualified path",
        new Path("file:/tmp/dsr-repo-test"), provider.getRootDirectory());
  }

  @Test(expected = DatasetRepositoryException.class)
  public void testHdfsFailsDefault() {
    // the environment doesn't contain the HDFS URI, so this should cause a
    // DatasetRepository exception about not finding HDFS
    DatasetRepositories.open("repo:hdfs:/");
  }

  @Test
  public void testHdfsAbsolute() throws URISyntaxException {
    URI hdfsUri = getDFS().getUri();
    DatasetRepository repository = DatasetRepositories.open(
        new URI("repo:hdfs://" + hdfsUri.getAuthority() + "/tmp/dsr-repo-test"));

    // We only do the deeper implementation checks one per combination.
    Assert.assertNotNull("Received a repository", repository);
    Assert.assertTrue("Repo is a FileSystem repo",
        repository instanceof FileSystemDatasetRepository);
    MetadataProvider provider = ((FileSystemDatasetRepository) repository)
        .getMetadataProvider();
    Assert.assertTrue("Repo is using a FileSystemMetadataProvider",
        provider instanceof FileSystemMetadataProvider);
    FileSystemMetadataProvider fsProvider = (FileSystemMetadataProvider) provider;
    Assert.assertTrue("FileSystem is a DistributedFileSystem",
      fsProvider.getFileSytem() instanceof DistributedFileSystem);
    Path expected = fsProvider.getFileSytem().makeQualified(
        new Path("/tmp/dsr-repo-test"));
    Assert.assertEquals("Root directory should be the correct qualified path",
        expected, fsProvider.getRootDirectory());
  }

}
