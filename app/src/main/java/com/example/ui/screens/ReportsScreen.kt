package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodels.SublimationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.db.entity.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.io.FileOutputStream
import java.io.InputStream
import java.io.File
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.BaseDirection
import com.itextpdf.io.font.PdfEncodings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: SublimationViewModel) {
    val totalCustomerDebt by viewModel.totalCustomerDebt.collectAsStateWithLifecycle()
    val todaySalesAmount by viewModel.todaySalesAmount.collectAsStateWithLifecycle()
    val todayOrdersCount by viewModel.todayOrdersCount.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val grossProfit by viewModel.grossProfit.collectAsStateWithLifecycle()
    val netProfit by viewModel.netProfit.collectAsStateWithLifecycle()
    val lowStockFabricsCount by viewModel.lowStockFabricsCount.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(0L) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = java.time.Instant.now().minus(java.time.Duration.ofDays(30)).toEpochMilli(),
        initialSelectedEndDateMillis = System.currentTimeMillis()
    )

    val xlsxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportOrdersToXlsx(context, uri, orders)
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportSummaryToPdf(context, uri, grossProfit, netProfit, totalExpenses, todaySalesAmount ?: 0.0)
            }
        }
    }
    
    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { xlsxLauncher.launch("orders_export.xlsx") }) {
                    Text("Excel", modifier = Modifier.padding(16.dp))
                }
                FloatingActionButton(onClick = { pdfLauncher.launch("reports_summary.pdf") }) {
                    Text("PDF (فارسی)", modifier = Modifier.padding(16.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "گزارشات کامل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, contentDescription = "تغییر تاریخ")
                Spacer(modifier = Modifier.width(8.dp))
                Text("انتخاب بازه تاریخی گزارش")
            }
            
            Text(
                text = "بازه انتخابی: ${getShamsiDate(datePickerState.selectedStartDateMillis ?: 0L)} تا ${getShamsiDate(datePickerState.selectedEndDateMillis ?: System.currentTimeMillis())}",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("آمار فروش امروز:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("تعداد سفارشات امروز: $todayOrdersCount")
                    Text("فروش کل امروز: ${String.format("%,.0f", todaySalesAmount ?: 0.0)} تومان")
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("سود و زیان کلی:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("سود ناخالص: ${String.format("%,.0f", grossProfit)} تومان")
                    Text("کل هزینه‌ها: ${String.format("%,.0f", totalExpenses)} تومان", color = MaterialTheme.colorScheme.error)
                    Text("سود خالص: ${String.format("%,.0f", netProfit)} تومان", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("وضعیت انبار و مشتریان:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("مجموع بدهی مشتریان: ${String.format("%,.0f", totalCustomerDebt ?: 0.0)} تومان")
                    Text("تعداد پارچه‌های کم‌موجود: $lowStockFabricsCount", color = if(lowStockFabricsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    val start = datePickerState.selectedStartDateMillis ?: 0L
                    val end = datePickerState.selectedEndDateMillis ?: System.currentTimeMillis()
                    viewModel.setDateRange(start, end)
                }) {
                    Text("تایید")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("انصراف")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                title = { Text("بازه گزارشات را انتخاب کنید", modifier = Modifier.padding(16.dp)) }
            )
        }
    }
}

private suspend fun exportOrdersToXlsx(context: Context, uri: Uri, orders: List<Order>) {
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("سفارشات")
                sheet.isRightToLeft = true

                // Header Row
                val headerRow = sheet.createRow(0)
                val headers = listOf("شناسه سفارش", "شناسه مشتری", "مبلغ کل", "مبلغ پرداختی", "مانده بدهی", "تاریخ")
                headers.forEachIndexed { colIndex, title ->
                    val cell = headerRow.createCell(colIndex)
                    cell.setCellValue(title)
                }

                // Data Rows
                orders.forEachIndexed { rowIndex, order ->
                    val row = sheet.createRow(rowIndex + 1)
                    row.createCell(0).setCellValue(order.id.toString())
                    row.createCell(1).setCellValue(order.customerId.toString())
                    row.createCell(2).setCellValue(order.totalAmount)
                    row.createCell(3).setCellValue(order.paidAmount)
                    row.createCell(4).setCellValue(order.remainingAmount)
                    
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val jalaliStr = "${getShamsiDate(order.date)} ${timeFormat.format(Date(order.date))}"
                    row.createCell(5).setCellValue(jalaliStr)
                }

                // Auto-size columns
                for (i in headers.indices) {
                    sheet.autoSizeColumn(i)
                }

                workbook.write(outputStream)
                workbook.close()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "فایل اکسل با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا در خروجی اکسل.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun exportSummaryToPdf(context: Context, uri: Uri, gross: Double, net: Double, exp: Double, todaySales: Double) {
    withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "temp_font.ttf")
            if (!file.exists()) {
                var isSet = false
                try {
                   context.assets.open("fonts/vazirmatn.ttf").use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                   }
                   isSet = true
                } catch(e: Exception) {}
                
                // Fallback to system fonts if user didn't drop the font yet, although text might not render nicely
                if (!isSet) {
                   file.createNewFile() // Creates empty file to fail gracefully
                }
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdfDocument = PdfDocument(writer)
                val document = Document(pdfDocument)
                document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                
                var font: PdfFont? = null
                try {
                    font = PdfFontFactory.createFont(file.absolutePath, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED)
                } catch(e: Exception) {
                    font = PdfFontFactory.createFont()
                }

                document.setFont(font)

                val table = Table(1)
                table.useAllAvailableWidth()
                table.setTextAlignment(TextAlignment.RIGHT)
                
                var cell = Cell().add(Paragraph("گزارش خلاصه وضعیت سیستم سابلیمیشن").setBold().setFontSize(18f))
                cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                cell.setTextAlignment(TextAlignment.CENTER)
                table.addCell(cell)
                
                cell = Cell().add(Paragraph("فروش امروز: ${String.format("%,.0f", todaySales)} تومان").setFontSize(14f))
                cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                table.addCell(cell)
                
                cell = Cell().add(Paragraph("کل سود ناخالص: ${String.format("%,.0f", gross)} تومان").setFontSize(14f))
                cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                table.addCell(cell)
                
                cell = Cell().add(Paragraph("کل هزینه‌ها: ${String.format("%,.0f", exp)} تومان").setFontSize(14f))
                cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                table.addCell(cell)
                
                cell = Cell().add(Paragraph("سود خالص: ${String.format("%,.0f", net)} تومان").setFontSize(14f))
                cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                table.addCell(cell)

                document.add(table)
                document.close()
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "فایل PDF با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                 Toast.makeText(context, "خطا در خروجی PDF. آیا فونت در فولدر assets قرار دارد؟", Toast.LENGTH_LONG).show()
            }
        }
    }
}

