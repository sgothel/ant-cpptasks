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
package net.sf.antcontrib.cpptasks.gcc.cross;
import java.io.File;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.LinkerParam;
import net.sf.antcontrib.cpptasks.compiler.CaptureStreamHandler;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.gcc.AbstractLdLinker;
import net.sf.antcontrib.cpptasks.types.LibrarySet;

import org.apache.tools.ant.BuildException;
/**
 * Adapter for the g++ variant of the GCC linker
 *
 * @author Stephen M. Webb <stephen.webb@bregmasoft.com>
 */
public class GppLinker extends AbstractLdLinker {
    protected static final String[] discardFiles = new String[0];
    protected static final String[] objFiles = new String[]{".o", ".a", ".lib",
            ".dll", ".so", ".sl"};
    private static final GppLinker arLinker = new GppLinker("gcc", objFiles,
            discardFiles, "lib", ".a", false, new GppLinker("gcc", objFiles,
                    discardFiles, "lib", ".a", true, null));
    private static final GppLinker dllLinker = new GppLinker("gcc", objFiles,
            discardFiles, "lib", ".so", false, new GppLinker("gcc", objFiles,
                    discardFiles, "lib", ".so", true, null));
    private final static String libPrefix = "libraries: =";
    protected static final String[] libtoolObjFiles = new String[]{".fo", ".a",
            ".lib", ".dll", ".so", ".sl"};
    private static String[] linkerOptions = new String[]{"-bundle", "-dylib",
            "-dynamic", "-dynamiclib", "-nostartfiles", "-nostdlib",
            "-prebind", "-s", "-static", "-shared", "-symbolic", "-Xlinker"};
    private static final GppLinker instance = new GppLinker("gcc", objFiles,
            discardFiles, "", "", false, null);
    private static final GppLinker machArLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".a", false, null);
    private static final GppLinker machDllLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".dylib", false, null);
    private static final GppLinker machPluginLinker = new GppLinker("gcc",
            objFiles, discardFiles, "lib", ".bundle", false, null);
    public static GppLinker getInstance() {
        return instance;
    }
    private File[] libDirs;
    private String runtimeLibrary;
    protected GppLinker(final String command, final String[] extensions,
            final String[] ignoredExtensions, final String outputPrefix,
            final String outputSuffix, final boolean isLibtool, final GppLinker libtoolLinker) {
        super(command, "-dumpversion", extensions, ignoredExtensions,
                outputPrefix, outputSuffix, false, isLibtool, libtoolLinker);
    }
    protected void addImpliedArgs(final boolean debug, final LinkType linkType, final Vector args) {
        super.addImpliedArgs(debug, linkType, args);
        if (getIdentifier().indexOf("mingw") >= 0) {
            if (linkType.isSubsystemConsole()) {
                args.addElement("-mconsole");
            }
            if (linkType.isSubsystemGUI()) {
                args.addElement("-mwindows");
            }
        }
        if (linkType.isStaticRuntime()) {
            final String[] cmdin = new String[]{"g++", "-print-file-name=libstdc++.a"};
            final String[] cmdout = CaptureStreamHandler.run(cmdin);
            if (cmdout.length > 0) {
                runtimeLibrary = cmdout[0];
            } else {
                runtimeLibrary = null;
            }
        } else {
            runtimeLibrary = "-lstdc++";
        }
    }
    public String[] addLibrarySets(final CCTask task, final LibrarySet[] libsets,
            final Vector preargs, final Vector midargs, final Vector endargs) {
        final String[] rs = super.addLibrarySets(task, libsets, preargs, midargs,
                endargs);
        if (runtimeLibrary != null) {
            endargs.addElement(runtimeLibrary);
        }
        return rs;
    }
    protected Object clone() throws CloneNotSupportedException {
        final GppLinker clone = (GppLinker) super.clone();
        return clone;
    }
    /**
     * Allows drived linker to decorate linker option. Override by GppLinker to
     * prepend a "-Wl," to pass option to through gcc to linker.
     *
     * @param buf
     *            buffer that may be used and abused in the decoration process,
     *            must not be null.
     * @param arg
     *            linker argument
     */
    public String decorateLinkerOption(final StringBuffer buf, final String arg) {
        String decoratedArg = arg;
        if (arg.length() > 1 && arg.charAt(0) == '-') {
            switch (arg.charAt(1)) {
                //
                //   passed automatically by GCC
                //
                case 'g' :
                case 'f' :
                case 'F' :
                /* Darwin */
                case 'm' :
                case 'O' :
                case 'W' :
                case 'l' :
                case 'L' :
                case 'u' :
                    break;
                default :
                    boolean known = false;
                    for (int i = 0; i < linkerOptions.length; i++) {
                        if (linkerOptions[i].equals(arg)) {
                            known = true;
                            break;
                        }
                    }
                    if (!known) {
                        buf.setLength(0);
                        buf.append("-Wl,");
                        buf.append(arg);
                        decoratedArg = buf.toString();
                    }
                    break;
            }
        }
        return decoratedArg;
    }
    /**
     * Returns library path.
     *
     */
    public File[] getLibraryPath() {
        if (libDirs == null) {
            final Vector dirs = new Vector();
            // Ask GCC where it will look for its libraries.
            final String[] args = new String[]{"g++", "-print-search-dirs"};
            final String[] cmdout = CaptureStreamHandler.run(args);
            for (int i = 0; i < cmdout.length; ++i) {
                final int prefixIndex = cmdout[i].indexOf(libPrefix);
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
            final String[] libpath = new String[dirs.size()];
            dirs.copyInto(libpath);
            final int count = CUtil.checkDirectoryArray(libpath);
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
    public Linker getLinker(final LinkType type) {
        if ( type.isStaticLibrary() && !type.getUseHighlevelTool() ) {
            return GccLibrarian.getInstance();
        }
        if (type.isStaticLibrary()) {
            if (isDarwin()) {
                return machArLinker;
            } else {
                return arLinker;
            }
        }
        if (type.isPluginModule()) {
            if (GccProcessor.getMachine().indexOf("darwin") >= 0) {
                return machPluginLinker;
            } else {
                return dllLinker;
            }
        }
        if (type.isSharedLibrary()) {
            if (GccProcessor.getMachine().indexOf("darwin") >= 0) {
                return machDllLinker;
            } else {
                return dllLinker;
            }
        }
        return instance;
    }
    public void link(final CCTask task, final File outputFile, final String[] sourceFiles,
            final CommandLineLinkerConfiguration config) throws BuildException {
        try {
            final GppLinker clone = (GppLinker) this.clone();
            final LinkerParam param = config.getParam("target");
            if (param != null)
                clone.setCommand(param.getValue() + "-" + this.getCommand());
            clone.superlink(task, outputFile, sourceFiles, config);
        } catch (final CloneNotSupportedException e) {
            superlink(task, outputFile, sourceFiles, config);
        }
    }
    private void superlink(final CCTask task, final File outputFile, final String[] sourceFiles,
            final CommandLineLinkerConfiguration config) throws BuildException {
        super.link(task, outputFile, sourceFiles, config);
    }
}
