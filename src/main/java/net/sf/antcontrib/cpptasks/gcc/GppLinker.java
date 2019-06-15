/*
 *
 * Copyright 2003-2004 The Ant-Contrib project
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.compiler.CaptureStreamHandler;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.types.LibrarySet;
/**
 * Adapter for the g++ variant of the GCC linker
 *
 * @author Stephen M. Webb <stephen.webb@bregmasoft.com>, et.al.
 */
public class GppLinker extends GnuLinker {
    protected static final String[] discardFiles = new String[0];
    protected static final String[] objFiles = new String[]{".o", ".a", ".lib",
            ".dll", ".so", ".sl"};
    private final static String libPrefix = "libraries: =";
    private static final String[] libtoolObjFiles = new String[]{".fo", ".a",
            ".lib", ".dll", ".so", ".sl"};
    private static String[] linkerOptions = new String[]{"-bundle", "-dylib",
            "-dynamic", "-dynamiclib", "-nostartfiles", "-nostdlib",
            "-prebind", "-s", "-static", "-shared", "-symbolic", "-Xlinker"};

    private static final GppLinker instance = new GppLinker("gcc", objFiles,
            discardFiles, "", "", false, false, null);
    private static final GppLinker clangInstance = new GppLinker("clang", objFiles,
            discardFiles, "", "", false, false, null);
    private static final GppLinker xcodeClangInstance = new GppLinker(clangInstance, true);

    private static final GppLinker dllLinker = new GppLinker("gcc", objFiles,
            discardFiles, "lib", ".so", false, false, new GppLinker("gcc", objFiles,
                    discardFiles, "lib", ".so", false, true, null));
    private static final GppLinker dllClangLinker = new GppLinker("clang", objFiles,
            discardFiles, "lib", ".so", false, false, new GppLinker("clang", objFiles,
                    discardFiles, "lib", ".so", false, true, null));

    private static final GppLinker arLinker = new GppLinker("gcc", objFiles,
            discardFiles, "lib", ".a", false, false, new GppLinker("gcc", objFiles,
                    discardFiles, "lib", ".a", false, true, null));
    private static final GppLinker arClangLinker = new GppLinker("clang", objFiles,
            discardFiles, "lib", ".a", false, false, new GppLinker("clang", objFiles,
                    discardFiles, "lib", ".a", false, true, null));

