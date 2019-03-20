/*******************************************************************************

 � 2007 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD SCT AND IS NOT TO BE COPIED,
 REPRODUCED, LENT, OR DISPOSED OF, NOR USED FOR ANY PURPOSE OTHER THAN THAT
 WHICH IT IS SPECIFICALLY PROVIDED WITHOUT THE WRITTEN PERMISSION OF THE
 SAID COMPANY
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend.automation

import java.text.FieldPosition
import java.text.MessageFormat
import java.text.NumberFormat

/**
 * Utilities to manage Strings, return Strings from various sources.
 *
 * @author Michael Brzycki
 **/
public class StringHelper {


    public static final String NEW_LINE = System.getProperty("line.separator", "\n");


    public static void main( String[] args ) throws IOException {
        String filePath = "v://library.css";
        String str1 = StringHelper.readFileToString( filePath );
        System.out.println( "Contents: \n" + str1 );
//        String filePath2 = "http://utenti.tripod.it/yanorel6/2/ch30.htm";
// java -Dhttp.proxyHost=149.24.28.4 -Dhttp.proxyPort=8080
// com.sctcorp.library.common.util.StringHelper
        /*
        String filePath2 = "http://www.yahoo.com";
        String str2 = StringHelper.readURLToString( filePath2 );
        System.out.println( "str2: " + str2 );
        */
        String filePath3 = "http://127.0.0.1:8000";
        String str3 = StringHelper.readURLToString( filePath3 );
        System.out.println( "Contents: \n" + str3 );
    }


    /**
     * From:
     * http://www.gjt.org/servlets/JCVSlet/show/gjt/com/starfarer/util/Strings.java/1.1.1.1
     *
     * Capitalizes the first letter of a string. If the first letter is already capitalized or if it
     * has no capital form, then the original string is returned instead.
     * @throws NullPointerException if <TT>s</TT> is <TT>null</TT>
     */
    public static String capitalize(String s) {
        char c = s.charAt(0);
        char d = Character.toTitleCase(c);
        if (c == d) {
            return s;
        }

        StringBuffer sb = new StringBuffer(s);
        sb.setCharAt(0, d);
        return sb.toString();
    }


    /**
     * Get everything after the last delimiter.
     **/
    public static String getAfterLastDelim( String text, String delim ) {
        int lastDelimPos = getLastIndexOf( text, delim );
        String after = text.substring( lastDelimPos + delim.length(), text.length() );
        return after;
    }


    /**
     * Get everything after a prefix.
     **/
    public static String getAfterPrefix( String text, String prefix ) {
        int lastDelimPos = getLastIndexOf( text, prefix );
        String after = text.substring( lastDelimPos + prefix.length(), text.length() );
        return after;
    }


    /**
     * Get everything before the last delimiter.
     **/
    public static String getBeforeLastDelim( String text, String delim ) {
        int lastDelimPos = getLastIndexOf( text, delim );
        String after = text.substring( 0, lastDelimPos );
        return after;
    }


    /**
     * Get everything before a prefix.
     **/
    public static String getBeforePrefix( String text, String prefix ) {
        int prefixPosition = text.indexOf( prefix );
        String before = text.substring( 0, prefixPosition );
        return before;
    }


    /**
     * Get text between two delimiting text
     **/
    public static String getBetweenDelims( String text,
                                           String leftDelim, String rightDelim ) {
        int idxLB = -1;
        int idxRB = -1;
        String nestedText = "";
        idxLB = text.indexOf( leftDelim );
        idxRB = text.indexOf( rightDelim, idxLB + leftDelim.length() );
        nestedText = text.substring( idxLB + leftDelim.length(), idxRB );
        return nestedText;
    }


    /**
     * Format file length as kilobytes.
     * @param lengthInBytes
     */
    public static String getFileLengthInBytesAsKiloBytes( long lengthInBytes ) {
        long size = lengthInBytes / 1024l;
        size = (size == 0l && lengthInBytes > 0) ? 1l: size;
        return NumberFormat.getInstance().format( size );
    }


    /**
     * Get the number of instances of a substring in a string.
     * @param text
     * @param substr
     **/
    public static int getInstancesOf( String text, String substr ) {
        int instances = 0;
        int idxSubStr = -1;
        int lastFoundIdx = -1;
        int nextSearch = -1;
        boolean first = true;
        while ((first)||(nextSearch != -1)) {
            first = false;
            idxSubStr = text.indexOf( substr, nextSearch + 1 );
            if (idxSubStr != -1) {
                lastFoundIdx = idxSubStr;
                instances++;
            }
            nextSearch = idxSubStr;
        }
        return instances;
    }


