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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Share
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
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import java.io.FileOutputStream

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

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportOrdersToCsv(context, uri, orders)
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
                FloatingActionButton(onClick = { csvLauncher.launch("orders_export.csv") }) {
                    Text("CSV", modifier = Modifier.padding(16.dp))
                }
                FloatingActionButton(onClick = { pdfLauncher.launch("reports_summary.pdf") }) {
                    Text("PDF (لاتین)", modifier = Modifier.padding(16.dp))
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
}

private suspend fun exportOrdersToCsv(context: Context, uri: Uri, orders: List<Order>) {
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    // Excel UTF-8 BOM
                    writer.write("\uFEFF")
                    writer.write("Order ID,Customer ID,Total Amount,Paid Amount,Remaining Amount,Date\n")
                    val df = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                    for (order in orders) {
                        writer.write("${order.id},${order.customerId},${order.totalAmount},${order.paidAmount},${order.remainingAmount},${df.format(Date(order.date))}\n")
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "فایل اکسل با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun exportSummaryToPdf(context: Context, uri: Uri, gross: Double, net: Double, exp: Double, todaySales: Double) {
    withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 14f
            
            canvas.drawText("Sublimation ERP Summary Report", 10f, 30f, paint)
            paint.textSize = 12f
            canvas.drawText("Today Sales: ${String.format("%,.0f", todaySales)} Tomans", 10f, 70f, paint)
            canvas.drawText("Gross Profit: ${String.format("%,.0f", gross)} Tomans", 10f, 100f, paint)
            canvas.drawText("Total Expenses: ${String.format("%,.0f", exp)} Tomans", 10f, 130f, paint)
            canvas.drawText("Net Profit: ${String.format("%,.0f", net)} Tomans", 10f, 160f, paint)
            
            document.finishPage(page)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "فایل PDF با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}
