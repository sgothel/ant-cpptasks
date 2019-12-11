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
package net.sf.antcontrib.cpptasks.sun;
import java.io.File;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinker;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.types.LibrarySet;
import net.sf.antcontrib.cpptasks.types.LibraryTypeEnum;
import net.sf.antcontrib.cpptasks.VersionInfo;

/**
 * Adapter for the Sun C89 Linker
 *
 * @author Hiram Chirino (cojonudo14@hotmail.com)
 */
public final class C89Linker extends CommandLineLinker {
    private static final C89Linker dllLinker = new C89Linker("lib", ".so");
    private static final C89Linker instance = new C89Linker("", "");
    public static C89Linker getInstance() {
        return instance;
    }
    private final String outputPrefix;
    private C89Linker(final String outputPrefix, final String outputSuffix) {
        super("ld", "/bogus", new String[]{".o", ".a", ".lib", ".x"},
                new String[]{}, outputSuffix, false, false, null);
        this.outputPrefix = outputPrefix;
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
            args.addElement("-G");
        }
    }
    @Override
    protected void addIncremental(final boolean incremental, final Vector args) {
    }
    @Override
    public String[] addLibrarySets(final CCTask task, final LibrarySet[] libsets,
            final Vector preargs, final Vector midargs, final Vector endargs) {
        super.addLibrarySets(task, libsets, preargs, midargs, endargs);
        final StringBuffer buf = new StringBuffer("-l");
        for (int i = 0; i < libsets.length; i++) {
            final LibrarySet set = libsets[i];
            final File libdir = set.getDir(null);
            final String[] libs = set.getLibs();
            if (libdir != null) {
                endargs.addElement("-L");
                endargs.addElement(libdir.getAbsolutePath());
            }
            for (int j = 0; j < libs.length; j++) {
                //
                //  reset the buffer to just "-l"
                //
                buf.setLength(2);
                //
                //  add the library name
                buf.append(libs[j]);
                //
                //  add the argument to the list
                endargs.addElement(buf.toString());
            }
        }
        return null;
    }
    @Override
    protected void addMap(final boolean map, final Vector args) {
    }
    @Override
    protected void addStack(final int stack, final Vector args) {
    }
    /* (non-Javadoc)
     * @see net.sf.antcontrib.cpptasks.compiler.CommandLineLinker#addEntry(int, java.util.Vector)
     */
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
        return C89Processor.getLibraryPatterns(libnames, libType);
    }
    @Override
    public Linker getLinker(final LinkType linkType) {
        if (linkType.isSharedLibrary()) {
            return dllLinker;
        }
        /*
         * if(linkType.isStaticLibrary()) { return
         * OS390Librarian.getInstance(); }
         */
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
    public String[] getOutputFileSwitch(final String outputFile) {
        return new String[]{"-o", outputFile};
    }
    @Override
    public boolean isCaseSensitive() {
        return C89Processor.isCaseSensitive();
    }
}
