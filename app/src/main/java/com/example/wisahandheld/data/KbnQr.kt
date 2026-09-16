package com.example.wisahandheld.data

/**
 * Parses the fixed-width QR string printed on a Kanban (KBN) tag — mirrors
 * the backend's own decoder (decodeLocalFreeZoneQr in freeZoneQr.js)
 * field-for-field, since both decode the identical QR text and must never
 * disagree about what a given scan means.
 *
 * Every field is genuinely fixed-width EXCEPT Part No.: real scans showed
 * the gap between Order Number and Part No. can be 1 OR 2 blank characters
 * (confirmed against a real printed Kanban label — some orders reserve a
 * short suffix there, blank-padded when unused), which a strict fixed-
 * offset read misreads entirely, shifting every field after it by one
 * character and rejecting an otherwise perfectly valid scan.
 *
 * Fix: don't assume Part No.'s width at all. Anchor on the first "/" in
 * the string instead — Box Seq is always exactly 4 digits immediately
 * before it, so Part No. is simply everything between the order number
 * and those 4 digits, trimmed of any leading/trailing padding, however
 * long it actually is. This assumes Part No. itself never contains a "/"
 * (true of every real sample seen so far) and that the first "/" in the
 * string is the Box Seq separator (always true — Arrival Date's own "/"
 * characters come much later positionally).
 *
 * Example (verified against a real printed label):
 *   SS12026013006  126010E010000001/000500000061PITAI1 30/01/202607:3011A001IFN4  - R00
 *   → Plant=S Dock=S1 Order=2026013006 PartNo=126010E01000 BoxSeq/Total=0001/0005
 *     Qty=6 Supplier=1PIT S.plant=A S.dock=I1 Date=30/01/2026 Time=07:30
 *     Lane=11 Kbn=A001 Conveyance=I Address="FN4  - R00"
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
    private const val BOX_SEQ_WIDTH = 4

    // Sanity bound on Part No.'s length — not meant to catch a subtle one-
    // character-off scan (genuinely indistinguishable from a real Part No.
    // one character longer/shorter now that width varies), only to reject
    // obviously-wrong input where the "/" search latched onto something
    // that isn't really this field at all.
    private const val MIN_PART_NO_LENGTH = 4
    private const val MAX_PART_NO_LENGTH = 20

    /** Returns null for anything that doesn't decode cleanly — see the file doc comment for the algorithm, which must stay in lockstep with the backend's decodeLocalFreeZoneQr. */
    fun parse(raw: String): ParsedKbn? {
        return try {
            var pos = 0
            fun take(n: Int): String {
                val v = raw.substring(pos, pos + n)
                pos += n
                return v
            }
            fun expect(ch: Char) {
                if (pos >= raw.length || raw[pos] != ch) throw IllegalArgumentException("expected '$ch' at $pos")
                pos += 1
            }

            val shop = take(1)
            val dock = take(2)
            val orderNumber = take(10)
            expect(' ')

            // Anchor on the first "/" — Box Seq is always the 4 digits right
            // before it, Part No. is everything before THAT (variable length,
            // trimmed of any leftover padding — see the file doc comment).
            val partNoStart = pos
            val slashIndex = raw.indexOf('/', pos)
            if (slashIndex == -1) return null
            if (slashIndex - partNoStart < BOX_SEQ_WIDTH) return null
            val partNumber = raw.substring(partNoStart, slashIndex - BOX_SEQ_WIDTH).trim()
            if (partNumber.length < MIN_PART_NO_LENGTH || partNumber.length > MAX_PART_NO_LENGTH) return null
            val boxSeq = raw.substring(slashIndex - BOX_SEQ_WIDTH, slashIndex).trim().toInt()
            pos = slashIndex
            expect('/')

            val totalBoxes = take(4).trim().toInt()
            val qtyPerBox = take(7).trim().toInt()
            val supplierCode = take(4)
            val sPlant = take(1)
            val sDock = take(2)
            expect(' ')
            val arrivalDate = take(10)
            val arrivalTime = take(5)
            val mrosLane = take(2)
            val kbnCode = take(4)
            val conveyance = take(1)
            val fullAddress = raw.substring(pos)
            if (fullAddress.isEmpty()) return null

            ParsedKbn(
                shop = shop,
                dock = dock,
                orderNumber = orderNumber,
                partNumber = partNumber,
                boxSeq = boxSeq,
                totalBoxes = totalBoxes,
                qtyPerBox = qtyPerBox,
                supplierCode = supplierCode,
                sPlant = sPlant,
                sDock = sDock,
                arrivalDate = arrivalDate,
                arrivalTime = arrivalTime,
                mrosLane = mrosLane,
                kbnCode = kbnCode,
                conveyance = conveyance,
                fullAddress = fullAddress
            )
        } catch (e: Exception) {
            null
        }
    }
}