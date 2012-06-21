
/*
  NOMADS Auksalaq NGlobals v.210
  Revised/cleaned, 6/20/2012, Steven Kemper
 */

package nomads.v210_Auk;


public class NGlobals {
    public static int clientDebugLevel = 1;  // Use this for printout info
    public static int serverDebugLevel = 1;  // Use this for printout info
    public static int libraryDebugLevel = 1;  // Use this for printout info
    public static String    serverName = "nomads.music.virginia.edu";
    public static int serverPort = 52910;
    public static int serverPortDT = 52911;
    public static int serverPortSK = 52912;
    public static int serverPortPT = 52913;
    public static int serverPortMB = 52914;

    public static void printit(String str) {
	if (clientDebugLevel > 0) {
	    System.out.println(str);
	}
    }

    public static void cPrint(String str) {
	if (clientDebugLevel > 0) {
	    System.out.println(str);
	}
    }

    public static void sPrint(String str) {
	if (serverDebugLevel > 0) {
	    System.out.println(str);
	}
    }

    public static void lPrint(String str) {
	if (libraryDebugLevel > 0) {
	    System.out.println(str);
	}
    }
    

}