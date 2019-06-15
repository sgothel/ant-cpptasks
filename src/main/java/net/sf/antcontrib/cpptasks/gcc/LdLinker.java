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

import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.gcc.cross.GccLibrarian;
/**
 * Adapter for the 'ld' linker
 *
 * @author Curt Arnold
 */
public final class LdLinker extends AbstractLdLinker {
    private static final String[] discardFiles = new String[0];
    private static final String[] objFiles = new String[]{".o", ".a", ".lib",
            ".dll", ".so", ".sl"};
    private static final LdLinker dllLinker = new LdLinker("ld", objFiles,
            discardFiles, "lib", ".so", false, new LdLinker("ld", objFiles,
                    discardFiles, "lib", ".so", true, null));
    private static final LdLinker arLinker = new LdLinker("ld", objFiles,
            discardFiles, "lib", ".a", false, new LdLinker("ld", objFiles,
                    discardFiles, "lib", ".a", true, null));
    private static final LdLinker instance = new LdLinker("ld", objFiles,
            discardFiles, "", "", false, null);
    private static final String[] libtoolObjFiles = new String[]{".fo", ".a",
            ".lib", ".dll", ".so", ".sl"};
    public static LdLinker getInstance() {
        return instance;
    }
    private File[] libDirs;
    private LdLinker(final String command, final String[] extensions,
            final String[] ignoredExtensions, final String outputPrefix,
            final String outputSuffix, final boolean isLibtool, final LdLinker libtoolLinker) {
        super(command, "-version", extensions, ignoredExtensions, outputPrefix,
                outputSuffix, false, isLibtool, libtoolLinker);
    }
    public Linker getLinker(final LinkType type) {
        if ( type.isStaticLibrary() && !type.getUseHighlevelTool() ) {
            return GccLibrarian.getInstance();
        }
        if (type.isStaticLibrary()) {
            return arLinker;
        }
        if (type.isSharedLibrary()) {
            return dllLinker;
        }
        return instance;
    }
}