    /**
     * Get the last index of a string in a string.
     **/
    public static int getLastIndexOf( String text, String search ) {
        int nextSearch = 0;
        int idxSearch = -1;
        int found = -1;
        while ( nextSearch != -1 ) {
            idxSearch = text.indexOf( search, nextSearch + 1 );
            if ( idxSearch != -1 ) {
                found = idxSearch;
            }
            nextSearch = idxSearch;
        }
        return found;
    }


    /**
     * Get the last part after the last period for each list element and restore.
     **/
    public static String[] getListOfAfterLastPeriod( String[] list ) {
        String origElt = "";
        String newElt = "";
        String[] newList = new String[list.length];
        for ( int i = 0; i < list.length; i++ ) {
            origElt = list[i];
            newElt = getAfterLastDelim( origElt, "." );
            newList[i] = newElt;
        }
        return newList;
    }


    /**
     * Convert a CSV or dotted list to a string array.
     * format: dot.dot.list
     **/
    public static String[] getListFromCSV( String text, String separator ) {

        if (text.length() == 0) {
            return null;
        }

        int size = getInstancesOf( text, separator );
        int count = 0;
        int listSize = size + 1;
        int nextSearch = 0;
        int idxSeparator = -1;
        boolean first = true;
        String[] list = new String[listSize];
        String elt = "";

        if (listSize == 1) {
            list[listSize - 1] = text;
            return list;
        }

        while (nextSearch != -1) {
            idxSeparator = text.indexOf( separator, nextSearch + 1 );
            if (idxSeparator != -1) {
                if (first == true) {
                    elt = text.substring( nextSearch, idxSeparator );
                    first = false;
                } else {
                    elt = text.substring( nextSearch + 1, idxSeparator );
                }
            }

            if (idxSeparator == -1) {
                elt = text.substring( nextSearch + 1, text.length() );
            }

            list[count++] = elt;
            nextSearch = idxSeparator;
        }
        return list;
    }


    /**
     * Get the length of the longest string.
     * @param list
     *
     * @return int
     */
    public static int getLongestLengthInList( String[] list ) {
        int maxLength = -1;
        for (int i = 0; i < list.length; i++) {
            int length = list[i].trim().length();
            if (length > maxLength) maxLength = length;
        }
        return maxLength;
    }


    /**
     * Get random key.
     **/
    public static String getRandomKey() {
        final int length = 5;
        return StringHelper.random( length );
    }


    /**
     * Checks to see that a string have a value greater than 0.
     * @param s
     **/
    public static boolean hasValue( String s ) {
        return ((s != null) && (s.length() > 0));
    }


    /**
     * Returns true if the string is a valid java identifier
     * @param string the String to check
     **/
    public static boolean isJavaIdentifier( String string ) {
        if (string.length() <= 0) return false;
        if (!Character.isJavaIdentifierStart(string.charAt(0))) return false;
        for (int i = 1; i < string.length(); i++) {
            if (!Character.isJavaIdentifierPart(string.charAt(i)) ) return false;
        }
        return true;
    }


