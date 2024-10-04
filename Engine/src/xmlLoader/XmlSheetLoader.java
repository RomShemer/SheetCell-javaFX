package xmlLoader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logicStructure.sheet.impl.SheetImpl;

import xmlLoader.jaxb.*;

import java.io.File;

public class XmlSheetLoader {

    public static SheetImpl fromXmlFileToObject(String filePath) {

        try {
            XmlSheetValidator.validateXmlPath(filePath);
            File file = new File(filePath);
            XmlSheetValidator.isXmlFileExists(file);
            JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            STLSheet sheet = (STLSheet) jaxbUnmarshaller.unmarshal(file);

            XmlSheetValidator.validateSheetSize(sheet);
            XmlSheetValidator.validateCellsWithinBounds(sheet);
            return createSheetObject(sheet);

        } catch (JAXBException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SheetImpl createSheetObject(STLSheet stlSheet) throws Exception {
        int rowSize = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        int colSize = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        SheetImpl sheet = new SheetImpl(stlSheet.getName(), stlSheet.getSTLLayout().getRows(), stlSheet.getSTLLayout().getColumns(), rowSize, colSize);
        STLCells cells = stlSheet.getSTLCells();

        STLRanges ranges = stlSheet.getSTLRanges();
        for (STLRange range : ranges.getSTLRange()){
            STLBoundaries boundaries = range.getSTLBoundaries();
            String toCoordinate = boundaries.getTo();
            String fromCoordinate = boundaries.getFrom();
            sheet.addRange(range.getName(), fromCoordinate, toCoordinate);
        }

        for (STLCell cell : cells.getSTLCell()) {
            try {
                sheet.addNewCell(XmlSheetValidator.createCellId(cell), cell.getSTLOriginalValue(), sheet.getVersion(), rowSize, colSize);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(String.format("%s (cell:%s)", e.getMessage(), XmlSheetValidator.createCellId(cell)));
            }
        }

        return sheet;
    }
}
