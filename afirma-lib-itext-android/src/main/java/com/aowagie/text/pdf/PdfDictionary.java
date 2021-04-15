/*
 * $Id: PdfDictionary.java 3762 2009-03-06 16:53:44Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.aowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * <CODE>PdfDictionary</CODE> is the Pdf dictionary object.
 * <P>
 * A dictionary is an associative table containing pairs of objects.
 * The first element of each pair is called the <I>key</I> and the second
 * element is called the <I>value</I>.
 * Unlike dictionaries in the PostScript language, a key must be a
 * <CODE>PdfName</CODE>.
 * A value can be any kind of <CODE>PdfObject</CODE>, including a dictionary.
 * A dictionary is generally used to collect and tie together the attributes
 * of a complex object, with each key-value pair specifying the name and value
 * of an attribute.<BR>
 * A dictionary is represented by two left angle brackets (&lt;&lt;), followed by a
 * sequence of key-value pairs, followed by two right angle brackets (&gt;&gt;).<BR>
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.6 (page 59-60).
 *
 * @see		PdfObject
 * @see		PdfName
 * @see		BadPdfFormatException
 */
public class PdfDictionary extends PdfObject {

    // CONSTANTS

    /** This is a possible type of dictionary */
    private static final PdfName FONT = PdfName.FONT;

    /** This is a possible type of dictionary */
    static final PdfName OUTLINES = PdfName.OUTLINES;

    /** This is a possible type of dictionary */
    static final PdfName PAGE = PdfName.PAGE;

    /** This is a possible type of dictionary */
    private static final PdfName PAGES = PdfName.PAGES;

    /** This is a possible type of dictionary */
    static final PdfName CATALOG = PdfName.CATALOG;

    // CLASS VARIABLES

    /** This is the type of this dictionary */
    private PdfName dictionaryType = null;

    /** This is the hashmap that contains all the values and keys of the dictionary */
    protected HashMap<PdfName, PdfObject> hashMap;

    // CONSTRUCTORS

    /**
     * Constructs an empty <CODE>PdfDictionary</CODE>-object.
     */
    public PdfDictionary() {
        super(DICTIONARY);
        this.hashMap = new LinkedHashMap();
    }

    /**
     * Constructs a <CODE>PdfDictionary</CODE>-object of a certain type.
     *
     * @param type a <CODE>PdfName</CODE>
     */
    public PdfDictionary(final PdfName type) {
        this();
        this.dictionaryType = type;
        put(PdfName.TYPE, this.dictionaryType);
    }

    // METHODS OVERRIDING SOME PDFOBJECT METHODS

    /**
     * Writes the PDF representation of this <CODE>PdfDictionary</CODE> as an
     * array of <CODE>byte</CODE> to the given <CODE>OutputStream</CODE>.
     *
     * @param writer for backwards compatibility
     * @param os the <CODE>OutputStream</CODE> to write the bytes to.
     * @throws IOException on error
     */
    @Override
	public void toPdf(final PdfWriter writer, final OutputStream os) throws IOException {
        os.write('<');
        os.write('<');
        // loop over all the object-pairs in the HashMap
        PdfName key;
        PdfObject value;
        int type = 0;
        for (final Object element : this.hashMap.keySet()) {
            key = (PdfName) element;
            value = this.hashMap.get(key);
            key.toPdf(writer, os);
            type = value.type();
            if (type != PdfObject.ARRAY && type != PdfObject.DICTIONARY && type != PdfObject.NAME && type != PdfObject.STRING) {
				os.write(' ');
			}
            value.toPdf(writer, os);
        }
        os.write('>');
        os.write('>');
    }

    /**
     * Returns a string representation of this <CODE>PdfDictionary</CODE>.
     *
     * The string doesn't contain any of the content of this dictionary.
     * Rather the string "dictionary" is returned, possibly followed by the
     * type of this <CODE>PdfDictionary</CODE>, if set.
     *
     * @return the string representation of this <CODE>PdfDictionary</CODE>
     * @see com.aowagie.text.pdf.PdfObject#toString()
     */
    @Override
	public String toString() {
        if (get(PdfName.TYPE) == null) {
			return "Dictionary";
		}
        return "Dictionary of type: " + get(PdfName.TYPE);
    }

    // DICTIONARY CONTENT METHODS

    /**
     * Associates the specified <CODE>PdfObject</CODE> as <VAR>value</VAR> with
     * the specified <CODE>PdfName</CODE> as <VAR>key</VAR> in this map.
     *
     * If the map previously contained a mapping for this <VAR>key</VAR>, the
     * old <VAR>value</VAR> is replaced. If the <VAR>value</VAR> is
     * <CODE>null</CODE> or <CODE>PdfNull</CODE> the key is deleted.
     *
     * @param key a <CODE>PdfName</CODE>
     * @param object the <CODE>PdfObject</CODE> to be associated with the
     *   <VAR>key</VAR>
     */
    public void put(final PdfName key, final PdfObject object) {
        if (object == null || object.isNull()) {
			this.hashMap.remove(key);
		} else {
			this.hashMap.put(key, object);
		}
    }



    /**
     * Copies all of the mappings from the specified <CODE>PdfDictionary</CODE>
     * to this <CODE>PdfDictionary</CODE>.
     *
     * These mappings will replace any mappings previously contained in this
     * <CODE>PdfDictionary</CODE>.
     *
     * @param dic The <CODE>PdfDictionary</CODE> with the mappings to be
     *   copied over
     */
    void putAll(final PdfDictionary dic) {
        this.hashMap.putAll(dic.hashMap);
    }

