/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.cli.porcelain;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.porcelain.InitOp;
import org.geogit.cli.AbstractCommand;
import org.geogit.cli.CLICommand;
import org.geogit.cli.GeogitCLI;
import org.geogit.repository.Repository;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * This command creates an empty geogit repository - basically a .geogit directory with
 * subdirectories for the object, refs, index, and config databases. An initial HEAD that references
 * the HEAD of the master branch is also created.
 * <p>
 * CLI proxy for {@link InitOp}
 * <p>
 * Usage:
 * <ul>
 * <li> {@code geogit init [<directory>]}
 * </ul>
 * 
 * @see InitOp
 */
@Parameters(commandNames = "init", commandDescription = "Create an empty geogit repository or reinitialize an existing one")
public class Init extends AbstractCommand implements CLICommand {

    @Parameter(description = "Repository location (directory).", required = false, arity = 1)
    private List<String> location;

    /**
     * Executes the init command.
     */
    @Override
    public void runInternal(GeogitCLI cli) throws IOException {

        final File repoDir;
        {
            File currDir = cli.getPlatform().pwd();
            if (location != null && location.size() == 1) {
                String target = location.get(0);
                File f = new File(target);
                if (!f.isAbsolute()) {
                    f = new File(currDir, target).getCanonicalFile();
                }
                repoDir = f;
                if (!repoDir.exists() && !repoDir.mkdirs()) {
                    throw new IllegalStateException("Can't create directory "
                            + repoDir.getAbsolutePath());
                }
            } else {
                repoDir = currDir;
            }
        }

        GeoGIT geogit = null;
        if (cli.getGeogit() == null) {
            geogit = new GeoGIT(cli.getGeogitInjector(), repoDir);
        } else {
            geogit = cli.getGeogit();
        }

        Repository repository = geogit.command(InitOp.class).call();
        final boolean repoExisted = repository == null;
        geogit.setRepository(repository);
        cli.setGeogit(geogit);

        final URL envHome = geogit.command(ResolveGeogitDir.class).call();

        File repoDirectory;
        try {
            repoDirectory = new File(envHome.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Environment home can't be resolved to a directory", e);
        }
        String message;
        if (repoExisted) {
            message = "Reinitialized existing Geogit repository in "
                    + repoDirectory.getAbsolutePath();
        } else {
            message = "Initialized empty Geogit repository in " + repoDirectory.getAbsolutePath();
        }
        cli.getConsole().println(message);

    }
}