/*
 *
 * Copyright 2001-2004 The Ant-Contrib project
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
package net.sf.antcontrib.cpptasks.gcc;
import java.io.File;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinker;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.VersionInfo;
import net.sf.antcontrib.cpptasks.types.LibraryTypeEnum;

import org.apache.tools.ant.BuildException;
/**
 * Adapter for the "ar" tool
 *
 * @author Adam Murdoch
 * @author Curt Arnold
 */
public abstract class AbstractArLibrarian extends CommandLineLinker {
    private final /* final */
    String outputPrefix;
    protected AbstractArLibrarian(final String command, final String identificationArg,
            final String[] inputExtensions, final String[] ignoredExtensions,
            final String outputPrefix, final String outputExtension, final boolean isLibtool,
            final AbstractArLibrarian libtoolLibrarian) {
        super(command, identificationArg, inputExtensions, ignoredExtensions,
                outputExtension, false, isLibtool, libtoolLibrarian);
        this.outputPrefix = outputPrefix;
    }
    @Override
    public void addBase(final long base, final Vector args) {
    }
    @Override
    public void addFixed(final Boolean fixed, final Vector args) {
    }
    @Override
    public void addImpliedArgs(final boolean debug, final LinkType linkType, final Vector args) {
    }
    @Override
    public void addIncremental(final boolean incremental, final Vector args) {
    }
    @Override
    public void addMap(final boolean map, final Vector args) {
    }
    @Override
    public void addStack(final int stack, final Vector args) {
    }
    /* (non-Javadoc)
     * @see net.sf.antcontrib.cpptasks.compiler.CommandLineLinker#addEntry(int, java.util.Vector)
     */
    @Override
    protected void addEntry(final String entry, final Vector args) {
    }

    @Override
    public String getCommandFileSwitch(final String commandFile) {
        return null;
    }
    @Override
    public File[] getLibraryPath() {
        return new File[0];
    }
    @Override
    public String[] getLibraryPatterns(final String[] libnames, final LibraryTypeEnum libType) {
    	return new String[0];
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
    public String[] getOutputFileSwitch(final String outputFile) {
        return GccProcessor.getOutputFileSwitch("rvs", outputFile);
    }
    @Override
    public boolean isCaseSensitive() {
        return true;
    }
    @Override
    public void link(final CCTask task, final File outputFile, final String[] sourceFiles,
            final CommandLineLinkerConfiguration config) throws BuildException {
        //
        //   if there is an existing library then
        //      we must delete it before executing "ar"
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                throw new BuildException("Unable to delete "
                        + outputFile.getAbsolutePath());
            }
        }
        //
        //   delegate to CommandLineLinker
        //
        super.link(task, outputFile, sourceFiles, config);
    }
}
