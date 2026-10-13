package ir.softwaretrain.soghatyar.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun paymentTypeToString(v: PaymentType): String = v.name
    @TypeConverter fun stringToPaymentType(v: String): PaymentType = PaymentType.valueOf(v)

    @TypeConverter fun payMethodToString(v: PayMethod): String = v.name
    @TypeConverter fun stringToPayMethod(v: String): PayMethod = PayMethod.valueOf(v)

    @TypeConverter fun channelToString(v: Channel): String = v.name
    @TypeConverter fun stringToChannel(v: String): Channel = Channel.valueOf(v)
}
