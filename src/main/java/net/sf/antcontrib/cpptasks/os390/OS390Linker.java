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
package net.sf.antcontrib.cpptasks.os390;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinker;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.types.LibrarySet;
import net.sf.antcontrib.cpptasks.types.LibraryTypeEnum;
import net.sf.antcontrib.cpptasks.VersionInfo;

import org.apache.tools.ant.BuildException;
/**
 * Adapter for the IBM (R) OS/390 (tm) Linker
 *
 * @author Hiram Chirino (cojonudo14@hotmail.com)
 */
public final class OS390Linker extends CommandLineLinker {
    private static final OS390Linker datasetLinker = new OS390Linker();
    private static final OS390Linker dllLinker = new OS390Linker("", ".dll");
    private static final OS390Linker instance = new OS390Linker("", "");
    public static OS390Linker getDataSetInstance() {
        return datasetLinker;
    }
    public static OS390Linker getInstance() {
        return instance;
    }
    private final boolean isADatasetLinker;
    File outputFile;
    private final String outputPrefix;
    CCTask task;
    private OS390Linker() {
        super("cxx", "/bogus", new String[]{".o", ".a", ".lib", ".xds"},
                new String[]{".dll", ".x"}, ".xds", false, false, null);
        this.outputPrefix = "";
        this.isADatasetLinker = true;
    }
    private OS390Linker(final String outputPrefix, final String outputSuffix) {
        super("cxx", "/bogus", new String[]{".o", ".a", ".lib", ".x"},
                new String[]{".dll"}, outputSuffix, false, false, null);
        this.outputPrefix = outputPrefix;
        this.isADatasetLinker = false;
    }
    @Override
    protected void addBase(final long base, final Vector args) {
    }
    @Override
    protected void addFixed(final Boolean fixed, final Vector args) {
    }
    @Override
    protected void addImpliedArgs(final boolean debug, final LinkType linkType, final Vector args) {
        if (linkType.isSharedLibrary()) {
            args.addElement("-W");
            args.addElement("l,DLL");
        }
    }
    @Override
    protected void addIncremental(final boolean incremental, final Vector args) {
    }
    /*
     * @see CommandLineLinker#addLibrarySets(LibrarySet[], Vector, Vector,
     *      Vector)
     */
    @Override
    protected String[] addLibrarySets(final CCTask task, final LibrarySet[] libsets,
            final Vector preargs, final Vector midargs, final Vector endargs) {
        // If yo want to link against a library sitting in a dataset and
        // not in the HFS, you can just use the //'dataset' notation
        // to specify it. e.g:
        // <libset dir="." libs="//'MQM.V5R2M0.SCSQLOAD'"/>
        //
        // We have to have special handling here because the file is not
        // on the normal filesystem so the task will not noramly include it
        // as part of the link command.
        if (libsets != null) {
            for (int i = 0; i < libsets.length; i++) {
                final String libs[] = libsets[i].getLibs();
                for (int j = 0; j < libs.length; j++) {
                    if (libs[j].startsWith("//")) {
                        endargs.addElement("-l");
                        endargs.addElement(libs[j]);
                    } else if (libsets[i].getDataset() != null) {
                        final String ds = libsets[i].getDataset();
                        endargs.addElement("//'" + ds + "(" + libs[j] + ")'");
                    }
                }
            }
        }
        return super.addLibrarySets(task, libsets, preargs, midargs, endargs);
    }
    @Override
    protected void addMap(final boolean map, final Vector args) {
    }
    @Override
    protected void addStack(final int stack, final Vector args) {
    }
    @Override
    protected void addEntry(final String entry, final Vector args) {
    }

    @Override
    public String getCommandFileSwitch(final String commandFile) {
        return "@" + commandFile;
    }
    @Override
    public File[] getLibraryPath() {
        return CUtil.getPathFromEnvironment("LIB", ";");
    }

    @Override
    public String[] getLibraryPatterns(final String[] libnames, final LibraryTypeEnum libType) {
        final StringBuffer buf = new StringBuffer();
        final String[] patterns = new String[libnames.length * 3];
        int offset = addLibraryPatterns(libnames, buf, "lib", ".a", patterns, 0);
        offset = addLibraryPatterns(libnames, buf, "", ".x", patterns, offset);
        offset = addLibraryPatterns(libnames, buf, "", ".o", patterns, offset);
        return patterns;
    }

    private static int addLibraryPatterns(final String[] libnames, final StringBuffer buf,
            final String prefix, final String extension, final String[] patterns, final int offset) {
        for (int i = 0; i < libnames.length; i++) {
            buf.setLength(0);
            buf.append(prefix);
            buf.append(libnames[i]);
            buf.append(extension);
            patterns[offset + i] = buf.toString();
        }
        return offset + libnames.length;
    }

    @Override
    public Linker getLinker(final LinkType linkType) {
        if (this == datasetLinker)
            return datasetLinker;
        if (linkType.isSharedLibrary())
            return dllLinker;
        return instance;
    }
    @Override
    public int getMaximumCommandLength() {
        return Integer.MAX_VALUE;
    }
    @Override
    public String getOutputFileName(final String baseName, final VersionInfo versionInfo) {
        return outputPrefix + super.getOutputFileName(baseName, versionInfo);
    }
    @Override
    protected String[] getOutputFileSwitch(final CCTask task, String outputFile) {
        if (isADatasetLinker && task.getDataset() != null) {
            final String ds = task.getDataset();
            outputFile = "//'" + ds + "(" + outputFile + ")'";
        }
        return getOutputFileSwitch(outputFile);
    }
    @Override
    public String[] getOutputFileSwitch(final String outputFile) {
        return new String[]{"-o", outputFile};
    }
    @Override
    public boolean isCaseSensitive() {
        return OS390Processor.isCaseSensitive();
    }
    /*
     * @see CommandLineLinker#link(Task, File, String[],
     *      CommandLineLinkerConfiguration)
     */
    @Override
    public void link(final CCTask task, File outputFile, final String[] sourceFiles,
            final CommandLineLinkerConfiguration config) throws BuildException {
        this.task = task;
        this.outputFile = outputFile;
        if (isADatasetLinker) {
            final int p = outputFile.getName().indexOf(".");
            if (p >= 0) {
                final String newname = outputFile.getName().substring(0, p);
                outputFile = new File(outputFile.getParent(), newname);
            }
        }
        super.link(task, outputFile, sourceFiles, config);
    }
    /*
     * @see CommandLineLinker#runCommand(Task, File, String[])
     */
    @Override
    protected int runCommand(final CCTask task, final File workingDir, final String[] cmdline)
            throws BuildException {
        final int rc = super.runCommand(task, workingDir, cmdline);
        // create the .xds file if everything was ok.
        if (rc == 0) {
            try {
                outputFile.delete();
                new FileOutputStream(outputFile).close();
            } catch (final IOException e) {
                throw new BuildException(e.getMessage());
            }
        }
        return rc;
    }
}
