package net.sf.antcontrib.cpptasks.gcc;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;

public abstract class GnuLinker extends AbstractLdLinker {

    static String[] darwinLinkerOptions = new String[]{"-arch", "-weak_framework", "-lazy_framework", "-weak_library" };
    static String[] clangLinkerOptions = new String[]{"-target" };

    protected File[] libDirs;

    public GnuLinker(final String command, final String identifierArg, final String[] extensions,
            final String[] ignoredExtensions, final String outputPrefix,
            final String outputSuffix, final boolean isXCoderun, final boolean isLibtool,
            final AbstractLdLinker libtoolLinker) {
        super(command, identifierArg, extensions, ignoredExtensions,
                outputPrefix, outputSuffix, isXCoderun, isLibtool,
                libtoolLinker);
    }

    public GnuLinker(final AbstractLdLinker ld, final boolean isXCoderun) {
        super(ld, isXCoderun);
    }

    protected abstract String[] getStaticLinkerOptions();
    protected abstract GnuLinker getStaticDllLinker();
    protected abstract GnuLinker getStaticDllClangLinker();
    protected abstract GnuLinker getStaticArLinker();
    protected abstract GnuLinker getStaticArClangLinker();
    protected abstract GnuLinker getStaticClangInstance();
    protected abstract GnuLinker getStaticXcodeClangInstance();
    protected abstract GnuLinker getStaticMachBundleLinker();
    protected abstract GnuLinker getStaticMachClangBundleLinker();
    protected abstract GnuLinker getStaticXcodeMachClangBundleLinker();
    protected abstract GnuLinker getStaticMachDllLinker();
    protected abstract GnuLinker getStaticMachDllClangLinker();
    protected abstract GnuLinker getStaticXcodeMachDllClangLinker();
    protected abstract GnuLinker getStaticMachArLinker();
    protected abstract GnuLinker getStaticMachArClangLinker();
    protected abstract GnuLinker getStaticXcodeMachArClangLinker();
    protected abstract GnuLinker getStaticInstance();

    @Override
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
    }

    /**
     * Allows drived linker to decorate linker option. Override by GccLinker to
     * prepend a "-Wl," to pass option to through gcc to linker.
     *
     * @param buf
     *            buffer that may be used and abused in the decoration process,
     *            must not be null.
     * @param arg
     *            linker argument
     */
    @Override
    public String decorateLinkerOption(final StringBuffer buf, final String arg) {
        if (arg.startsWith("--sysroot")) {
          return arg;
        }
        if (arg.startsWith("-nostdlib")) {
          return arg;
        }
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
                case 'v' :
                    break;
                default :
                    boolean known = false;
                    final HashSet<String> allLinkerOptions = new HashSet<String>();
                    allLinkerOptions.addAll(Arrays.asList(getStaticLinkerOptions()));
                    if( isCLANG() ) {
                        allLinkerOptions.addAll(Arrays.asList(clangLinkerOptions));
                    }
                    if (isDarwin()) {
                        allLinkerOptions.addAll(Arrays.asList(darwinLinkerOptions));
                    }
                    known = allLinkerOptions.contains(arg);

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

    @Override
    public Linker getLinker(final LinkType type) {
        if ( type.isStaticLibrary() && !type.getUseHighlevelTool() ) {
            return GccLibrarian.getInstance(); // uses 'ar', which is 'gcc' agnostic
        }
        if (type.isStaticLibrary()) {
            if (isDarwin()) {
                return isGNU() ? getStaticMachArLinker() : ( isXcodeRun() ? getStaticXcodeMachArClangLinker() : getStaticMachArClangLinker() );
            } else {
                return isGNU() ? getStaticArLinker() : getStaticArClangLinker();
            }
        }
        if (type.isPluginModule()) {
            if (isDarwin()) {
                return isGNU() ? getStaticMachBundleLinker() : ( isXcodeRun() ? getStaticXcodeMachClangBundleLinker() : getStaticMachClangBundleLinker() );
            } else {
                return isGNU() ? getStaticDllLinker() : getStaticDllClangLinker();
            }
        }
        if (type.isSharedLibrary()) {
            if (isDarwin()) {
                return isGNU() ? getStaticMachDllLinker() : ( isXcodeRun() ? getStaticXcodeMachDllClangLinker() : getStaticMachDllClangLinker() );
            } else {
                return isGNU() ? getStaticDllLinker() : getStaticDllClangLinker();
            }
        }
        return isGNU() ? getStaticInstance() : ( isXcodeRun() ? getStaticXcodeClangInstance() : getStaticClangInstance() ) ;
    }

}