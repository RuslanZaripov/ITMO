/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author Ruslan Zaripov
 */
class Solution : MonotonicClock {
    private var fc1 by RegularInt(0)
    private var fc2 by RegularInt(0)
    private var fc3 by RegularInt(0)

    private var sc1 by RegularInt(0)
    private var sc2 by RegularInt(0)
    private var sc3 by RegularInt(0)

    override fun write(time: Time) {
        // write right-to-left
        sc1 = time.d1
        sc2 = time.d2
        sc3 = time.d3

        fc3 = time.d3
        fc2 = time.d2
        fc1 = time.d1
    }

    override fun read(): Time {
        // read left-to-right
        val fr1 = fc1
        val fr2 = fc2
        val fr3 = fc3

        val sr3 = sc3
        val sr2 = sc2
        val sr1 = sc1

        return when {
            fr1 == sr1 && fr2 == sr2 && fr3 == sr3 -> Time(fr1, fr2, fr3)
            fr1 == sr1 && fr2 == sr2 -> Time(fr1, fr2, sr3)
            fr1 == sr1 -> Time(fr1, sr2, 0)
            else -> Time(sr1, 0, 0)
        }
    }
}