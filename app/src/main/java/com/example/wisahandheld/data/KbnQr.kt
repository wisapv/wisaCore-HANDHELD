package com.example.wisahandheld.data

/**
 * Parses the fixed-width QR string printed on a Kanban (KBN) tag. Every
 * field up through Conveyance lives at a fixed character position (same
 * layout for every shop/dock — confirmed, not just Samrong/S1); Full
 * Address is everything left after that, NOT a fixed 8 characters — same
 * as the backend's own decoder (decodeLocalFreeZoneQr), which this must
 * match exactly since both are decoding the identical QR text. A stricter
 * client-side rule than the backend's used to reject perfectly valid scans
 * whose address happened to run longer than 8 characters. Example:
 *
 *   SS12026090301 335040K270C00001/00020000028DAIWGD3 03/09/202609:4011A610ASD - R03
 *
 *   0      Shop            (1)   "S"
 *   1-2    Dock             (2)   "S1"
 *   3-12   Order number    (10)   "2026090301"
 *   13     (space)
 *   14-25  Part number     (12)   "335040K270C0"
 *   26-29  Box Seq          (4)   "0001"  — which box this tag is (1st, 2nd...)
 *   30     "/"
 *   31-34  Total Boxes      (4)   "0002"  — how many boxes this order/part has in total
 *   35-41  Qty per Box      (7)   "0000028" — standard quantity in one full box
 *   42-45  Supplier code    (4)   "DAIW"
 *   46     S.plant          (1)   "G"
 *   47-48  S.dock           (2)   "D3"
 *   49     (space)
 *   50-59  Arrival date    (10)   "03/09/2026"
 *   60-64  Arrival time     (5)   "09:40"
 *   65-66  MROS Lane No.    (2)   "11"
 *   67-70  KBN code         (4)   "A610"
 *   71     Conveyance       (1)   "A"
 *   72-end Full Address  (rest)   "SD - R03" — whatever's left, length varies
 */
data class ParsedKbn(
    val shop: String,
    val dock: String,
    val orderNumber: String,
    val partNumber: String,
    val boxSeq: Int,
    val totalBoxes: Int,
    val qtyPerBox: Int,
    val supplierCode: String,
    val sPlant: String,
    val sDock: String,
    val arrivalDate: String,
    val arrivalTime: String,
    val mrosLane: String,
    val kbnCode: String,
    val conveyance: String,
    val fullAddress: String
)

object KbnQr {
    // Every fixed-width field up through Conveyance — Full Address is
    // whatever's left, so this is a MINIMUM length, not an exact one.
    private const val FIXED_PREFIX_LENGTH = 72

    /** Returns null for anything shorter than a valid Kanban QR can possibly be (too short to even hold every
     *  fixed field), or with non-numeric qty/box fields. Does NOT require an exact length — Full Address can run
     *  longer or shorter than any particular sample, same as the backend's own decoder. */
    fun parse(raw: String): ParsedKbn? {
        if (raw.length <= FIXED_PREFIX_LENGTH) return null // must have at least 1 char left over for the address
        return try {
            ParsedKbn(
                shop = raw.substring(0, 1),
                dock = raw.substring(1, 3),
                orderNumber = raw.substring(3, 13),
                partNumber = raw.substring(14, 26),
                boxSeq = raw.substring(26, 30).trim().toInt(),
                totalBoxes = raw.substring(31, 35).trim().toInt(),
                qtyPerBox = raw.substring(35, 42).trim().toInt(),
                supplierCode = raw.substring(42, 46),
                sPlant = raw.substring(46, 47),
                sDock = raw.substring(47, 49),
                arrivalDate = raw.substring(50, 60),
                arrivalTime = raw.substring(60, 65),
                mrosLane = raw.substring(65, 67),
                kbnCode = raw.substring(67, 71),
                conveyance = raw.substring(71, 72),
                fullAddress = raw.substring(72)
            )
        } catch (e: Exception) {
            null
        }
    }
}