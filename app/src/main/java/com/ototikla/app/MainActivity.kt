package com.ototikla.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("ayar", MODE_PRIVATE)
        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        fun text(s: String, size: Float = 16f, bold: Boolean = false) = TextView(this).apply {
            text = s; textSize = size; setTextColor(Color.BLACK)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, pad / 2, 0, pad / 2)
        }

        root.addView(text("Oto Tıkla", 28f, true))
        root.addView(text(
            "Bu uygulama Erişilebilirlik Servisi iznini YALNIZCA ekranda sizin seçtiğiniz noktaya " +
            "otomatik dokunma yapmak için kullanır. Ekrandaki yazıları, şifreleri veya kişisel " +
            "verilerinizi okumaz, toplamaz ve hiçbir yere göndermez. İnternet izni yoktur."
        ))
        root.addView(text("1) Aşağıdaki butonla Erişilebilirlik ayarlarını açın, \"Oto Tıkla\" servisini açın.", 15f))
        root.addView(Button(this).apply {
            text = "Erişilebilirlik Ayarlarını Aç"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(text("2) Tıklama aralığı (milisaniye):", 15f))
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getLong("aralik", 50L).toString())
        }
        root.addView(input)
        root.addView(Button(this).apply {
            text = "Kaydet"
            setOnClickListener {
                val v = (input.text.toString().toLongOrNull() ?: 50L).coerceAtLeast(10L)
                prefs.edit().putLong("aralik", v).apply()
                Toast.makeText(this@MainActivity, "Kaydedildi: $v ms", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(text(
            "3) Servis açılınca ekranda kırmızı bir hedef ve küçük bir panel çıkar. Hedefi " +
            "tıklanacak yere sürükleyin, panelden BAŞLAT'a basın. Durdurmak için aynı butona basın.", 15f))
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
