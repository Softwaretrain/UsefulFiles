package ir.softwaretrain.soghatyar.util

import android.content.Context
import android.provider.ContactsContract

/** خواندن مخاطبین گوشی برای افزودن سریع مشتری. نیازمند مجوز READ_CONTACTS. */
object ContactsImporter {

    data class Contact(val name: String, val phone: String)

    fun load(context: Context): List<Contact> {
        val out = ArrayList<Contact>()
        val resolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        try {
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val seen = HashSet<String>()
                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                    val num = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                    val clean = num.replace(" ", "").replace("-", "")
                    if (clean.isNotBlank() && seen.add(clean)) {
                        out.add(Contact(name.ifBlank { clean }, clean))
                    }
                }
            }
        } catch (_: Exception) {
        }
        return out
    }
}
