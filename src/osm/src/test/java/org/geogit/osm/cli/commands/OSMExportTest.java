/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.cli.commands;

import java.io.File;
import java.util.Iterator;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.Platform;
import org.geogit.api.TestPlatform;
import org.geogit.api.plumbing.RevParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.DiffOp;
import org.geogit.cli.GeogitCLI;
import org.geogit.osm.internal.OSMImportOp;
import org.geogit.repository.WorkingTree;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class OSMExportTest extends Assert {

    private GeogitCLI cli;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                new UnsupportedTerminal());
        cli = new GeogitCLI(consoleReader);
        File workingDirectory = tempFolder.getRoot();
        Platform platform = new TestPlatform(workingDirectory);
        cli.setPlatform(platform);
        cli.execute("init");
        cli.execute("config", "user.name", "Gabriel Roldan");
        cli.execute("config", "user.email", "groldan@opengeo.org");
        assertTrue(new File(workingDirectory, ".geogit").exists());

    }

    @Test
    public void testExportOnlyNodes() throws Exception {
        String filename = OSMImportOp.class.getResource("nodes.xml").getFile();
        File file = new File(filename);
        cli.execute("osm", "import", file.getAbsolutePath());
        long unstaged = cli.getGeogit().getRepository().getWorkingTree().countUnstaged("node")
                .getCount();
        assertTrue(unstaged > 0);
        File exportFile = new File(tempFolder.getRoot(), "export.xml");
        cli.execute("osm", "export", exportFile.getAbsolutePath());
    }

    @Test
    public void testExportAndThenReimport() throws Exception {
        String filename = OSMImportOp.class.getResource("fire.xml").getFile();
        File filterFile = new File(filename);
        cli.execute("osm", "import", filterFile.getAbsolutePath());
        cli.execute("add");
        cli.execute("commit", "-m", "message");
        GeoGIT geogit = cli.getGeogit();
        Optional<ObjectId> id = geogit.command(RevParse.class).setRefSpec("HEAD:node").call();
        assertTrue(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec("HEAD:way").call();
        assertTrue(id.isPresent());
        File file = new File(tempFolder.getRoot(), "export.xml");
        cli.execute("osm", "export", file.getAbsolutePath());
        WorkingTree workingTree = geogit.getRepository().getWorkingTree();
        workingTree.delete("node");
        workingTree.delete("way");
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("Deleted OSM data").call();
        id = geogit.command(RevParse.class).setRefSpec("HEAD:node").call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec("HEAD:way").call();
        assertFalse(id.isPresent());
        cli.execute("osm", "import", file.getAbsolutePath());
        long unstaged = workingTree.countUnstaged("node").getCount();
        assertTrue(unstaged > 0);
        unstaged = workingTree.countUnstaged("way").getCount();
        assertTrue(unstaged > 0);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("Reimported").call();
        Iterator<DiffEntry> diffs = geogit.command(DiffOp.class).setNewVersion("HEAD")
                .setOldVersion("HEAD~2").call();
        assertFalse(diffs.hasNext());
    }

    @Test
    public void testExportFromWorkingHead() throws Exception {
        String filename = OSMImportOp.class.getResource("ways.xml").getFile();
        File file = new File(filename);
        cli.execute("osm", "import", file.getAbsolutePath());
        WorkingTree workingTree = cli.getGeogit().getRepository().getWorkingTree();
        long unstaged = workingTree.countUnstaged("node").getCount();
        assertTrue(unstaged > 0);
        unstaged = workingTree.countUnstaged("way").getCount();
        assertTrue(unstaged > 0);
        File exportFile = new File(tempFolder.getRoot(), "export.xml");
        cli.execute("osm", "export", exportFile.getAbsolutePath(), "WORK_HEAD");
        workingTree.delete("node");
        workingTree.delete("way");
        unstaged = workingTree.countUnstaged("node").getCount();
        assertFalse(unstaged > 0);
        unstaged = workingTree.countUnstaged("way").getCount();
        assertFalse(unstaged > 0);
        cli.execute("osm", "import", exportFile.getAbsolutePath());
        unstaged = workingTree.countUnstaged("node").getCount();
        assertTrue(unstaged > 0);
        unstaged = workingTree.countUnstaged("way").getCount();
        assertTrue(unstaged > 0);
    }

    @Test
    public void testExportAndThenReimportUsingPbfFormat() throws Exception {
        String filename = OSMImportOp.class.getResource("fire.xml").getFile();
        File filterFile = new File(filename);
        cli.execute("osm", "import", filterFile.getAbsolutePath());
        cli.execute("add");
        cli.execute("commit", "-m", "message");
        GeoGIT geogit = cli.getGeogit();
        Optional<ObjectId> id = geogit.command(RevParse.class).setRefSpec("HEAD:node").call();
        assertTrue(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec("HEAD:way").call();
        assertTrue(id.isPresent());
        File file = new File(tempFolder.getRoot(), "export.pbf");
        cli.execute("osm", "export", file.getAbsolutePath());
        WorkingTree workingTree = geogit.getRepository().getWorkingTree();
        workingTree.delete("node");
        workingTree.delete("way");
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).setMessage("Deleted OSM data").call();
        id = geogit.command(RevParse.class).setRefSpec("HEAD:node").call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec("HEAD:way").call();
        assertFalse(id.isPresent());
        cli.execute("osm", "import", file.getAbsolutePath());
        long unstaged = workingTree.countUnstaged("node").getCount();
        assertTrue(unstaged > 0);
        unstaged = workingTree.countUnstaged("way").getCount();
        assertTrue(unstaged > 0);
    }

}
