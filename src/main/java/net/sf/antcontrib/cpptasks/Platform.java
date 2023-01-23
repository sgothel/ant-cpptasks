package net.sf.antcontrib.cpptasks;

/**
 * Platform provides certain OS and architecture properties of the runtime host platform.
 */
public class Platform {
    /**
     * The architecture name, , i.e. Java's `os.arch` property.
     */
    public static final String OS_ARCH;

    /**
     * The OS name, , i.e. Java's `os.name` property.
     * <p>In case of {@link OSType#ANDROID}, see {@link #OS_TYPE}, the OS name is Linux</p>
     */
    public static final String OS_NAME;

    /**
     * The OS version string, i.e. Java's `os.version` property.
     */
    public static final String OS_VERSION;

    /**
     * The OS type, derived from {@link #OS_NAME}
     * <p>In case of {@link OSType#ANDROID} the {@link #getOSName() OS name}, is Linux</p>
     */
    public static final OSType OS_TYPE;

    static {
        OS_ARCH = System.getProperty("os.arch");
        OS_NAME =  System.getProperty("os.name");
        OS_VERSION =  System.getProperty("os.version");
        OS_TYPE = getOSTypeImpl(OS_NAME.toLowerCase(), false /* isAndroid */);
    }

    private static final OSType getOSTypeImpl(final String osLower, final boolean isAndroid) throws RuntimeException {
        if ( isAndroid ) {
            return OSType.ANDROID;
        }
        if ( osLower.startsWith("linux") ) {
            return OSType.LINUX;
        }
        if ( osLower.startsWith("freebsd") ) {
            return OSType.FREEBSD;
        }
        if ( osLower.startsWith("android") ) {
            return OSType.ANDROID;
        }
        if ( osLower.startsWith("mac os x") ||
             osLower.startsWith("darwin") ) {
            return OSType.MACOS;
        }
        if ( osLower.startsWith("sunos") ) {
            return OSType.SUNOS;
        }
        if ( osLower.startsWith("hp-ux") ) {
            return OSType.HPUX;
        }
        if ( osLower.startsWith("windows") ) {
            return OSType.WINDOWS;
        }
        if ( osLower.startsWith("kd") ) {
            return OSType.OPENKODE;
        }
        if ( osLower.startsWith("ios") ) {
            return OSType.IOS;
        }
        throw new RuntimeException("Please port OS detection to your platform (" + OS_NAME + ")");
    }
}
