/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.geotools.porcelain;

import java.io.IOException;
import java.util.List;

import org.geogit.cli.CLICommand;
import org.geogit.cli.CommandFailedException;
import org.geogit.cli.GeogitCLI;
import org.geogit.cli.RequiresRepository;
import org.geogit.geotools.plumbing.GeoToolsOpException;
import org.geogit.geotools.plumbing.ListOp;
import org.geotools.data.DataStore;

import com.beust.jcommander.Parameters;
import com.google.common.base.Optional;

/**
 * Lists tables from a SpatiaLite database.
 * 
 * SpatiaLite CLI proxy for {@link ListOp}
 * 
 * @see ListOp
 */
@RequiresRepository
@Parameters(commandNames = "list", commandDescription = "List available feature types in a database")
public class SLList extends AbstractSLCommand implements CLICommand {

    /**
     * Executes the list command using the provided options.
     */
    @Override
    protected void runInternal(GeogitCLI cli) throws IOException {
        DataStore dataStore = getDataStore();

        try {
            cli.getConsole().println("Fetching feature types...");

            Optional<List<String>> features = cli.getGeogit().command(ListOp.class)
                    .setDataStore(dataStore).call();

            if (features.isPresent()) {
                for (String featureType : features.get()) {
                    cli.getConsole().println(" - " + featureType);
                }
            } else {
                throw new CommandFailedException(
                        "No features types were found in the specified database.");
            }
        } catch (GeoToolsOpException e) {
            throw new CommandFailedException("Unable to get feature types from the database.", e);
        } finally {
            dataStore.dispose();
            cli.getConsole().flush();
        }
    }
}