    /**
     * Load property file.
     * @param propertyFilePath
     *
     * @return Properties
     */
    public static Properties loadPropertyFile( String propertyFilePath ) {
        if (propertyFilePath == null) throw new IllegalArgumentException( "propertyFilePath is null!" );

        Properties properties = new Properties();

        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream( propertyFilePath );
            properties.load( inStream );
        } catch (FileNotFoundException e) {
            inStream = null;
        } catch (IOException e) {
            inStream = null;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) { }
                inStream = null;
            }
        }
        return properties;
    }


    /**
     * Open a file for output.
     * @param fn the path to the file.
     * @return print writer for file.
     **/
    public static PrintWriter openOutputFile( String fn ) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter( fn );
            bw = new BufferedWriter( fw );
            pw = new PrintWriter( bw );
        } catch ( Exception e ) {
            System.out.println("Exception: " + e );
            e.printStackTrace();
        }
        return pw;
    }


    /**
     * Pack contents in <tag></tag>
     **/
    public static String packInTag( String tag, String contents ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "<" )
        .append( tag )
        .append( ">\n" )
        .append( contents )
        .append( "</" )
        .append( tag )
        .append( ">\n" );
        return sb.toString();
    }


    /**
     * Pack contents in <tag ID="xyz"></tag>
     **/
    public static String packInTag( String tag, String ID, String contents ) {
        StringBuffer sb = new StringBuffer();
        sb.append( tag )
        .append( " ID=\"" )
        .append( ID )
        .append( "\"" )
        .append( ">\n" )
        .append( contents )
        .append( "</" )
        .append( tag )
        .append( ">\n" );
        return sb.toString();
    }


    /**
     * Move this to StringHelper. Copied from HelpFileBuilder
     *
     * @param text
     *
     * @return String[]
     */
    public static String[] parseLines( String text ) {
        StringTokenizer tokenizer = new StringTokenizer( text, NEW_LINE );
        ArrayList lines = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            lines.add( line );
        }
        return (String[]) lines.toArray( new String[0] );
    }


    /**
     * Generate a string composed of random characters.
     *
     * @param length Length of the random string
     **/
    public static String random( int length ) {
        final int HI_RANGE = 36;
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < length; i++ ) {
            double d1 = Math.floor(java.lang.Math.random() * HI_RANGE);
            Double D1 = new Double( d1 );
            int i1 = D1.intValue();
            sb.append( Character.forDigit(i1, HI_RANGE) );
        }
        return sb.toString();
    }


    /**
     * Generate a string with a prefix and random characters.
     * For example, name_avadgv, name_vweksd, etc.
     * @param prefix
     * @param length
     *
     * @return String
     */
    public static String random( String prefix, int length ) {
        StringBuffer sb = new StringBuffer();
        sb.append( prefix );
        sb.append( random( length ) );
        return sb.toString();
    }


    /**
     * Read a file.
     * @param fn the path to the file
     * @return reader to file specified.
     **/
    public static BufferedReader readFileToBuffer( String fn ) {
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader( fn );
            br = new BufferedReader( fr );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return br;
    }


    /**
     * Read in a file and output as String.
     **/
    public static String readFileToString( String filePath ) throws IOException {
        return readFileToString( new File( filePath ) );
    }


    /**
     * Read in a file and output as String.
     **/
    public static String readFileToString( File file ) throws IOException {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        String line = null;
        FileReader fr = new FileReader( file );
        br = new BufferedReader( fr );
        while (( line = br.readLine()) != null) {
            sb.append( line ).append( "\n" );
        }
        return sb.toString();
    }


    /**
     * Read in HTTP URL and output as String.
     * Note: will not work with external unless proxy settings specified.
     *
     * Source: Java 1.2 Unleashed, Ch. 30:
     * http://utenti.tripod.it/yanorel6/2/ch30.htm
     **/
    public static String readURLToString( String httpPath ) {
        StringBuffer sb = new StringBuffer();
        try {
            System.out.println( "Fetching URL [" + httpPath + "]" );
            URL url = new URL( httpPath );
            BufferedReader inStream = new BufferedReader(
                new InputStreamReader( url.openStream() ) );
            String line;
            while ( (line = inStream.readLine() )!= null ) {
                sb.append( line ).append( "\n" );
            }
            inStream.close();
        } catch ( MalformedURLException e1 ){
            System.out.println( "Bad URL" );
            e1.printStackTrace();
        } catch ( IOException e2 ){
            System.out.println( "IOException occurred." );
            e2.printStackTrace();
        } catch ( Exception e3 ) {
            e3.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * Replace special formatting characters.
     * @param text
     */
    public static String replaceFormattingCharacters( String text ) {
        // don't use the NEW_LINE constant - use \n explicitly
        String newLinesReplaced = StringHelper.replaceString( text, "\n", " " /* one space */ );
        return StringHelper.replaceString( newLinesReplaced, "\t", " " /* one space */ );
    }


    /**
     * Replaces all occurances of a given substring with another substring in a String object.
     *
     * From:
     * http://www.planet-source-code.com/xq/ASP/txtCodeId.2433/lngWId.2/qx/vb/scripts/ShowCode.htm
     * @param aSearch
     * @param aFind
     * @param aReplace
     *
     * @return String
     */
    public static String replaceString( String aSearch,
                                        String aFind,
                                        String aReplace ) {
        String result = aSearch;
        if (result != null && result.length() > 0) {
            int a = 0;
            int b = 0;
            while (true) {
                a = result.indexOf(aFind, b);
                if (a != -1) {
                    result = result.substring(0, a) + aReplace + result.substring(a + aFind.length());
                    b = a + aReplace.length();
                }
                else
                    break;
            }
        }
        return result;
    }


    /**
     * Very helpful method to store the exception stack trace as a String.
     *
     * source: http://www.drbob42.com/JBuilder/jbjar062.htm
     **/
    public static String stackTraceToString( Throwable e ) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        StringBuffer error = sw.getBuffer();
        String errorStr = error.toString();
        // replace 'Integer.<init>' with 'Integer.[init]'
        errorStr = errorStr.replace( '<', '[' );
        errorStr = errorStr.replace( '>', ']' );
        return errorStr;
    }


    /**
     * Replace numbered tokens in text with values in list.
     * The {1} is invalid -> The activity is invalid
     * @param text
     * @param substitutionValues
     *
     * @return String
     */
    public static String substituteParameters( String text,
                                               Serializable[] substitutionValues ) {
        MessageFormat format = new MessageFormat( text );
        StringBuffer substitutedText = new StringBuffer( "" );
        format.format( substitutionValues, substitutedText, new FieldPosition(0) );
        return substitutedText.toString();
    }


    public static String substituteParametersWithEscape( String text, Serializable[] substitutionValues ) {
        return substituteParameters( escape( text ), substitutionValues );
    }


    /**
     * Write text to file.
     * @param text
     */
    public static void writeToFile( String fileName, String text ) {
        PrintWriter pw = openOutputFile( fileName );
        if ( pw != null ) {
            pw.write( text );
            pw.flush();
            pw.close();
        }
    }


    /**
     * Make a separated list.
     * @param list
     * @param separator
     *
     * @return String
     */
    public static String toSeparatedList( Object[] list, String separator ) {
        if (list == null) throw new IllegalArgumentException( "list is null!" );
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.length; i++) {
            sb.append( String.valueOf(list[i]) );
            if (i != (list.length - 1)) sb.append( separator );
        }
        return sb.toString();
    }



    /**
     * Create a word-wrapped version of a String. Wrap at a specified width and
     * use newlines as the delimiter. If a word is over the width in lenght
     * use a - sign to split it.
     */
    public static String wordWrap( String str, int width ) {
        return wordWrap( str, width, NEW_LINE );
    }


    /**
     * Word-wrap a string.
     *
     * @param str   String to word-wrap
     * @param width int to wrap at
     * @param delim String to use to separate lines
     *
     * @return String that has been word wrapped
     */
    public static String wordWrap(String str, int width, String delim ) {
        int sz = str.length();

        /// shift width up one. mainly as it makes the logic easier
        width++;

        // our best guess as to an initial size
        StringBuffer buffer = new StringBuffer( sz/width * delim.length() + sz);

        // every line will include a delim on the end
        width = width - delim.length();

        int idx = -1;
        String substr = null;

        // beware: i is rolled-back inside the loop
        for(int i = 0; i < sz; i += width) {

            // on the last line
            if(i > sz - width) {
                buffer.append( str.substring(i) );
                break;
            }

            // the current line
            substr = str.substring( i, i + width );

            // is the delim already on the line
            idx = substr.indexOf( delim );
            if(idx != -1) {
                buffer.append( substr.substring(0 ,idx ) );

                buffer.append( delim );
                i -= width - idx -delim.length();


                // Erase a space after a delim. Is this too obscure?
                if(substr.length() > idx+1) {
                    if(substr.charAt(idx+1) != '\n') {
                        if(Character.isWhitespace(substr.charAt(idx + 1))) {
                            i++;
                        }
                    }
                }

                continue;
            }

            idx = -1;

            // figure out where the last space is
            char[] chrs = substr.toCharArray();
            for(int j=width; j>0; j--) {
                if(Character.isWhitespace(chrs[j - 1])) {
                    idx = j;
                    break;
                }
            }


            if(idx == -1) {
                for(int j=width; j>0; j--) {
                    if(chrs[j-1] == '-') {
                        idx = j;
                        break;
                    }
                }
                if(idx == -1) {
                    buffer.append(substr);
                    buffer.append(delim);
                } else {
                    if(idx != width) {
                        idx++;
                    }
                    buffer.append(substr.substring(0,idx));
                    buffer.append(delim);
                    i -= width-idx;
                }
            } else {
                // insert spaces
                buffer.append(substr.substring( 0,idx ));
                buffer.append( repeat( " ", width-idx ));
                buffer.append(delim);
                i -= width-idx;
            }
        }

        return buffer.toString();
    }


    public static String repeat( String str, int count ) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < count; i++) {
            sb.append( str );
        }

        return sb.toString();
    }

    /**
     * Splits the specified string into an array of strings in which no element of the array is larger than limit.
     */
    public static String[] split( String source, int limit ) {
        if (source.length() == 0) {
            String[] result = new String[1]
            result[0] = ""
            return result
        }
        String remainingValue = source;
        ArrayList pieces = new ArrayList();
        while (remainingValue.length() > 0) {
            if (remainingValue.length() > limit) {
                String chunk = remainingValue.substring( 0, limit );
                remainingValue = remainingValue.substring( limit );
                pieces.add( chunk );
            } else {
                pieces.add( remainingValue );
                break;
            }
        }

        return (String[]) pieces.toArray( new String[0] );

    }


    private static String escape( String string ) {
        if ((string == null) || (string.indexOf('\'') < 0)) {
            return string;
        }

        int n = string.length();
        StringBuffer sb = new StringBuffer(n);

        for (int i = 0; i < n; i++) {
            char ch = string.charAt(i);

            if (ch == '\'') {
                sb.append('\'');
            }

            sb.append(ch);
        }

        return sb.toString();

    }

}
