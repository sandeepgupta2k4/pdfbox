/*
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.encoding;

import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

/**
 * This will perform the encoding from a dictionary.
 *
 * @author Ben Litchfield
 */
public class DictionaryEncoding extends Encoding
{
    private final COSDictionary encoding;
    private final Encoding baseEncoding;
    private final Map<Integer, String> differences = new HashMap<Integer, String>();

    /**
     * Creates a new DictionaryEncoding for embedding.
     */
    public DictionaryEncoding(COSName baseEncoding, COSArray differences)
    {
        encoding = new COSDictionary();
        encoding.setItem(COSName.NAME, COSName.ENCODING);
        encoding.setItem(COSName.DIFFERENCES, differences);
        if (baseEncoding != COSName.STANDARD_ENCODING)
        {
            encoding.setItem(COSName.BASE_ENCODING, baseEncoding);
            this.baseEncoding = Encoding.getInstance(baseEncoding);
        }
        else
        {
            this.baseEncoding = Encoding.getInstance(baseEncoding);
        }
    }

    /**
     * Creates a new DictionaryEncoding from a PDF.
     *
     * @param fontEncoding The encoding dictionary.
     */
    public DictionaryEncoding(COSDictionary fontEncoding, boolean isNonSymbolic, Encoding builtIn)
    {
        encoding = fontEncoding;

        if (encoding.containsKey(COSName.BASE_ENCODING))
        {
            COSName name = encoding.getCOSName(COSName.BASE_ENCODING);
            baseEncoding = Encoding.getInstance(name);
        }
        else if (isNonSymbolic)
        {
            // Otherwise, for a nonsymbolic font, it is StandardEncoding
            baseEncoding = StandardEncoding.INSTANCE;
        }
        else
        {
            // and for a symbolic font, it is the font's built-in encoding.
            if (builtIn != null)
            {
                baseEncoding = builtIn;
            }
            else
            {
                throw new IllegalArgumentException("Built-in Encoding required for symbolic font");
            }
        }

        codeToName.putAll( baseEncoding.codeToName );
        names.addAll( baseEncoding.names );

        // now replace with the differences
        COSArray differences = (COSArray)encoding.getDictionaryObject( COSName.DIFFERENCES );
        int currentIndex = -1;
        for( int i=0; differences != null && i<differences.size(); i++ )
        {
            COSBase next = differences.getObject( i );
            if( next instanceof COSNumber )
            {
                currentIndex = ((COSNumber)next).intValue();
            }
            else if( next instanceof COSName )
            {
                COSName name = (COSName)next;
                add(currentIndex, name.getName());
                this.differences.put(currentIndex, name.getName());
                currentIndex++;
            }
        }
    }

    /**
     * Returns the base encoding.
     */
    public Encoding getBaseEncoding()
    {
        return baseEncoding;
    }

    /**
     * Returns the Differences array.
     */
    public Map<Integer, String> getDifferences()
    {
        return differences;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return encoding;
    }
}