    /**
     * Removes a <CODE>PdfObject</CODE> and its <VAR>key</VAR> from the
     * <CODE>PdfDictionary</CODE>.
     *
     * @param key a <CODE>PdfName</CODE>
     */
    public void remove(final PdfName key) {
        this.hashMap.remove(key);
    }

    /**
     * Returns the <CODE>PdfObject</CODE> associated to the specified
     * <VAR>key</VAR>.
     *
     * @param key a <CODE>PdfName</CODE>
     * @return the <CODE>PdfObject</CODE> previously associated to the
     *   <VAR>key</VAR>
     */
    public PdfObject get(final PdfName key) {
        return this.hashMap.get(key);
    }

    /**
     * Returns the <CODE>PdfObject</CODE> associated to the specified
     * <VAR>key</VAR>, resolving a possible indirect reference to a direct
     * object.
     *
     * This method will never return a <CODE>PdfIndirectReference</CODE>
     * object.
     *
     * @param key A key for the <CODE>PdfObject</CODE> to be returned
     * @return A direct <CODE>PdfObject</CODE> or <CODE>null</CODE>
     */
    public PdfObject getDirectObject(final PdfName key) {
        return PdfReader.getPdfObject(get(key));
    }

    /**
     * Get all keys that are set.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public Set<PdfName> getKeys() {
        return this.hashMap.keySet();
    }

    /**
     * Returns the number of <VAR>key</VAR>-<VAR>value</VAR> mappings in this
     * <CODE>PdfDictionary</CODE>.
     *
     * @return the number of <VAR>key</VAR>-<VAR>value</VAR> mappings in this
     *   <CODE>PdfDictionary</CODE>.
     */
    public int size() {
        return this.hashMap.size();
    }

    /**
     * Returns <CODE>true</CODE> if this <CODE>PdfDictionary</CODE> contains a
     * mapping for the specified <VAR>key</VAR>.
     * @param key Key
     * @return <CODE>true</CODE> if the key is set, otherwise <CODE>false</CODE>.
     */
    boolean contains(final PdfName key) {
        return this.hashMap.containsKey(key);
    }

    // DICTIONARY TYPE METHODS

    /**
     * Checks if a <CODE>Dictionary</CODE> is of the type FONT.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public boolean isFont() {
        return FONT.equals(this.dictionaryType);
    }

    /**
     * Checks if a <CODE>Dictionary</CODE> is of the type PAGE.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public boolean isPage() {
        return PAGE.equals(this.dictionaryType);
    }

    /**
     * Checks if a <CODE>Dictionary</CODE> is of the type PAGES.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public boolean isPages() {
        return PAGES.equals(this.dictionaryType);
    }

    /**
     * Checks if a <CODE>Dictionary</CODE> is of the type CATALOG.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public boolean isCatalog() {
        return CATALOG.equals(this.dictionaryType);
    }

    /**
     * Checks if a <CODE>Dictionary</CODE> is of the type OUTLINES.
     *
     * @return <CODE>true</CODE> if it is, otherwise <CODE>false</CODE>.
     */
    public boolean isOutlineTree() {
        return OUTLINES.equals(this.dictionaryType);
    }

    // OTHER METHODS

    void merge(final PdfDictionary other) {
        this.hashMap.putAll(other.hashMap);
    }

    void mergeDifferent(final PdfDictionary other) {
        for (final PdfName key : other.hashMap.keySet()) {
            if (!this.hashMap.containsKey(key)) {
				this.hashMap.put(key, other.hashMap.get(key));
			}
        }
    }

     // DOWNCASTING GETTERS
     // @author Mark A Storer (2/17/06)

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfDictionary</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfDictionary</CODE>, it is cast down and returned as
     * such. Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfDictionary</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfDictionary getAsDict(final PdfName key) {
        PdfDictionary dict = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isDictionary()) {
			dict = (PdfDictionary) orig;
		}
        return dict;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfArray</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfArray</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfArray</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfArray getAsArray(final PdfName key) {
        PdfArray array = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isArray()) {
			array = (PdfArray) orig;
		}
        return array;
    }



    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfString</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfString</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfString</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfString getAsString(final PdfName key) {
        PdfString string = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isString()) {
			string = (PdfString) orig;
		}
        return string;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfNumber</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfNumber</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfNumber</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfNumber getAsNumber(final PdfName key) {
        PdfNumber number = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isNumber()) {
			number = (PdfNumber) orig;
		}
        return number;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfName</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfName</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfName</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfName getAsName(final PdfName key) {
        PdfName name = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isName()) {
			name = (PdfName) orig;
		}
        return name;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfBoolean</CODE>,
     * resolving indirect references.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * and resolved to a direct object.
     * If it is a <CODE>PdfBoolean</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfBoolean</CODE> object,
     *   or <CODE>null</CODE>
     */
    PdfBoolean getAsBoolean(final PdfName key) {
        PdfBoolean bool = null;
        final PdfObject orig = getDirectObject(key);
        if (orig != null && orig.isBoolean()) {
			bool = (PdfBoolean)orig;
		}
        return bool;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfIndirectReference</CODE>.
     *
     * The object associated with the <CODE>PdfName</CODE> given is retrieved
     * If it is a <CODE>PdfIndirectReference</CODE>, it is cast down and returned
     * as such. Otherwise <CODE>null</CODE> is returned.
     *
     * @param key A <CODE>PdfName</CODE>
     * @return the associated <CODE>PdfIndirectReference</CODE> object,
     *   or <CODE>null</CODE>
     */
    PdfIndirectReference getAsIndirectObject(final PdfName key) {
        PdfIndirectReference ref = null;
        final PdfObject orig = get(key); // not getDirect this time.
        if (orig != null && orig.isIndirect()) {
			ref = (PdfIndirectReference) orig;
		}
        return ref;
    }
}