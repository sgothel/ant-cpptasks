/*
 *
 * Copyright 2002-2004 The Ant-Contrib project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.antcontrib.cpptasks.compiler;
import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.LinkerParam;
import net.sf.antcontrib.cpptasks.ProcessorParam;
import net.sf.antcontrib.cpptasks.TargetInfo;
import net.sf.antcontrib.cpptasks.VersionInfo;

import org.apache.tools.ant.BuildException;
/**
 * A configuration for a command line linker
 *
 * @author Curt Arnold
 */
public final class CommandLineLinkerConfiguration
        implements
            LinkerConfiguration {
    private/* final */String[][] args;
    private final /* final */String identifier;
    private String[] libraryNames;
    private final /* final */CommandLineLinker linker;
    private final /* final */boolean map;
    private final /* final */ProcessorParam[] params;
    private final /* final */boolean rebuild;
    private final boolean debug;
    private final String startupObject;
    public CommandLineLinkerConfiguration(final CommandLineLinker linker,
            final String identifier, final String[][] args, final ProcessorParam[] params,
            final boolean rebuild, final boolean map, final boolean debug, final String[] libraryNames,
            final String startupObject) {
        if (linker == null) {
            throw new NullPointerException("linker");
        }
        if (args == null) {
            throw new NullPointerException("args");
        } else {
            this.args = args.clone();
        }
        this.linker = linker;
        this.params = params.clone();
        this.rebuild = rebuild;
        this.identifier = identifier;
        this.map = map;
        this.debug = debug;
        if (libraryNames == null) {
            this.libraryNames = new String[0];
        } else {
            this.libraryNames = libraryNames.clone();
        }
        this.startupObject = startupObject;
    }
    @Override
    public int bid(final String filename) {
        return linker.bid(filename);
    }
    public String[] getEndArguments() {
        final String[] clone = args[1].clone();
        return clone;
    }
    /**
     * Returns a string representation of this configuration. Should be
     * canonical so that equivalent configurations will have equivalent string
     * representations
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }
    public String[] getLibraryNames() {
        final String[] clone = libraryNames.clone();
        return clone;
    }
    public boolean getMap() {
        return map;
    }
    @Override
    public final String[] getOutputFileNames(final String inputFile, final VersionInfo versionInfo) {
        return linker.getOutputFileNames(inputFile, versionInfo);
    }
    @Override
    public final String getOutputFileName(final String inputFile, final VersionInfo versionInfo) {
        return linker.getOutputFileName(inputFile, versionInfo);
    }
    @Override
    public LinkerParam getParam(final String name) {
        for (int i = 0; i < params.length; i++) {
            if (name.equals(params[i].getName()))
                return (LinkerParam) params[i];
        }
        return null;
    }
    @Override
    public ProcessorParam[] getParams() {
        return params;
    }
    public String[] getPreArguments() {
        final String[] clone = args[0].clone();
        return clone;
    }
    @Override
    public boolean getRebuild() {
        return rebuild;
    }
    public String getStartupObject() {
        return startupObject;
    }
    @Override
    public void link(final CCTask task, final TargetInfo linkTarget) throws BuildException {
        //
        //  AllSourcePath's include any syslibsets
        //
        final String[] sourcePaths = linkTarget.getAllSourcePaths();
        linker.link(task, linkTarget.getOutput(), sourcePaths, this);
    }
    @Override
    public String toString() {
        return identifier;
    }
    @Override
    public Linker getLinker() {
    	return linker;
    }
    @Override
    public boolean isDebug() {
    	return debug;
    }
}
