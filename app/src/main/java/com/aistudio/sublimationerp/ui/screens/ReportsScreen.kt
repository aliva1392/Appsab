package com.aistudio.sublimationerp.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, viewModel: SublimationViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val grossProfit by viewModel.grossProfit.collectAsStateWithLifecycle()
    val netProfit by viewModel.netProfit.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle() // or maybe filtered orders, but using all is fine for now
    
    // Add simple state for date range testing (e.g. 1 month ago to now)
    var startDate by remember { mutableStateOf(System.currentTimeMillis() - 30L * 24 * 3600 * 1000) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var startInput by remember { mutableStateOf(getShamsiDate(startDate)) }
    var endInput by remember { mutableStateOf(getShamsiDate(endDate)) }

    LaunchedEffect(startDate, endDate) {
        viewModel.setDateRange(startDate, endDate)
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportSummaryToPdf(context, uri, grossProfit, netProfit, totalExpenses)
            }
        }
    }
    
    val xlsxExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportOrdersToXlsx(context, uri, orders, viewModel)
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("گزارشات و سود و زیان", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        // simple offset buttons since compose DatePicker doesn't support Jalali natively
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { 
                startDate -= 7L*24*3600*1000 
                startInput = getShamsiDate(startDate)
            }) { Text("هفته قبل") }
            Button(onClick = { 
                startDate = System.currentTimeMillis() - 30L*24*3600*1000
                endDate = System.currentTimeMillis() 
                startInput = getShamsiDate(startDate)
                endInput = getShamsiDate(endDate)
            }) { Text("ماه اخیر") }
            Button(onClick = { 
                startDate += 7L*24*3600*1000
                endDate += 7L*24*3600*1000 
                startInput = getShamsiDate(startDate)
                endInput = getShamsiDate(endDate)
            }) { Text("هفته بعد") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = startInput,
                onValueChange = { 
                    startInput = it
                    shamsiToTimestamp(it)?.let { ts -> startDate = ts }
                },
                label = { Text("از تاریخ (شمسی)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endInput,
                onValueChange = { 
                    endInput = it
                    shamsiToTimestamp(it)?.let { ts -> endDate = ts }
                },
                label = { Text("تا تاریخ (شمسی)") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("هزینه‌ها: ${formatCurrency(totalExpenses)}")
                Text("سود ناخالص: ${formatCurrency(grossProfit)}")
                Text(
                    "سود خالص: ${formatCurrency(netProfit)}", 
                    style = MaterialTheme.typography.titleLarge,
                    color = if (netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pdfExportLauncher.launch("summary_report.pdf") }, modifier = Modifier.fillMaxWidth()) { 
            Text("خروجی PDF سود و زیان (ویژه مدیریت)") 
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { xlsxExportLauncher.launch("orders_report.xlsx") }, modifier = Modifier.fillMaxWidth()) { 
            Text("خروجی Excel سفارشات") 
        }
    }
}

suspend fun exportSummaryToPdf(context: Context, uri: Uri, gross: Double, net: Double, exp: Double) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            val fontBytes = context.assets.open("fonts/vazirmatn.ttf").readBytes()
            val font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)
            
            val title = Paragraph("گزارش سود و زیان کارگاه")
                .setFont(font)
                .setFontSize(20f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(title)
            
            document.add(Paragraph("سود ناخالص: ${formatCurrency(gross)}").setFont(font).setTextAlignment(TextAlignment.RIGHT))
            document.add(Paragraph("هزینه‌ها: ${formatCurrency(exp)}").setFont(font).setTextAlignment(TextAlignment.RIGHT))
            document.add(Paragraph("سود خالص: ${formatCurrency(net)}").setFont(font).setTextAlignment(TextAlignment.RIGHT))
            
            document.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun exportOrdersToXlsx(context: Context, uri: Uri, orders: List<Order>, viewModel: SublimationViewModel) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("سفارشات")
            sheet.isRightToLeft = true
            
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("شماره سفارش")
            headerRow.createCell(1).setCellValue("تاریخ")
            headerRow.createCell(2).setCellValue("مشتری (کد)")
            headerRow.createCell(3).setCellValue("مبلغ کل")
            headerRow.createCell(4).setCellValue("نوع سفارش")
            headerRow.createCell(5).setCellValue("وضعیت")
            
            orders.forEachIndexed { index, order ->
                val row = sheet.createRow(index + 1)
                    val customerName = viewModel.customers.value.find { it.id == order.customerId }?.name ?: "نامشخص"
                row.createCell(0).setCellValue(order.id.toString())
                row.createCell(1).setCellValue(getShamsiDate(order.date))
                row.createCell(2).setCellValue(customerName)
                row.createCell(3).setCellValue(order.totalAmount)
                row.createCell(4).setCellValue(order.type.displayName)
                row.createCell(5).setCellValue(order.status.displayName)
            }
            
            workbook.write(outputStream)
            workbook.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
