/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.osm.cli.commands;

import java.io.IOException;
import java.util.List;

import jline.console.ConsoleReader;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.cli.AbstractCommand;
import org.geogit.cli.CLICommand;
import org.geogit.cli.CommandFailedException;
import org.geogit.cli.GeogitCLI;
import org.geogit.cli.RequiresRepository;
import org.geogit.osm.internal.Mapping;
import org.geogit.osm.internal.OSMMapOp;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Creates new data in the geogit repository based on raw OSM data already in the repository.
 * 
 * 
 * @see OSMMapOp
 */
@RequiresRepository
@Parameters(commandNames = "map", commandDescription = "Create new data in the repository, applying a mapping to the current OSM data")
public class OSMMap extends AbstractCommand implements CLICommand {

    @Parameter(description = "<file>")
    public List<String> args;

    private GeoGIT geogit;

    /**
     * Executes the map command using the provided options.
     */
    @Override
    protected void runInternal(GeogitCLI cli) throws IOException {
        if (args == null || args.isEmpty() || args.size() != 1) {
            printUsage();
            throw new CommandFailedException();
        }

        String mappingFilepath = args.get(0);

        Mapping mapping = Mapping.fromFile(mappingFilepath);

        geogit = cli.getGeogit();

        ObjectId oldTreeId = geogit.getRepository().getWorkingTree().getTree().getId();

        ObjectId newTreeId = geogit.command(OSMMapOp.class).setMapping(mapping).call().getId();

        ConsoleReader console = cli.getConsole();
        if (newTreeId.equals(oldTreeId)) {
            console.println("No features matched the specified filter.\n"
                    + "No changes have been made to the working tree");
        } else {
            // print something?
        }
    }

}
