package work.pomelo.admin.common.excel;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import work.pomelo.admin.domain.SmsDetails;
import work.pomelo.admin.utils.OtherUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ghostxbh
 * @date 2018/6/14
 * 
 */
public class ExcelUtils {

    private final static String excel2003l = ".xls";
    private final static String excel2007l = ".xlsx";
    private static String[] HEADS = {"短信编号", "短信号码", "短信内容", "状态", "发送时间", "送达时间"};

    public static List<String> fileImport(InputStream ins, String fileName) throws Exception {
        List<String> phoneList = new ArrayList<String>();
        String type = fileName.split("\\.")[1];
        if ("xls".equals(type) || "xlsx".equals(type) ) {
            List<String> excelPhoneList = ExcelUtils.getExcelPhoneList(ins, fileName);
            phoneList.addAll(excelPhoneList);
        } else if ("txt".equals(type)) {
            List<String> txtPhoneList = ExcelUtils.getTxtPhoneList(ins, fileName);
            phoneList.addAll(txtPhoneList);
        }
        return phoneList;
    }

    // 读取文件数据
    public static List<List<Object>> getExcelList(InputStream is, String fileName, Integer... integers)
            throws Exception {
        List<List<Object>> list = new ArrayList<List<Object>>();
        Workbook workbook = null;
        // 文件验证格式
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix.equals(excel2003l)) {
            workbook = new HSSFWorkbook(is);
        } else if (suffix.equals(excel2007l)) {
            workbook = new XSSFWorkbook(is);
        } else {
            // throw
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            if (sheet == null)
                continue;
            // 遍历循环sheet中全部行
            for (int j = sheet.getFirstRowNum() + 1; j <= sheet.getLastRowNum(); j++) {
                row = sheet.getRow(j);
                if (row == null)
                    continue;
                // 循环row中的全部列
                List<Object> li = new ArrayList<Object>();
                for (int k = row.getFirstCellNum(); k <= row.getLastCellNum(); k++) {
                    cell = row.getCell(k);
                    if (cell != null) {
                        li.add(getCellValue(cell));
                    }
                }
                list.add(li);
            }
        }
        return list;

    }

    public static List<String> getTxtPhoneList(InputStream is, String fileName) throws IOException {
        List<String> phoneList = new ArrayList<String>();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        try {
            String line = null;
            while ((line = reader.readLine())!=null) {
                String phone = StringUtils.trimAllWhitespace(line);
                phoneList.add(phone);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            isr.close();
            reader.close();
        }
        return phoneList;
    }

    public static List<String> getExcelPhoneList(InputStream is, String fileName, Integer... integers)
            throws Exception {
        List<String> list = new ArrayList<>();
        Workbook workbook = null;
        // 文件验证格式
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix.equals(excel2003l)) {
            workbook = new HSSFWorkbook(is);
        } else if (suffix.equals(excel2007l)) {
            workbook = new XSSFWorkbook(is);
        } else {
            // throw
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheet = workbook.getSheetAt(i);
            if (sheet == null)
                continue;
            // 遍历循环sheet中全部行
            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                row = sheet.getRow(j);
                if (row == null)
                    continue;
                // 循环row中的全部列
                for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
                    Object stringCellValue = getCellValue(row.getCell(k));
                    stringCellValue = stringCellValue != null ? stringCellValue : "";
                    if (!OtherUtil.checkStrIsNum(stringCellValue.toString())) {
                        continue;
                    }
                    stringCellValue = OtherUtil.splitStr(stringCellValue.toString());
                    list.add(stringCellValue.toString());
                }
            }
        }
        return list;
    }

    // 单元格数据类型格式化
    private static Object getCellValue(Cell cell) {
        Object value = null;
        DecimalFormat decimalFormat = new DecimalFormat("0");
        DecimalFormat decimalFormat2 = new DecimalFormat("0.00");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                    value = decimalFormat.format(cell.getNumericCellValue());
                } else if ("m-d-yy".equals(cell.getCellStyle().getDataFormatString())) {
                    value = dateFormat.format(cell.getDateCellValue());
                } else {
                    value = decimalFormat2.format(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_BLANK:
                value = "";
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
        }
        return value;
    }

    // 导出
    public static XSSFWorkbook downLoadExcelModel(String sheetName, List<SmsDetails> list)
            throws Exception {
        // 创建一个新的Excel文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 填充内容
        if (list != null) {
            // 创建Excel文件的工作表
            XSSFSheet sheet = workbook.createSheet(sheetName);
            createFont(workbook);
            Row headRow = sheet.createRow(0);
            addRowHead(headRow);
            for (int i = 0; i < list.size(); i++) {
                Row row = null;// 创建行
                Cell cell = null;// 创建列
                row = sheet.createRow(i + 1);
                SmsDetails mobileinfo = list.get(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 35 / 10);
                for (int j = 0; j < HEADS.length; j++) {
                    cell = row.createCell(j);
                    switch (j) {
                        case 0:
                            cell.setCellValue(mobileinfo.getId());
                            break;
                        case 1:
                            cell.setCellValue(mobileinfo.getPhone());
                            break;
                        case 2:
                            cell.setCellValue(mobileinfo.getContents());
                            break;
                        case 3:
                            cell.setCellValue(getStatus(mobileinfo.getStatus()));
                            break;
                        case 4:
                            Date createTime = mobileinfo.getCreateTime();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            cell.setCellValue(sdf.format(createTime));
                            break;
                        case 5:
                            Date sendTime = mobileinfo.getSendTime();
                            if (sendTime != null) {
                                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                cell.setCellValue(sdf1.format(sendTime));
                            }
                            break;
                    }
                    cell.setCellStyle(fontStyle2);
                }
            }
        }
        // 设置Excel的字体样式
        createFont(workbook);
        return workbook;
    }

    private static void addRowHead(Row row) {
        for (int i = 0; i < HEADS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADS[i]);
        }
    }

    private static String getStatus(int status){
        switch (status) {
            case 1:
                return "未发送";
            case 2:
                return "已发送";
            case 3:
                return "运营商发送中";
            case 10:
                return "发送成功";
            case -1:
                return "发送失败";
            default:
                return "未知";
        }
    }

    private static XSSFCellStyle fontStyle2;

    public static void createFont(XSSFWorkbook workbook) {
        // 内容
        fontStyle2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontName("宋体");
        font2.setFontHeightInPoints((short) 12);// 设置字体大小
        fontStyle2.setFont(font2);
        fontStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN); // 下边框
        fontStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);// 左边框
        fontStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);// 上边框
        fontStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);// 右边框
        fontStyle2.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 居中
    }
}
