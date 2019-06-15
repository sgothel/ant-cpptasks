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
package net.sf.antcontrib.cpptasks.gcc;

import java.io.File;

import net.sf.antcontrib.cpptasks.CUtil;

/**
 * Adapter for the GCC linker
 *
 * @author Adam Murdoch, et.al.
 */
public class GccLinker extends GnuLinker {
    private static final String[] discardFiles = new String[0];
    private static final String[] objFiles = new String[]{".o", ".a", ".lib",
            ".dll", ".so", ".sl"};
    private static final String[] libtoolObjFiles = new String[]{".fo", ".a",
            ".lib", ".dll", ".so", ".sl"};
    private static final String[] linkerOptions = new String[]{"-bundle",
            "-dynamiclib", "-nostartfiles", "-nostdlib", "-prebind", "-noprebind", "-s",
            "-static", "-shared", "-symbolic", "-Xlinker",
            "--export-all-symbols", "-static-libgcc", "-static-libstdc++",};

    private static final GccLinker instance = new GccLinker("gcc", objFiles,
            discardFiles, "", "", false, null);
    private static final GccLinker clangInstance = new GccLinker("clang", objFiles,
            discardFiles, "", "", false, null);
    private static final GccLinker xcodeClangInstance = new GccLinker(clangInstance, true);

    private static final GccLinker dllLinker = new GccLinker("gcc", objFiles,
            discardFiles, "lib", ".so", false, new GccLinker("gcc", objFiles, discardFiles, "lib", ".so", true, null));
    private static final GccLinker dllClangLinker = new GccLinker("clang", objFiles,
            discardFiles, "lib", ".so", false, new GccLinker("clang", objFiles, discardFiles, "lib", ".so", true, null));

    private static final GccLinker arLinker = new GccLinker("gcc", objFiles,
            discardFiles, "lib", ".a", false, new GccLinker("gcc", objFiles, discardFiles, "lib", ".a", true, null));
    private static final GccLinker arClangLinker = new GccLinker("clang", objFiles,
            discardFiles, "lib", ".a", false, new GccLinker("clang", objFiles, discardFiles, "lib", ".a", true, null));

    private static final GccLinker machBundleLinker = new GccLinker("gcc",
            objFiles, discardFiles, "lib", ".bundle", false, null);
    private static final GccLinker machClangBundleLinker = new GccLinker("clang",
            objFiles, discardFiles, "lib", ".bundle", false, null);
    private static final GccLinker xcodeMachClangBundleLinker = new GccLinker(machClangBundleLinker, true);

    private static final GccLinker machDllLinker = new GccLinker("gcc",
            objFiles, discardFiles, "lib", ".dylib", false, null);
    private static final GccLinker machDllClangLinker = new GccLinker("clang",
            objFiles, discardFiles, "lib", ".dylib", false, null);
    private static final GccLinker xcodeMachDllClangLinker = new GccLinker(machDllClangLinker, true);

    private static final GccLinker machArLinker = new GccLinker("gcc",
            objFiles, discardFiles, "lib", ".a", false, null);
    private static final GccLinker machArClangLinker = new GccLinker("clang",
            objFiles, discardFiles, "lib", ".a", false, null);
    private static final GccLinker xcodeMachArClangLinker = new GccLinker(machArClangLinker, true);

    public static GccLinker getInstance() {
        return instance;
    }
    public static GccLinker getClangInstance() {
        return clangInstance;
    }
    public static GccLinker getXcodeClangInstance() {
        return xcodeClangInstance;
    }

    protected GccLinker(String command, String[] extensions,
            String[] ignoredExtensions, String outputPrefix,
            String outputSuffix, boolean isLibtool, GccLinker libtoolLinker) {
        super(command, "-dumpversion", extensions, ignoredExtensions,
                outputPrefix, outputSuffix, false, isLibtool, libtoolLinker);
    }
    protected GccLinker(GccLinker ld, boolean isXCoderun) {
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

    /**
     * Returns library path.
     *
     */
    @Override
    public File[] getLibraryPath() {
        if (libDirs == null) {
            //
            //   construct gcc lib path from machine and version
            //
            StringBuffer buf = new StringBuffer("/lib/gcc-lib/");
            buf.append(GccProcessor.getMachine());
            buf.append('/');
            buf.append(GccProcessor.getVersion());
            //
            //   build default path from gcc and system /lib and /lib/w32api
            //
            // String[] impliedLibPath = new String[]{buf.toString(), "/lib/w32api", "/lib"};

            //
            //     read gcc specs file for other library paths
            //
            String[] specs = GccProcessor.getSpecs();
            String[][] libpaths = GccProcessor.parseSpecs(specs, "*link:",
                    new String[]{"%q"});
            String[] libpath;
            if (libpaths[0].length > 0) {
                libpath = new String[libpaths[0].length + 3];
                int i = 0;
                for (; i < libpaths[0].length; i++) {
                    libpath[i] = libpaths[0][i];
                }
                libpath[i++] = buf.toString();
                libpath[i++] = "/lib/w32api";
                libpath[i++] = "/lib";
            } else {
                //
                //   if a failure to find any matches then
                //      use some default values for lib path entries
                libpath = new String[]{"/usr/local/lib/mingw",
                        "/usr/local/lib", "/usr/lib/w32api", "/usr/lib/mingw",
                        "/usr/lib", buf.toString(), "/lib/w32api", "/lib"};
            }
            for (int i = 0; i < libpath.length; i++) {
                if (libpath[i].indexOf("mingw") >= 0) {
                    libpath[i] = null;
                }
            }
            //
            //   if cygwin then
            //     we have to prepend location of gcc32
            //       and .. to start of absolute filenames to
            //       have something that will exist in the
            //       windows filesystem
            if (GccProcessor.isCygwin()) {
                GccProcessor.convertCygwinFilenames(libpath);
            }
            //
            //  check that remaining entries are actual directories
            //
            int count = CUtil.checkDirectoryArray(libpath);
            //
            //   populate return array with remaining entries
            //
            libDirs = new File[count];
            int index = 0;
            for (int i = 0; i < libpath.length; i++) {
                if (libpath[i] != null) {
                    libDirs[index++] = new File(libpath[i]);
                }
            }
        }
        return libDirs;
    }
}