    private static final GppLinker machBundleLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".bundle", false, false, null);
    private static final GppLinker machClangBundleLinker = new GppLinker("clang",
            objFiles, discardFiles, "lib", ".bundle", false, false, null);
    private static final GppLinker xcodeMachClangBundleLinker = new GppLinker(machClangBundleLinker, true);

    private static final GppLinker machDllLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".dylib", false, false, null);
    private static final GppLinker machDllClangLinker = new GppLinker("clang",
            objFiles, discardFiles, "lib", ".dylib", false, false, null);
    private static final GppLinker xcodeMachDllClangLinker = new GppLinker(machDllClangLinker, true);

    private static final GppLinker machArLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".a", false, false, null);
    private static final GppLinker machArClangLinker = new GppLinker("clang",
            objFiles, discardFiles, "lib", ".a", false, false, null);
    private static final GppLinker xcodeMachArClangLinker = new GppLinker(machArClangLinker, true);

    public static GppLinker getInstance() {
        return instance;
    }
    public static GppLinker getClangInstance() {
        return clangInstance;
    }
    public static GppLinker getXcodeClangInstance() {
        return xcodeClangInstance;
    }
    private String runtimeLibrary;
    protected GppLinker(String command, String[] extensions,
            String[] ignoredExtensions, String outputPrefix,
            String outputSuffix, boolean isXCoderun, boolean isLibtool, GppLinker libtoolLinker) {
        super(command, "-dumpversion", extensions, ignoredExtensions,
                outputPrefix, outputSuffix, false, isLibtool, libtoolLinker);
    }
    protected GppLinker(GppLinker ld, boolean isXCoderun) {
        super(ld, isXCoderun);
    }

    @Override
    protected final String[] getStaticLinkerOptions() { return  linkerOptions; }

    @Override
    protected final GnuLinker getStaticDllLinker() {
        return dllLinker;
    }
    @Override
    protected final GnuLinker getStaticDllClangLinker() {
        return dllClangLinker;
    }
    @Override
    protected final GnuLinker getStaticArLinker() {
        return arLinker;
    }
    @Override
    protected final GnuLinker getStaticArClangLinker() {
        return arClangLinker;
    }
    @Override
    protected final GnuLinker getStaticClangInstance() {
        return clangInstance;
    }
    @Override
    protected final GnuLinker getStaticXcodeClangInstance() {
        return xcodeClangInstance;
    }
    @Override
    protected final GnuLinker getStaticMachBundleLinker() {
        return machBundleLinker;
    }
    @Override
    protected final GnuLinker getStaticMachClangBundleLinker() {
        return machClangBundleLinker;
    }
    @Override
    protected final GnuLinker getStaticXcodeMachClangBundleLinker() {
        return xcodeMachClangBundleLinker;
    }
    @Override
    protected final GnuLinker getStaticMachDllLinker() {
        return machDllLinker;
    }
    @Override
    protected final GnuLinker getStaticMachDllClangLinker() {
        return machDllClangLinker;
    }
    @Override
    protected final GnuLinker getStaticXcodeMachDllClangLinker() {
        return xcodeMachDllClangLinker;
    }
    @Override
    protected final GnuLinker getStaticMachArLinker() {
        return machArLinker;
    }
    @Override
    protected final GnuLinker getStaticMachArClangLinker() {
        return machArClangLinker;
    }
    @Override
    protected final GnuLinker getStaticXcodeMachArClangLinker() {
        return xcodeMachArClangLinker;
    }
    @Override
    protected final GnuLinker getStaticInstance() {
        return instance;
    }

    @Override
    protected void addImpliedArgs(boolean debug, LinkType linkType, Vector args) {
        super.addImpliedArgs(debug, linkType, args);
        if (linkType.isStaticRuntime()) {
            String[] cmdin = new String[]{"g++", "-print-file-name=libstdc++.a"};
            String[] cmdout = CaptureStreamHandler.run(cmdin);
            if (cmdout.length > 0) {
                runtimeLibrary = cmdout[0];
            } else {
                runtimeLibrary = null;
            }
        } else {
            runtimeLibrary = "-lstdc++";
        }
    }
    @Override
    public String[] addLibrarySets(CCTask task, LibrarySet[] libsets,
            Vector preargs, Vector midargs, Vector endargs) {
        String[] rs = super.addLibrarySets(task, libsets, preargs, midargs,
                endargs);
        if (runtimeLibrary != null) {
            endargs.addElement(runtimeLibrary);
        }
        return rs;
    }
    /**
     * Returns library path.
     *
     */
    @Override
    public File[] getLibraryPath() {
        if (libDirs == null) {
            Vector dirs = new Vector();
            // Ask GCC where it will look for its libraries.
            String[] args = new String[]{"g++", "-print-search-dirs"};
            String[] cmdout = CaptureStreamHandler.run(args);
            for (int i = 0; i < cmdout.length; ++i) {
                int prefixIndex = cmdout[i].indexOf(libPrefix);
                if (prefixIndex >= 0) {
                    // Special case DOS-type GCCs like MinGW or Cygwin
                    int s = prefixIndex + libPrefix.length();
                    int t = cmdout[i].indexOf(';', s);
                    while (t > 0) {
                        dirs.addElement(cmdout[i].substring(s, t));
                        s = t + 1;
                        t = cmdout[i].indexOf(';', s);
                    }
                    dirs.addElement(cmdout[i].substring(s));
                    ++i;
                    for (; i < cmdout.length; ++i) {
                        dirs.addElement(cmdout[i]);
                    }
                }
            }
            // Eliminate all but actual directories.
            String[] libpath = new String[dirs.size()];
            dirs.copyInto(libpath);
            int count = CUtil.checkDirectoryArray(libpath);
            // Build return array.
            libDirs = new File[count];
            int index = 0;
            for (int i = 0; i < libpath.length; ++i) {
                if (libpath[i] != null) {
                    libDirs[index++] = new File(libpath[i]);
                }
            }
        }
        return libDirs;
    }
}
