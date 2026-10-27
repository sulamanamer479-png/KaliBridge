package com.example.kalibridge

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.view.ViewGroup
import android.graphics.Typeface

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val pickFile = 501

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)

        val title = TextView(this).apply {
            text = "Kali Bridge"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        }
        content.addView(title)
        content.addView(TextView(this).apply {
            text = "واجهة مساعدة لتشغيل بيئة Kali Linux على أندرويد وفتح ملفات iOS للاختيار والمعاينة."
            textSize = 16f
            setPadding(0, pad / 2, 0, pad)
        })

        content.addView(makeButton("فتح Termux / Kali") { openTermux() })
        content.addView(makeButton("اختيار ملف iOS (.ipa) أو ملف آخر") { chooseFile() })
        content.addView(makeButton("طريقة الإعداد") {
            status.text = "١) ثبّت Termux من مصدره الرسمي.\\n" +
                "٢) داخل Termux، اتبع توثيق Kali NetHunter الرسمي لإعداد بيئة Kali المناسبة لجهازك.\\n" +
                "٣) ارجع واضغط «فتح Termux / Kali».\\n\\n" +
                "ملاحظة: التطبيق لا يثبّت Kali بنفسه ولا يشغّل ملفات iOS."
        })

        status = TextView(this).apply {
            text = "الحالة: جاهز.\\nيمكن اختيار ملفات .ipa وحفظ مرجعها داخل هذه الجلسة."
            textSize = 15f
            setPadding(0, pad, 0, pad)
        }
        content.addView(status)

        content.addView(TextView(this).apply {
            text = "تنبيه مهم: ملفات iOS بصيغة IPA لا تعمل كتطبيقات على أندرويد. يمكن للتطبيق اختيارها أو عرض اسمها فقط، وليس تشغيلها."
            textSize = 14f
            setPadding(0, pad, 0, pad)
        })

        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            setOnClickListener { action() }
            isAllCaps = false
        }

    private fun openTermux() {
        val intent = packageManager.getLaunchIntentForPackage("com.termux")
        if (intent != null) {
            startActivity(intent)
        } else {
            status.text = "Termux غير مثبت. ثبّته من المصدر الرسمي ثم أعد المحاولة."
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://termux.dev/")))
            } catch (_: Exception) {}
        }
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, pickFile)
    }

    @Deprecated("Deprecated in Android but retained for broad compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickFile && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val name = queryDisplayName(uri) ?: uri.lastPathSegment ?: "ملف غير معروف"
            status.text = "تم اختيار الملف: $name\\nالموقع: $uri\\n\\n" +
                "تم اختيار الملف فقط؛ لم يتم تشغيله أو تثبيته."
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }
}
