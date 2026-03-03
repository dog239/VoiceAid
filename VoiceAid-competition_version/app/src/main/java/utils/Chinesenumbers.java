package utils;

public class Chinesenumbers {
        // 将数字转换为中文大写（仅用于1到100）
        public static String numberToChinese(int number) {
            String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
            String result = "";

            if (number < 1 || number > 100) {
                throw new IllegalArgumentException("Number must be between 1 and 100.");
            }

            if (number <= 10) {
                result = chineseNumbers[number - 1];
            } else if (number < 20) {
                result = chineseNumbers[9] + chineseNumbers[number - 11]; // 十一到十九特殊处理
            } else if (number % 10 == 0) { // 整十
                result = chineseNumbers[(number / 10) - 1] + "十";
            } else { // 其余二十到九十九
                result = chineseNumbers[(number / 10) - 1] + "十" + chineseNumbers[(number % 10) - 1];
            }

            return result;
        }

        // 生成由“一”到“一百”的字符串数组
        public static String[] generateChineseNumbersArray(int num) {
            String[] array = new String[num];
            for (int i = 1; i <= num; i++) {
                array[i - 1] = numberToChinese(i); // 数组索引从0开始，所以需要i-1
            }
            return array;
        }
}
