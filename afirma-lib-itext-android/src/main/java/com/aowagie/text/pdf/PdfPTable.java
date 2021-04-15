/*
 * $Id: PdfPTable.java 4010 2009-07-07 11:05:23Z blowagie $
 *
 * Copyright 2001, 2002 Paulo Soares
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

import java.util.ArrayList;

import com.aowagie.text.DocumentException;
import com.aowagie.text.Element;
import com.aowagie.text.LargeElement;
import com.aowagie.text.Phrase;
import com.aowagie.text.pdf.events.PdfPTableEventForwarder;

/**
 * This is a table that can be put at an absolute position but can also
 * be added to the document as the class <CODE>Table</CODE>.
 * In the last case when crossing pages the table always break at full rows; if a
 * row is bigger than the page it is dropped silently to avoid infinite loops.
 * <P>
 * A PdfPTableEvent can be associated to the table to do custom drawing
 * when the table is rendered.
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class PdfPTable implements LargeElement{

    /**
     * The index of the original <CODE>PdfcontentByte</CODE>.
     */
    private static final int BASECANVAS = 0;

    /**
     * The index of the duplicate <CODE>PdfContentByte</CODE> where the background will be drawn.
     */
    public static final int BACKGROUNDCANVAS = 1;

    /**
     * The index of the duplicate <CODE>PdfContentByte</CODE> where the border lines will be drawn.
     */
    public static final int LINECANVAS = 2;

    /**
     * The index of the duplicate <CODE>PdfContentByte</CODE> where the text will be drawn.
     */
    static final int TEXTCANVAS = 3;

    private ArrayList rows = new ArrayList();
    private float totalHeight = 0;
    private PdfPCell currentRow[];
    private int currentRowIdx = 0;
    private PdfPCell defaultCell = new PdfPCell((Phrase)null);
    private float totalWidth = 0;
    private float relativeWidths[];
    private float absoluteWidths[];
    private PdfPTableEvent tableEvent;

    /**
     * Holds value of property headerRows.
     */
    private int headerRows;

    /**
     * Holds value of property widthPercentage.
     */
    private float widthPercentage = 80;

    /**
     * Holds value of property horizontalAlignment.
     */
    private int horizontalAlignment = Element.ALIGN_CENTER;

    /**
     * Holds value of property skipFirstHeader.
     */
    private boolean skipFirstHeader = false;
    /**
     * Holds value of property skipLastFooter.
     * @since	2.1.6
     */
    private boolean skipLastFooter = false;

    private boolean isColspan = false;

    private int runDirection = PdfWriter.RUN_DIRECTION_DEFAULT;

    /**
     * Holds value of property lockedWidth.
     */
    private boolean lockedWidth = false;

    /**
     * Holds value of property splitRows.
     */
    private boolean splitRows = true;

    /**
     * The spacing before the table.
     */
    private float spacingBefore;

    /**
     * The spacing after the table.
     */
    private float spacingAfter;

    /**
     * Holds value of property extendLastRow.
     */
    private boolean extendLastRow;

    /**
     * Holds value of property headersInEvent.
     */
    private boolean headersInEvent;

    /**
     * Holds value of property splitLate.
     */
    private boolean splitLate = true;

    /**
     * Defines if the table should be kept
     * on one page if possible
     */
    private boolean keepTogether;

    /**
     * Indicates if the PdfPTable is complete once added to the document.
     *
     * @since	iText 2.0.8
     */
    private boolean complete = true;

    /**
     * Holds value of property footerRows.
     */
    private int footerRows;

    /**
     * Keeps track of the completeness of the current row.
     * @since	2.1.6
     */
    private boolean rowCompleted = true;

    protected PdfPTable() {
    }

    /**
     * Constructs a <CODE>PdfPTable</CODE> with the relative column widths.
     *
     * @param relativeWidths the relative column widths
     */
    public PdfPTable(final float relativeWidths[]) {
        if (relativeWidths == null) {
			throw new NullPointerException("The widths array in PdfPTable constructor can not be null.");
		}
        if (relativeWidths.length == 0) {
			throw new IllegalArgumentException("The widths array in PdfPTable constructor can not have zero length.");
		}
        this.relativeWidths = new float[relativeWidths.length];
        System.arraycopy(relativeWidths, 0, this.relativeWidths, 0, relativeWidths.length);
        this.absoluteWidths = new float[relativeWidths.length];
        calculateWidths();
        this.currentRow = new PdfPCell[this.absoluteWidths.length];
        this.keepTogether = false;
    }

    /**
     * Constructs a <CODE>PdfPTable</CODE> with <CODE>numColumns</CODE> columns.
     *
     * @param numColumns the number of columns
     */
    public PdfPTable(final int numColumns) {
        if (numColumns <= 0) {
			throw new IllegalArgumentException("The number of columns in PdfPTable constructor must be greater than zero.");
		}
        this.relativeWidths = new float[numColumns];
        for (int k = 0; k < numColumns; ++k) {
			this.relativeWidths[k] = 1;
		}
        this.absoluteWidths = new float[this.relativeWidths.length];
        calculateWidths();
        this.currentRow = new PdfPCell[this.absoluteWidths.length];
        this.keepTogether = false;
    }

    /**
     * Constructs a copy of a <CODE>PdfPTable</CODE>.
     *
     * @param table the <CODE>PdfPTable</CODE> to be copied
     */
    PdfPTable(final PdfPTable table) {
        copyFormat(table);
        for (int k = 0; k < this.currentRow.length; ++k) {
            if (table.currentRow[k] == null) {
				break;
			}
            this.currentRow[k] = new PdfPCell(table.currentRow[k]);
        }
        for (int k = 0; k < table.rows.size(); ++k) {
            PdfPRow row = (PdfPRow)table.rows.get(k);
            if (row != null) {
				row = new PdfPRow(row);
			}
            this.rows.add(row);
        }
    }

    /**
     * Makes a shallow copy of a table (format without content).
     *
     * @param table Table
     * @return a shallow copy of the table
     */
    static PdfPTable shallowCopy(final PdfPTable table) {
        final PdfPTable nt = new PdfPTable();
        nt.copyFormat(table);
        return nt;
    }

    /**
     * Copies the format of the sourceTable without copying the content.
     *
     * @param sourceTable Table
	 * @since	2.1.6 private is now protected
	 */
    private void copyFormat(final PdfPTable sourceTable) {
        this.relativeWidths = new float[sourceTable.getNumberOfColumns()];
        this.absoluteWidths = new float[sourceTable.getNumberOfColumns()];
        System.arraycopy(sourceTable.relativeWidths, 0, this.relativeWidths, 0, getNumberOfColumns());
        System.arraycopy(sourceTable.absoluteWidths, 0, this.absoluteWidths, 0, getNumberOfColumns());
        this.totalWidth = sourceTable.totalWidth;
        this.totalHeight = sourceTable.totalHeight;
        this.currentRowIdx = 0;
        this.tableEvent = sourceTable.tableEvent;
        this.runDirection = sourceTable.runDirection;
        this.defaultCell = new PdfPCell(sourceTable.defaultCell);
        this.currentRow = new PdfPCell[sourceTable.currentRow.length];
        this.isColspan = sourceTable.isColspan;
        this.splitRows = sourceTable.splitRows;
        this.spacingAfter = sourceTable.spacingAfter;
        this.spacingBefore = sourceTable.spacingBefore;
        this.headerRows = sourceTable.headerRows;
        this.footerRows = sourceTable.footerRows;
        this.lockedWidth = sourceTable.lockedWidth;
        this.extendLastRow = sourceTable.extendLastRow;
        this.headersInEvent = sourceTable.headersInEvent;
        this.widthPercentage = sourceTable.widthPercentage;
        this.splitLate = sourceTable.splitLate;
        this.skipFirstHeader = sourceTable.skipFirstHeader;
        this.skipLastFooter = sourceTable.skipLastFooter;
        this.horizontalAlignment = sourceTable.horizontalAlignment;
        this.keepTogether = sourceTable.keepTogether;
        this.complete = sourceTable.complete;
    }

    /**
     * Sets the relative widths of the table.
     *
     * @param relativeWidths the relative widths of the table.
     * @throws DocumentException if the number of widths is different than the number
     * of columns
     */
    public void setWidths(final float relativeWidths[]) throws DocumentException {
        if (relativeWidths.length != getNumberOfColumns()) {
			throw new DocumentException("Wrong number of columns.");
		}
        this.relativeWidths = new float[relativeWidths.length];
        System.arraycopy(relativeWidths, 0, this.relativeWidths, 0, relativeWidths.length);
        this.absoluteWidths = new float[relativeWidths.length];
        this.totalHeight = 0;
        calculateWidths();
        calculateHeights(true);
    }

    /**
     * Sets the relative widths of the table.
     *
     * @param relativeWidths the relative widths of the table.
     * @throws DocumentException if the number of widths is different than the number
     * of columns
     */
    public void setWidths(final int relativeWidths[]) throws DocumentException {
        final float tb[] = new float[relativeWidths.length];
        for (int k = 0; k < relativeWidths.length; ++k) {
			tb[k] = relativeWidths[k];
		}
        setWidths(tb);
    }

	/**
	 * @since	2.1.6 private is now protected
	 */
    private void calculateWidths() {
        if (this.totalWidth <= 0) {
			return;
		}
        float total = 0;
        final int numCols = getNumberOfColumns();
        for (int k = 0; k < numCols; ++k) {
			total += this.relativeWidths[k];
		}
        for (int k = 0; k < numCols; ++k) {
			this.absoluteWidths[k] = this.totalWidth * this.relativeWidths[k] / total;
		}
    }

    /**
     * Sets the full width of the table.
     *
     * @param totalWidth the full width of the table.
     */
    public void setTotalWidth(final float totalWidth) {
        if (this.totalWidth == totalWidth) {
			return;
		}
        this.totalWidth = totalWidth;
        this.totalHeight = 0;
        calculateWidths();
        calculateHeights(true);
    }

    /**
     * Sets the full width of the table from the absolute column width.
     *
     * @param columnWidth the absolute width of each column
     * @throws DocumentException if the number of widths is different than the number
     * of columns
     */
    public void setTotalWidth(final float columnWidth[]) throws DocumentException {
        if (columnWidth.length != getNumberOfColumns()) {
			throw new DocumentException("Wrong number of columns.");
		}
        this.totalWidth = 0;
        for (final float element : columnWidth) {
			this.totalWidth += element;
		}
        setWidths(columnWidth);
    }



    /**
     * Gets the full width of the table.
     *
     * @return the full width of the table
     */
    public float getTotalWidth() {
        return this.totalWidth;
    }

    /**
     * Calculates the heights of the table.
     *
     * @param	firsttime	if true, the heights of the rows will be recalculated.
     * This takes time; normally the heights of the rows are already calcultated,
     * so in most cases, it's save to use false as parameter.
     * @return	the total height of the table. Note that it will be 0 if you didn't
     * specify the width of the table with setTotalWidth().
     * @since	2.1.5	added a parameter and a return type to an existing method,
     * and made it public
     */
    private float calculateHeights(final boolean firsttime) {
        if (this.totalWidth <= 0) {
			return 0;
		}
        this.totalHeight = 0;
        for (int k = 0; k < this.rows.size(); ++k) {
        	this.totalHeight += getRowHeight(k, firsttime);
        }
        return this.totalHeight;
    }



    /**
     * Gets the default <CODE>PdfPCell</CODE> that will be used as
     * reference for all the <CODE>addCell</CODE> methods except
     * <CODE>addCell(PdfPCell)</CODE>.
     *
     * @return default <CODE>PdfPCell</CODE>
     */
    public PdfPCell getDefaultCell() {
        return this.defaultCell;
    }

    /**
     * Adds a cell element.
     *
     * @param cell the cell element
     */
    public void addCell(final PdfPCell cell) {
    	this.rowCompleted = false;
        final PdfPCell ncell = new PdfPCell(cell);

        int colspan = ncell.getColspan();
        colspan = Math.max(colspan, 1);
        colspan = Math.min(colspan, this.currentRow.length - this.currentRowIdx);
        ncell.setColspan(colspan);

        if (colspan != 1) {
			this.isColspan = true;
		}
        final int rdir = ncell.getRunDirection();
        if (rdir == PdfWriter.RUN_DIRECTION_DEFAULT) {
			ncell.setRunDirection(this.runDirection);
		}

        skipColsWithRowspanAbove();

        boolean cellAdded = false;
        if (this.currentRowIdx < this.currentRow.length) {
	        this.currentRow[this.currentRowIdx] = ncell;
	        this.currentRowIdx += colspan;
	        cellAdded = true;
        }

        skipColsWithRowspanAbove();

        if (this.currentRowIdx >= this.currentRow.length) {
        	final int numCols = getNumberOfColumns();
            if (this.runDirection == PdfWriter.RUN_DIRECTION_RTL) {
                final PdfPCell rtlRow[] = new PdfPCell[numCols];
                int rev = this.currentRow.length;
                for (int k = 0; k < this.currentRow.length; ++k) {
                    final PdfPCell rcell = this.currentRow[k];
                    final int cspan = rcell.getColspan();
                    rev -= cspan;
                    rtlRow[rev] = rcell;
                    k += cspan - 1;
                }
                this.currentRow = rtlRow;
            }
            final PdfPRow row = new PdfPRow(this.currentRow);
            if (this.totalWidth > 0) {
                row.setWidths(this.absoluteWidths);
                this.totalHeight += row.getMaxHeights();
            }
            this.rows.add(row);
            this.currentRow = new PdfPCell[numCols];
            this.currentRowIdx = 0;
            this.rowCompleted = true;
        }

        if (!cellAdded) {
            this.currentRow[this.currentRowIdx] = ncell;
            this.currentRowIdx += colspan;
        }
    }

    /**
     * When updating the row index, cells with rowspan should be taken into account.
     * This is what happens in this method.
     * @since	2.1.6
     */
    private void skipColsWithRowspanAbove() {
    	int direction = 1;
    	if (this.runDirection == PdfWriter.RUN_DIRECTION_RTL) {
			direction = -1;
		}
    	while (rowSpanAbove(this.rows.size(), this.currentRowIdx)) {
			this.currentRowIdx += direction;
		}
    }

    /**
     * Checks if there are rows above belonging to a rowspan.
     * @param	currRow	the current row to check
     * @param	currCol	the current column to check
     * @return	true if there's a cell above that belongs to a rowspan
     * @since	2.1.6
     */
    boolean rowSpanAbove(final int currRow, final int currCol) {

    	if (currCol >= getNumberOfColumns()
    			|| currCol < 0
    			|| currRow == 0) {
			return false;
		}

    	int row = currRow - 1;
    	PdfPRow aboveRow = (PdfPRow)this.rows.get(row);
    	if (aboveRow == null) {
			return false;
		}
    	PdfPCell aboveCell = aboveRow.getCells()[currCol];
    	while (aboveCell == null && row > 0) {
    		aboveRow  = (PdfPRow)this.rows.get(--row);
    		if (aboveRow == null) {
				return false;
			}
    		aboveCell = aboveRow.getCells()[currCol];
    	}

    	int distance = currRow - row;

    	if (aboveCell == null) {
        	int col = currCol - 1;
        	aboveCell = aboveRow.getCells()[col];
        	while (aboveCell == null && row > 0) {
				aboveCell = aboveRow.getCells()[--col];
			}
        	return aboveCell != null && aboveCell.getRowspan() > distance;
    	}

    	if (aboveCell.getRowspan() == 1 && distance > 1) {
        	int col = currCol - 1;
        	aboveRow = (PdfPRow)this.rows.get(row + 1);
        	distance--;
        	aboveCell = aboveRow.getCells()[col];
        	while (aboveCell == null && col > 0) {
				aboveCell = aboveRow.getCells()[--col];
			}
    	}

    	return aboveCell != null && aboveCell.getRowspan() > distance;
    }








    /**
     * Adds a cell element.
     *
     * @param phrase the <CODE>Phrase</CODE> to be added to the cell
     */
    private void addCell(final Phrase phrase) {
        this.defaultCell.setPhrase(phrase);
        addCell(this.defaultCell);
        this.defaultCell.setPhrase(null);
    }

    /**
     * Writes the selected rows to the document.
     * <CODE>canvases</CODE> is obtained from <CODE>beginWritingRows()</CODE>.
     *
     * @param rowStart the first row to be written, zero index
     * @param rowEnd the last row to be written + 1. If it is -1 all the
     * rows to the end are written
     * @param xPos the x write coordinate
     * @param yPos the y write coordinate
     * @param canvases an array of 4 <CODE>PdfContentByte</CODE> obtained from
     * <CODE>beginWrittingRows()</CODE>
     * @return the y coordinate position of the bottom of the last row
     * @see #beginWritingRows(com.aowagie.text.pdf.PdfContentByte)
     */
    float writeSelectedRows(final int rowStart, final int rowEnd, final float xPos, final float yPos, final PdfContentByte[] canvases) {
        return writeSelectedRows(0, -1, rowStart, rowEnd, xPos, yPos, canvases);
    }

    /**
     * Writes the selected rows and columns to the document.
     * This method does not clip the columns; this is only important
     * if there are columns with colspan at boundaries.
     * <CODE>canvases</CODE> is obtained from <CODE>beginWritingRows()</CODE>.
     * The table event is only fired for complete rows.
     *
     * @param colStart the first column to be written, zero index
     * @param colEnd the last column to be written + 1. If it is -1 all the
     * columns to the end are written
     * @param rowStart the first row to be written, zero index
     * @param rowEnd the last row to be written + 1. If it is -1 all the
     * rows to the end are written
     * @param xPos the x write coordinate
     * @param yPos the y write coordinate
     * @param canvases an array of 4 <CODE>PdfContentByte</CODE> obtained from
     * <CODE>beginWritingRows()</CODE>
     * @return the y coordinate position of the bottom of the last row
     * @see #beginWritingRows(com.aowagie.text.pdf.PdfContentByte)
     */
    private float writeSelectedRows(int colStart, int colEnd, int rowStart, int rowEnd, final float xPos, float yPos, final PdfContentByte[] canvases) {
        if (this.totalWidth <= 0) {
			throw new RuntimeException("The table width must be greater than zero.");
		}

        final int totalRows = this.rows.size();
        if (rowStart < 0) {
			rowStart = 0;
		}
        if (rowEnd < 0) {
			rowEnd = totalRows;
		} else {
			rowEnd = Math.min(rowEnd, totalRows);
		}
        if (rowStart >= rowEnd) {
			return yPos;
		}

        final int totalCols = getNumberOfColumns();
        if (colStart < 0) {
			colStart = 0;
		} else {
			colStart = Math.min(colStart, totalCols);
		}
        if (colEnd < 0) {
			colEnd = totalCols;
		} else {
			colEnd = Math.min(colEnd, totalCols);
		}

        final float yPosStart = yPos;
        for (int k = rowStart; k < rowEnd; ++k) {
            final PdfPRow row = (PdfPRow)this.rows.get(k);
            if (row != null) {
                row.writeCells(colStart, colEnd, xPos, yPos, canvases);
                yPos -= row.getMaxHeights();
            }
        }

        if (this.tableEvent != null && colStart == 0 && colEnd == totalCols) {
            final float heights[] = new float[rowEnd - rowStart + 1];
            heights[0] = yPosStart;
            for (int k = rowStart; k < rowEnd; ++k) {
                final PdfPRow row = (PdfPRow)this.rows.get(k);
                float hr = 0;
                if (row != null) {
					hr = row.getMaxHeights();
				}
                heights[k - rowStart + 1] = heights[k - rowStart] - hr;
            }
            this.tableEvent.tableLayout(this, getEventWidths(xPos, rowStart, rowEnd, this.headersInEvent), heights, this.headersInEvent ? this.headerRows : 0, rowStart, canvases);
        }

        return yPos;
    }

    /**
     * Writes the selected rows to the document.
     *
     * @param rowStart the first row to be written, zero index
     * @param rowEnd the last row to be written + 1. If it is -1 all the
     * rows to the end are written
     * @param xPos the x write coordinate
     * @param yPos the y write coordinate
     * @param canvas the <CODE>PdfContentByte</CODE> where the rows will
     * be written to
     * @return the y coordinate position of the bottom of the last row
     */
    float writeSelectedRows(final int rowStart, final int rowEnd, final float xPos, final float yPos, final PdfContentByte canvas) {
        return writeSelectedRows(0, -1, rowStart, rowEnd, xPos, yPos, canvas);
    }

    /**
     * Writes the selected rows and columns to the document.
     * This method clips the columns; this is only important
     * if there are columns with colspan at boundaries.
     * The table event is only fired for complete rows.
     *
     * @param colStart the first column to be written, zero index
     * @param colEnd the last column to be written + 1. If it is -1 all the
     * columns to the end are written
     * @param rowStart the first row to be written, zero index
     * @param rowEnd the last row to be written + 1. If it is -1 all the
     * rows to the end are written
     * @param xPos the x write coordinate
     * @param yPos the y write coordinate
     * @param canvas the <CODE>PdfContentByte</CODE> where the rows will
     * be written to
     * @return the y coordinate position of the bottom of the last row
     */
    private float writeSelectedRows(int colStart, int colEnd, final int rowStart, final int rowEnd, final float xPos, final float yPos, final PdfContentByte canvas) {
        final int totalCols = getNumberOfColumns();
        if (colStart < 0) {
			colStart = 0;
		} else {
			colStart = Math.min(colStart, totalCols);
		}

    	if (colEnd < 0) {
			colEnd = totalCols;
		} else {
			colEnd = Math.min(colEnd, totalCols);
		}

    	final boolean clip = colStart != 0 || colEnd != totalCols;

        if (clip) {
            float w = 0;
            for (int k = colStart; k < colEnd; ++k) {
				w += this.absoluteWidths[k];
			}
            canvas.saveState();
            final float lx = colStart == 0 ? 10000 : 0;
            final float rx = colEnd == totalCols ? 10000 : 0;
            canvas.rectangle(xPos - lx, -10000, w + lx + rx, PdfPRow.RIGHT_LIMIT);
            canvas.clip();
            canvas.newPath();
        }

        final PdfContentByte[] canvases = beginWritingRows(canvas);
        final float y = writeSelectedRows(colStart, colEnd, rowStart, rowEnd, xPos, yPos, canvases);
        endWritingRows(canvases);

        if (clip) {
			canvas.restoreState();
		}

        return y;
    }

    /**
     * Gets and initializes the 4 layers where the table is written to. The text or graphics are added to
     * one of the 4 <CODE>PdfContentByte</CODE> returned with the following order:
     * <ul>
     * <li><CODE>PdfPtable.BASECANVAS</CODE> - the original <CODE>PdfContentByte</CODE>. Anything placed here
     * will be under the table.
     * <li><CODE>PdfPtable.BACKGROUNDCANVAS</CODE> - the layer where the background goes to.
     * <li><CODE>PdfPtable.LINECANVAS</CODE> - the layer where the lines go to.
     * <li><CODE>PdfPtable.TEXTCANVAS</CODE> - the layer where the text go to. Anything placed here
     * will be over the table.
     * </ul>
     * The layers are placed in sequence on top of each other.
     *
     * @param canvas the <CODE>PdfContentByte</CODE> where the rows will
     * be written to
     * @return an array of 4 <CODE>PdfContentByte</CODE>
     * @see #writeSelectedRows(int, int, float, float, PdfContentByte[])
     */
    private static PdfContentByte[] beginWritingRows(final PdfContentByte canvas) {
        return new PdfContentByte[]{
            canvas,
            canvas.getDuplicate(),
            canvas.getDuplicate(),
            canvas.getDuplicate(),
        };
    }

    /**
     * Finishes writing the table.
     *
     * @param canvases the array returned by <CODE>beginWritingRows()</CODE>
     */
    private static void endWritingRows(final PdfContentByte[] canvases) {
        final PdfContentByte canvas = canvases[BASECANVAS];
        canvas.saveState();
        canvas.add(canvases[BACKGROUNDCANVAS]);
        canvas.restoreState();
        canvas.saveState();
        canvas.setLineCap(2);
        canvas.resetRGBColorStroke();
        canvas.add(canvases[LINECANVAS]);
        canvas.restoreState();
        canvas.add(canvases[TEXTCANVAS]);
    }

    /**
     * Gets the number of rows in this table.
     *
     * @return the number of rows in this table
     */
    int size() {
        return this.rows.size();
    }

    /**
     * Gets the total height of the table.
     *
     * @return the total height of the table
     */
    public float getTotalHeight() {
        return this.totalHeight;
    }

    /**
     * Gets the height of a particular row.
     *
     * @param idx the row index (starts at 0)
     * @return the height of a particular row
     */
    float getRowHeight(final int idx) {
    	return getRowHeight(idx, false);
    }
    /**
     * Gets the height of a particular row.
     *
     * @param idx the row index (starts at 0)
     * @param firsttime	is this the first time the row heigh is calculated?
     * @return the height of a particular row
     * @since	3.0.0
     */
    private float getRowHeight(final int idx, final boolean firsttime) {
        if (this.totalWidth <= 0 || idx < 0 || idx >= this.rows.size()) {
			return 0;
		}
        final PdfPRow row = (PdfPRow)this.rows.get(idx);
        if (row == null) {
			return 0;
		}
        if (firsttime) {
			row.setWidths(this.absoluteWidths);
		}
        float height = row.getMaxHeights();
        PdfPCell cell;
        PdfPRow tmprow;
        for (int i = 0; i < this.relativeWidths.length; i++) {
        	if(!rowSpanAbove(idx, i)) {
				continue;
			}
        	int rs = 1;
        	while (rowSpanAbove(idx - rs, i)) {
        		rs++;
        	}
        	tmprow = (PdfPRow)this.rows.get(idx - rs);
        	cell = tmprow.getCells()[i];
        	float tmp = 0;
        	if (cell.getRowspan() == rs + 1) {
        		tmp = cell.getMaxHeight();
        		while (rs > 0) {
        			tmp -= getRowHeight(idx - rs);
        			rs--;
        		}
        	}
        	if (tmp > height) {
				height = tmp;
			}
        }
        row.setMaxHeights(height);
        return height;
    }

    /**
     * Gets the maximum height of a cell in a particular row (will only be different
     * from getRowHeight is one of the cells in the row has a rowspan &lt; 1).
     *
     * @param	rowIndex	the row index
     * @param	cellIndex	the cell index
     * @return the height of a particular row including rowspan
     * @since	2.1.6
     */
    private float getRowspanHeight(final int rowIndex, final int cellIndex) {
        if (this.totalWidth <= 0 || rowIndex < 0 || rowIndex >= this.rows.size()) {
			return 0;
		}
        final PdfPRow row = (PdfPRow)this.rows.get(rowIndex);
        if (row == null || cellIndex >= row.getCells().length) {
			return 0;
		}
        final PdfPCell cell = row.getCells()[cellIndex];
        if (cell == null) {
			return 0;
		}
        float rowspanHeight = 0;
        for (int j = 0; j < cell.getRowspan(); j++) {
        	rowspanHeight += getRowHeight(rowIndex + j);
        }
        return rowspanHeight;
    }

    /**
     * Gets the height of the rows that constitute the header as defined by
     * <CODE>setHeaderRows()</CODE>.
     *
     * @return the height of the rows that constitute the header and footer
     */
    public float getHeaderHeight() {
        float total = 0;
        final int size = Math.min(this.rows.size(), this.headerRows);
        for (int k = 0; k < size; ++k) {
            final PdfPRow row = (PdfPRow)this.rows.get(k);
            if (row != null) {
				total += row.getMaxHeights();
			}
        }
        return total;
    }

    /**
     * Gets the height of the rows that constitute the footer as defined by
     * <CODE>setFooterRows()</CODE>.
     *
     * @return the height of the rows that constitute the footer
     * @since 2.1.1
     */
    public float getFooterHeight() {
        float total = 0;
        final int start = Math.max(0, this.headerRows - this.footerRows);
        final int size = Math.min(this.rows.size(), this.headerRows);
        for (int k = start; k < size; ++k) {
            final PdfPRow row = (PdfPRow)this.rows.get(k);
            if (row != null) {
				total += row.getMaxHeights();
			}
        }
        return total;
    }

    /**
     * Deletes a row from the table.
     *
     * @param rowNumber the row to be deleted
     * @return <CODE>true</CODE> if the row was deleted
     */
    private boolean deleteRow(final int rowNumber) {
        if (rowNumber < 0 || rowNumber >= this.rows.size()) {
			return false;
		}
        if (this.totalWidth > 0) {
            final PdfPRow row = (PdfPRow)this.rows.get(rowNumber);
            if (row != null) {
				this.totalHeight -= row.getMaxHeights();
			}
        }
        this.rows.remove(rowNumber);
        if (rowNumber < this.headerRows) {
        	--this.headerRows;
        	if (rowNumber >= this.headerRows - this.footerRows) {
				--this.footerRows;
			}
        }
        return true;
    }



    /**
     * Removes all of the rows except headers
     */
    private void deleteBodyRows() {
        final ArrayList rows2 = new ArrayList();
        for (int k = 0; k < this.headerRows; ++k) {
			rows2.add(this.rows.get(k));
		}
        this.rows = rows2;
        this.totalHeight = 0;
        if (this.totalWidth > 0) {
			this.totalHeight = getHeaderHeight();
		}
    }

    /**
     * Returns the number of columns.
     *
     * @return	the number of columns.
     * @since	2.1.1
     */
    public int getNumberOfColumns() {
    	return this.relativeWidths.length;
    }

    /**
     * Gets the number of the rows that constitute the header.
     *
     * @return the number of the rows that constitute the header
     */
    public int getHeaderRows() {
        return this.headerRows;
    }

    /**
     * Sets the number of the top rows that constitute the header.
     * This header has only meaning if the table is added to <CODE>Document</CODE>
     * and the table crosses pages.
     *
     * @param headerRows the number of the top rows that constitute the header
     */
    public void setHeaderRows(int headerRows) {
        if (headerRows < 0) {
			headerRows = 0;
		}
        this.headerRows = headerRows;
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return	an <CODE>ArrayList</CODE>
     */
    @Override
	public ArrayList getChunks() {
        return new ArrayList();
    }

    /**
     * Gets the type of the text element.
     *
     * @return	a type
     */
    @Override
	public int type() {
        return Element.PTABLE;
    }

	/**
	 * @see com.aowagie.text.Element#isContent()
	 * @since	iText 2.0.8
	 */
	@Override
	public boolean isContent() {
		return true;
	}

	/**
	 * @see com.aowagie.text.Element#isNestable()
	 * @since	iText 2.0.8
	 */
	@Override
	public boolean isNestable() {
		return true;
	}



    /**
     * Gets the width percentage that the table will occupy in the page.
     *
     * @return the width percentage that the table will occupy in the page
     */
    public float getWidthPercentage() {
        return this.widthPercentage;
    }

    /**
     * Sets the width percentage that the table will occupy in the page.
     *
     * @param widthPercentage the width percentage that the table will occupy in the page
     */
    public void setWidthPercentage(final float widthPercentage) {
        this.widthPercentage = widthPercentage;
    }

    /**
     * Gets the horizontal alignment of the table relative to the page.
     *
     * @return the horizontal alignment of the table relative to the page
     */
    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the table relative to the page.
     * It only has meaning if the width percentage is less than 100%.
     *
     * @param horizontalAlignment the horizontal alignment of the table
     * relative to the page
     */
    public void setHorizontalAlignment(final int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Gets a row with a given index
     * (added by Jin-Hsia Yang).
     *
     * @param idx Index
     * @return the row at position idx
     */
    PdfPRow getRow(final int idx) {
        return (PdfPRow)this.rows.get(idx);
    }

    /**
     * Gets an arraylist with all the rows in the table.
     *
     * @return an arraylist
     */
    public ArrayList getRows() {
        return this.rows;
    }

    /**
     * Gets an arraylist with a selection of rows.
     * @param	start	the first row in the selection
     * @param	end 	the first row that isn't part of the selection
     * @return	a selection of rows
     * @since	2.1.6
     */
    ArrayList getRows(final int start, final int end) {
    	final ArrayList list = new ArrayList();
    	if (start < 0 || end > size()) {
    		return list;
    	}
    	final PdfPRow firstRow = adjustCellsInRow(start, end);
    	int colIndex = 0;
    	PdfPCell cell;
    	while (colIndex < getNumberOfColumns()) {
    		int rowIndex = start;
    		while (rowSpanAbove(rowIndex--, colIndex)) {
    			final PdfPRow row = getRow(rowIndex);
    			if (row != null) {
    				final PdfPCell replaceCell = row.getCells()[colIndex];
    				if (replaceCell != null) {
        				firstRow.getCells()[colIndex] = new PdfPCell(replaceCell);
    					float extra = 0;
    					final int stop = Math.min(rowIndex + replaceCell.getRowspan(), end);
    					for (int j = start + 1; j < stop; j++) {
    						extra += getRowHeight(j);
    					}
    					firstRow.setExtraHeight(colIndex, extra);
    					final float diff = getRowspanHeight(rowIndex, colIndex)
    						- getRowHeight(start) - extra;
    					firstRow.getCells()[colIndex].consumeHeight(diff);
    				}
    			}
    		}
    		cell = firstRow.getCells()[colIndex];
    		if (cell == null) {
				colIndex++;
			} else {
				colIndex += cell.getColspan();
			}
    	}
    	list.add(firstRow);
    	for (int i = start + 1; i < end; i++) {
    		list.add(adjustCellsInRow(i, end));
    	}
    	return list;
    }

    /**
     * Calculates the extra height needed in a row because of rowspans.
     * @param	start	the index of the start row (the one to adjust)
     * @param	end		the index of the end row on the page
     * @return Row
     * @since	2.1.6
     */
    private PdfPRow adjustCellsInRow(final int start, final int end) {
    	final PdfPRow row = new PdfPRow(getRow(start));
		row.initExtraHeights();
		PdfPCell cell;
		final PdfPCell[] cells = row.getCells();
		for (int i = 0; i < cells.length; i++) {
			cell = cells[i];
			if (cell == null || cell.getRowspan() == 1) {
				continue;
			}
			final int stop = Math.min(end, start + cell.getRowspan());
			float extra = 0;
			for (int k = start + 1; k < stop; k++) {
				extra += getRowHeight(k);
			}
			row.setExtraHeight(i, extra);
		}
    	return row;
    }

    /** Sets the table event for this table.
     * @param event the table event for this table
     */
    public void setTableEvent(final PdfPTableEvent event) {
    	if (event == null) {
			this.tableEvent = null;
		} else if (this.tableEvent == null) {
			this.tableEvent = event;
		} else if (this.tableEvent instanceof PdfPTableEventForwarder) {
			((PdfPTableEventForwarder)this.tableEvent).addTableEvent(event);
		} else {
    		final PdfPTableEventForwarder forward = new PdfPTableEventForwarder();
    		forward.addTableEvent(this.tableEvent);
    		forward.addTableEvent(event);
    		this.tableEvent = forward;
    	}
    }

    /**
     * Gets the table event for this page.
     *
     * @return the table event for this page
     */
    public PdfPTableEvent getTableEvent() {
        return this.tableEvent;
    }

    /**
     * Gets the absolute sizes of each column width.
     *
     * @return he absolute sizes of each column width
     */
    public float[] getAbsoluteWidths() {
        return this.absoluteWidths;
    }

    private float [][] getEventWidths(final float xPos, int firstRow, int lastRow, final boolean includeHeaders) {
        if (includeHeaders) {
            firstRow = Math.max(firstRow, this.headerRows);
            lastRow = Math.max(lastRow, this.headerRows);
        }
        final float widths[][] = new float[(includeHeaders ? this.headerRows : 0) + lastRow - firstRow][];
        if (this.isColspan) {
            int n = 0;
            if (includeHeaders) {
                for (int k = 0; k < this.headerRows; ++k) {
                    final PdfPRow row = (PdfPRow)this.rows.get(k);
                    if (row == null) {
						++n;
					} else {
						widths[n++] = row.getEventWidth(xPos);
					}
                }
            }
            for (; firstRow < lastRow; ++firstRow) {
                    final PdfPRow row = (PdfPRow)this.rows.get(firstRow);
                    if (row == null) {
						++n;
					} else {
						widths[n++] = row.getEventWidth(xPos);
					}
            }
        }
        else {
        	final int numCols = getNumberOfColumns();
            final float width[] = new float[numCols + 1];
            width[0] = xPos;
            for (int k = 0; k < numCols; ++k) {
				width[k + 1] = width[k] + this.absoluteWidths[k];
			}
            for (int k = 0; k < widths.length; ++k) {
				widths[k] = width;
			}
        }
        return widths;
    }


    /**
     * Tells you if the first header needs to be skipped
     * (for instance if the header says "continued from the previous page").
     *
     * @return Value of property skipFirstHeader.
     */
    public boolean isSkipFirstHeader() {
        return this.skipFirstHeader;
    }


    /**
     * Tells you if the last footer needs to be skipped
     * (for instance if the footer says "continued on the next page")
     *
     * @return Value of property skipLastFooter.
     * @since	2.1.6
     */
    public boolean isSkipLastFooter() {
        return this.skipLastFooter;
    }

    /**
     * Skips the printing of the first header. Used when printing
     * tables in succession belonging to the same printed table aspect.
     *
     * @param skipFirstHeader New value of property skipFirstHeader.
     */
    public void setSkipFirstHeader(final boolean skipFirstHeader) {
        this.skipFirstHeader = skipFirstHeader;
    }

    /**
     * Skips the printing of the last footer. Used when printing
     * tables in succession belonging to the same printed table aspect.
     *
     * @param skipLastFooter New value of property skipLastFooter.
     * @since	2.1.6
     */
    public void setSkipLastFooter(final boolean skipLastFooter) {
        this.skipLastFooter = skipLastFooter;
    }

    /**
     * Sets the run direction of the contents of the table.
     *
     * @param runDirection One of the following values:
     * PdfWriter.RUN_DIRECTION_DEFAULT, PdfWriter.RUN_DIRECTION_NO_BIDI,
     * PdfWriter.RUN_DIRECTION_LTR or PdfWriter.RUN_DIRECTION_RTL.
     */
    public void setRunDirection(final int runDirection) {
        switch (runDirection) {
        	case PdfWriter.RUN_DIRECTION_DEFAULT:
        	case PdfWriter.RUN_DIRECTION_NO_BIDI:
        	case PdfWriter.RUN_DIRECTION_LTR:
        	case PdfWriter.RUN_DIRECTION_RTL:
        		this.runDirection = runDirection;
        		break;
        	default:
        		throw new RuntimeException("Invalid run direction: " + runDirection);
        }
    }

    /**
     * Returns the run direction of the contents in the table.
     *
     * @return One of the following values:
     * PdfWriter.RUN_DIRECTION_DEFAULT, PdfWriter.RUN_DIRECTION_NO_BIDI,
     * PdfWriter.RUN_DIRECTION_LTR or PdfWriter.RUN_DIRECTION_RTL.
     */
    public int getRunDirection() {
        return this.runDirection;
    }

    /**
     * Getter for property lockedWidth.
     *
     * @return Value of property lockedWidth.
     */
    public boolean isLockedWidth() {
        return this.lockedWidth;
    }

    /**
     * Uses the value in <CODE>setTotalWidth()</CODE> in <CODE>Document.add()</CODE>.
     *
     * @param lockedWidth <CODE>true</CODE> to use the value in <CODE>setTotalWidth()</CODE> in <CODE>Document.add()</CODE>
     */
    public void setLockedWidth(final boolean lockedWidth) {
        this.lockedWidth = lockedWidth;
    }

    /**
     * Gets the split value.
     *
     * @return true to split; false otherwise
     */
    public boolean isSplitRows() {
        return this.splitRows;
    }

    /**
     * When set the rows that won't fit in the page will be split.
     * Note that it takes at least twice the memory to handle a split table row
     * than a normal table. <CODE>true</CODE> by default.
     *
     * @param splitRows true to split; false otherwise
     */
    public void setSplitRows(final boolean splitRows) {
        this.splitRows = splitRows;
    }

    /**
     * Sets the spacing before this table.
     *
     * @param	spacing		the new spacing
     */
    public void setSpacingBefore(final float spacing) {
        this.spacingBefore = spacing;
    }

    /**
     * Sets the spacing after this table.
     *
     * @param	spacing		the new spacing
     */
    public void setSpacingAfter(final float spacing) {
        this.spacingAfter = spacing;
    }

    /**
     * Gets the spacing before this table.
     *
     * @return	the spacing
     */
    float spacingBefore() {
        return this.spacingBefore;
    }

    /**
     * Gets the spacing after this table.
     *
     * @return	the spacing
     */
    float spacingAfter() {
        return this.spacingAfter;
    }

    /**
     * Gets the value of the last row extension.
     *
     * @return true if the last row will extend; false otherwise
     */
    public boolean isExtendLastRow() {
        return this.extendLastRow;
    }

    /**
     * When set the last row will be extended to fill all the remaining space
     * to the bottom boundary.
     *
     * @param extendLastRow true to extend the last row; false otherwise
     */
    public void setExtendLastRow(final boolean extendLastRow) {
        this.extendLastRow = extendLastRow;
    }

    /**
     * Gets the header status inclusion in PdfPTableEvent.
     *
     * @return true if the headers are included; false otherwise
     */
    public boolean isHeadersInEvent() {
        return this.headersInEvent;
    }

    /**
     * When set the PdfPTableEvent will include the headers.
     *
     * @param headersInEvent true to include the headers; false otherwise
     */
    public void setHeadersInEvent(final boolean headersInEvent) {
        this.headersInEvent = headersInEvent;
    }

    /**
     * Gets the property splitLate.
     *
     * @return the property splitLate
     */
    public boolean isSplitLate() {
        return this.splitLate;
    }

    /**
     * If true the row will only split if it's the first one in an empty page.
     * It's true by default.
     * It's only meaningful if setSplitRows(true).
     *
     * @param splitLate the property value
     */
    public void setSplitLate(final boolean splitLate) {
        this.splitLate = splitLate;
    }

    /**
     * If true the table will be kept on one page if it fits, by forcing a
     * new page if it doesn't fit on the current page. The default is to
     * split the table over multiple pages.
     *
     * @param keepTogether whether to try to keep the table on one page
     */
    public void setKeepTogether(final boolean keepTogether) {
        this.keepTogether = keepTogether;
    }

    /**
     * Getter for property keepTogether
     *
     * @return true if it is tried to keep the table on one page;
     * false otherwise
     */
    public boolean getKeepTogether() {
        return this.keepTogether;
    }

    /**
     * Gets the number of rows in the footer.
     *
     * @return the number of rows in the footer
     */
    public int getFooterRows() {
        return this.footerRows;
    }

    /**
     * Sets the number of rows to be used for the footer. The number
     * of footer rows are subtracted from the header rows. For
     * example, for a table with two header rows and one footer row the
     * code would be:
     * <pre>
     * table.setHeaderRows(3);
     * table.setFooterRows(1);
     * </pre>
     * Row 0 and 1 will be the header rows and row 2 will be the footer row.
     *
     * @param footerRows the number of rows to be used for the footer
     */
    public void setFooterRows(int footerRows) {
        if (footerRows < 0) {
			footerRows = 0;
		}
        this.footerRows = footerRows;
    }



	/**
	 * @since	iText 2.0.8
	 * @see com.aowagie.text.LargeElement#flushContent()
	 */
	@Override
	public void flushContent() {
		deleteBodyRows();
		setSkipFirstHeader(true);
	}

	/**
     * @since	iText 2.0.8
	 * @see com.aowagie.text.LargeElement#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return this.complete;
	}

	/**
     * @since	iText 2.0.8
	 * @see com.aowagie.text.LargeElement#setComplete(boolean)
	 */
	@Override
	public void setComplete(final boolean complete) {
		this.complete = complete;
	}
}