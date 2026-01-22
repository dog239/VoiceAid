package utils;

import java.util.Arrays;
import java.util.List;

import bean.a;

public interface ImageUrls {
    String[] E_imageUrls = {"cloth", "horse", "rope", "ship", "spoon",
            "television","socks","bike","goat","belt",
            "chair","bucket","comb","glove","banana",
            "broom","scissors"};

    String[] E_imageUrlsC = {"衣服", "马", "绳子", "船", "勺子",
            "电视","袜子","自行车","羊","皮带",
            "椅子","桶","梳子","手套","香蕉",
            "扫帚","剪刀"};

    String[] RE_imageUrls = { "woodentable","tennis","dog","watch","cup",
            "scissors","eyes","cow","umbrella","box",
            "chair","leaf","mouth","ear","nose",
            "foot","hand","body" };
    String[] RE_imageUrlsC = {"桌子","球","狗","手表","杯子",
            "剪刀","眼睛","牛","雨伞","盒子",
            "椅子","叶子","嘴巴","耳朵","鼻子",
            "脚","手","身子"};
    int[][] RE_turn = new int[][]{{7,0,8,9,10,11},
            {6,3,8,11,1,10},
            {3,1,2,5,9,7},
            {0,4,6,3,5,7},
            {11,10,6,4,2,1},
            {3,8,11,1,5,10},
            {2,3,4,5,6,7},
            {10,11,7,1,0,5},
            {2,8,9,3,1,0},
            {6,5,4,9,8,7},
            {10,3,6,7,11,4},
            {0,2,4,8,10,11},
            {14,17,15,12,16,13},
            {14,17,15,12,16,13},
            {14,17,15,12,16,13},
            {14,17,15,12,16,13},
            {14,17,15,12,16,13},
            {14,17,15,12,16,13}
    };

    String[] S_words = { "咖啡和茶","白菜和黄瓜","花和草","自行车和飞机","冰箱和洗衣机",
            "铅笔和橡皮","酱油和盐","项链和耳环","钢琴和小提琴","佛教和基督教"};

    String[] S_wordsAns = {"饮料","蔬菜","植物","交通工具","家用电器","文具","调料","首饰/饰品","乐器","宗教"};

//    String[][] PST_hints = {{"N1.11","N1.12","N1.13","N1.14"},
//            {"N1.21","N1.22","N1.23","N1.24"},
//            {"N1.31","N1.32","N1.33","N1.34"},
//            {"N1.41","N1.42","N1.43","N1.44"},
//            {"N1.51","N1.52","N1.53","N1.54"},
//    };

    String[] PN_hints = {"N2.1","N2.2","N2.3"};
    String[] PST_imageUrls = {"pst1","pst2","pst3","pst4","pst5"};


    String[] PN_imageUrls = { "go_to_hospital","fall_off_the_bike","slide_and_children"};

    String[] A_imageUrls = {"nose","foot","mouth","tennis","car","woodentable","umbrella","bird","plane","hair",
                            "door","piano","racing","crab","watermelon","dress","apple","vegetables","flower","fatmeat",
                            "ear","mouse","chopsticks","shorts","milk","lantern","panda","hotwater","stairs","stick",
                            "moon","bag","finger","rainbow","bed","girl","clamp","sun","ring","brushteeth","goodbye"
                            };

    String[] A_imageUrlsC = {"鼻子","脚","嘴","球","汽车","桌子","雨伞","鸟","飞机","头发",
                            "木门","钢琴","赛跑","螃蟹","西瓜","裙子","苹果","蔬菜","花","肥肉",
                            "耳朵","老鼠","筷子","短裤","牛奶","灯笼","熊猫","热水","楼梯","棍子",
                            "月亮","书包","手指","彩虹","床","女孩","夹子","太阳","圆圈","刷牙","再见"
                            };
    String[][] A_proAns = {{"/b/","/z/"},
            {"/j/",""},
            {"/z/",""},
            {"/q/",""},
            {"/q/","/ch/"},
            {"/zh/","/z/"},
            {"/y/","/s/"},
            {"/n/",""},
            {"/f/","/j/"},
            {"/t/","/f/"},
            {"/m/","/m/"},
            {"/g/","/q/"},
            {"/s/","/p/"},
            {"/p/","/x/"},
            {"/x/","/g/"},
            {"/q/","/z/"},
            {"/p/","/g/"},
            {"/sh/","/c/"},
            {"/h/",""},
            {"/f/","/r/"},
            {"/er/","/d/"},
            {"/l/","/sh/"},
            {"/k/","/z/"},
            {"/d/","/k/"},
            {"/n/","/n/"},
            {"/d/","/l/"},
            {"/x/","/m/"},
            {"/r/","/sh/"},
            {"/l/","/t/"},
            {"/g/","/z/"},
            {"/y/","/l/"},
            {"/sh/","/b/"},
            {"/sh/","/zh/"},
            {"/c/","/h/"},
            {"/ch/",""},
            {"/n/","/h/"},
            {"/j/","/z/"},
            {"/t/","/y/"},
            {"/y/","/q/"},
            {"/sh/","/y/"},
            {"/z/","/j/"},
    };
    String[] A_characs = {"/b/","/p/","/m/","/f/","/d/","/t/","/n/","/l/","/s/","/z/","/c/","/x/","/j/","/q/",
                            "/sh/","/r/","/zh/","/ch/","/g/","/k/","/h/"};
    int[]  A_nums= {2, 3, 3, 3, 3, 3, 4, 4, 2, 8, 2, 3, 3, 5, 6, 2, 2, 2, 4, 2, 3};

    String[] A_newImageUrls = {
            "new_cry", "new_soccer", "new_apple", "new_candy", "new_stool", "new_cloud", "new_fork", "new_chopsticks", "new_meat", "new_book",
            "new_run", "new_cat", "new_mouth", "new_person", "new_banana", "new_moon", "new_orange", "new_tooth", "new_snake", "new_umbrella",
            "new_milk", "new_kite", "new_bowl", "new_bed", "new_grass", "new_scallion", "new_door", "new_strawberry", "new_ear", "new_swim_ring",
            "new_swallow", "new_scarf", "new_needle", "new_tiger", "new_cucumber", "new_boat", "new_radish", "new_table", "new_red", "new_shoes",
            "new_hair", "new_cow", "new_garlic", "new_fan", "new_potato", "new_sunflower", "new_fish", "new_bubble", "new_turtle", "new_medicine",
            "new_purple", "new_skirt", "new_smell"
    };

    String[] A_newImageUrlsC = {"哭", "足球", "苹果", "糖", "凳", "白云", "叉", "筷", "肉", "书",
            "跑步", "猫", "嘴", "人", "香蕉", "月亮", "橘", "牙", "蛇", "雨伞",
            "牛奶", "风筝", "碗", "床", "草", "葱", "门", "草莓", "耳朵", "游泳圈",
            "燕", "围巾", "针", "老虎", "黄瓜", "船", "萝卜", "桌", "红色", "鞋",
            "头发", "牛", "蒜", "风扇", "土豆", "向日葵", "鱼", "泡泡", "乌龟", "药",
            "紫色", "裙", "闻"};

    String[] A_newImageUrlsPinyin = {"ku", "zuqiu", "pingguo", "tang", "deng", "baiyun", "cha", "kuai",
            "rou", "shu", "paobu", "mao", "zui", "ren", "xiangjiao", "yueliang", "ju", "ya", "she", "yusan",
            "niunai", "fengzheng", "wan", "chuang", "cao", "cong", "men", "caomei", "erduo", "youyongquan",
            "yan", "weijin", "zhen", "laohu", "huanggua", "chuan", "luobo", "zhuo", "hongse", "xie", "toufa",
            "niu", "suan", "fengshan", "tudou", "xiangrikui", "yu", "paopao", "wugui", "yao", "zise", "qun", "wen"};

    a.CharacterPhonology[][] A_targetWord = new a.CharacterPhonology[A_newImageUrlsC.length][];

    static boolean useNewAPhonology() {
        return A_newImageUrls != null && A_newImageUrls.length > 0;
    }

    static String[] getAImageUrls() {
        return useNewAPhonology() ? A_newImageUrls : A_imageUrls;
    }

    static String[] getAImageUrlsC() {
        return useNewAPhonology() ? A_newImageUrlsC : A_imageUrlsC;
    }

    static int getAImageCount() {
        return getAImageUrls().length;
    }

    static a.CharacterPhonology cp(String hanzi, String initial, String medial, String nucleus, String coda, boolean isInducible) {
        a.PhonologyPart part = new a.PhonologyPart();
        part.initial = initial;
        part.medial = medial;
        part.nucleus = nucleus;
        part.coda = coda;
        part.isInducible = isInducible;
        a.CharacterPhonology cp = new a.CharacterPhonology();
        cp.hanzi = hanzi;
        cp.phonology = part;
        return cp;
    }

    static List<a.CharacterPhonology> toList(a.CharacterPhonology[] arr) {
        return arr == null ? null : Arrays.asList(arr);
    }

    static int indexOfAWord(String word) {
        for (int i = 0; i < A_newImageUrlsC.length; i++) {
            if (word.equals(A_newImageUrlsC[i])) return i;
        }
        return -1;
    }

    static void setAWord(String word, a.CharacterPhonology... cps) {
        int idx = indexOfAWord(word);
        if (idx >= 0) {
            A_targetWord[idx] = cps;
        }
    }

    static void initAPhonologyLexicon() {
        Arrays.fill(A_targetWord, null);

        setAWord("哭", cp("哭", "k", "", "u", "", false));
        setAWord("足球", cp("足", "z", "", "u", "", false), cp("球", "q", "i", "o", "u", false));
        setAWord("苹果", cp("苹", "p", "", "i", "ng", false), cp("果", "g", "u", "o", "", false));
        setAWord("糖", cp("糖", "t", "", "a", "ng", false));
        setAWord("凳", cp("凳", "d", "", "e", "ng", false));
        setAWord("白云", cp("白", "b", "", "a", "i", false), cp("云", "", "", "ü", "n", false));
        setAWord("叉", cp("叉", "ch", "", "a", "", false));
        setAWord("筷", cp("筷", "k", "u", "a", "i", false));
        setAWord("肉", cp("肉", "r", "", "o", "u", false));
        setAWord("书", cp("书", "sh", "", "u", "", false));
        setAWord("跑步", cp("跑", "p", "", "a", "o", false), cp("步", "b", "", "u", "", false));
        setAWord("猫", cp("猫", "m", "", "a", "o", false));
        setAWord("嘴", cp("嘴", "z", "u", "e", "i", false));
        setAWord("人", cp("人", "r", "", "e", "n", false));
        setAWord("香蕉", cp("香", "x", "i", "a", "ng", false), cp("蕉", "j", "i", "a", "o", false));
        setAWord("月亮", cp("月", "", "ü", "e", "", false), cp("亮", "l", "i", "a", "ng", false));
        setAWord("橘", cp("橘", "j", "", "ü", "", false));
        setAWord("牙", cp("牙", "", "", "a", "", false));
        setAWord("蛇", cp("蛇", "sh", "", "e", "", false));
        setAWord("雨伞", cp("雨", "", "", "ü", "", false), cp("伞", "s", "", "a", "n", false));
        setAWord("牛奶", cp("牛", "n", "i", "o", "u", false), cp("奶", "n", "", "a", "i", false));
        setAWord("风筝", cp("风", "f", "", "e", "ng", false), cp("筝", "zh", "", "e", "ng", false));
        setAWord("碗", cp("碗", "", "u", "a", "n", false));
        setAWord("床", cp("床", "ch", "u", "a", "ng", false));
        setAWord("草", cp("草", "c", "", "a", "o", false));
        setAWord("葱", cp("葱", "c", "", "o", "ng", false));
        setAWord("门", cp("门", "m", "", "e", "n", false));
        setAWord("草莓", cp("草", "c", "", "a", "o", false), cp("莓", "m", "", "e", "i", false));
        setAWord("耳朵", cp("耳", "", "", "er", "", false), cp("朵", "d", "u", "o", "", false));
        setAWord("游泳圈", cp("游", "", "i", "o", "u", false), cp("泳", "", "i", "o", "ng", false), cp("圈", "q", "ü", "a", "n", false));
        setAWord("燕", cp("燕", "", "i", "a", "n", false));
        setAWord("围巾", cp("围", "", "u", "e", "i", false), cp("巾", "j", "", "i", "n", false));
        setAWord("针", cp("针", "zh", "", "e", "n", false));
        setAWord("老虎", cp("老", "l", "", "a", "o", false), cp("虎", "h", "", "u", "", false));
        setAWord("黄瓜", cp("黄", "h", "u", "a", "ng", false), cp("瓜", "g", "u", "a", "", false));
        setAWord("船", cp("船", "ch", "u", "a", "n", false));
        setAWord("萝卜", cp("萝", "l", "u", "o", "", false), cp("卜", "b", "", "o", "", false));
        setAWord("桌", cp("桌", "zh", "u", "o", "", false));
        setAWord("红色", cp("红", "h", "", "o", "ng", false), cp("色", "s", "", "e", "", false));
        setAWord("鞋", cp("鞋", "x", "i", "e", "", false));
        setAWord("头发", cp("头", "t", "", "o", "u", false), cp("发", "f", "", "a", "", false));
        setAWord("牛", cp("牛", "n", "i", "o", "u", false));
        setAWord("蒜", cp("蒜", "s", "u", "a", "n", false));
        setAWord("风扇", cp("风", "f", "", "e", "ng", false), cp("扇", "sh", "", "a", "n", false));
        setAWord("土豆", cp("土", "t", "", "u", "", false), cp("豆", "d", "", "o", "u", false));
        setAWord("向日葵", cp("向", "x", "i", "a", "ng", false), cp("日", "r", "", "i", "", false), cp("葵", "k", "u", "e", "i", false));
        setAWord("鱼", cp("鱼", "", "", "ü", "", false));
        setAWord("泡泡", cp("泡", "p", "", "a", "o", false), cp("泡", "p", "", "a", "o", false));
        setAWord("乌龟", cp("乌", "", "", "u", "", false), cp("龟", "g", "u", "e", "i", false));
        setAWord("药", cp("药", "", "", "a", "o", false));
        setAWord("紫色", cp("紫", "z", "", "i", "", false), cp("色", "s", "", "e", "", false));
        setAWord("裙", cp("裙", "q", "", "ü", "n", false));
        setAWord("闻", cp("闻", "", "u", "e", "n", false));
    }

    static String getAPinyin(String word) {
        int idx = indexOfAWord(word);
        if (idx >= 0 && idx < A_newImageUrlsPinyin.length) return A_newImageUrlsPinyin[idx];
        return "";
    }

String[][] NWR_characs = {{"把ba3","丹dan1","召zhao4","歹dai3","库ku4","尚shang4"},
        {"商shang1","楷kai3","到dao4","尬ga4","展zhan3","铺pu4"},
        {"帮bang1","债zhai4","看kan4","赌du3","刹sha1","搞gao3"},
        {"战zhan4","搭da1","树shu4","稿gao3","掰bai1","抗kang4"},
        {"嘎ga2","少shao3","棒bang4","苦ku3","胆dan3","摘zhai1"},
        {"稍shao1","嘎ga2","办ban4","污wu1","康kang1","歹dai3"}};


    String[][] NWR_characsC = {{"把","丹","召","歹","库","尚"},
            {"商","楷","到","尬","展","铺"},
            {"帮","债","看","赌","刹","搞"},
            {"战","搭","树","稿","掰","抗"},
            {"嘎","少","棒","苦","胆","摘"},
            {"稍","嘎","办","污","康","歹"}};


String[] RGC_hints={"鸭子在椅子上","妈妈开门","爸爸洗碗","找出最高的人","找出最胖的人",
        "爸爸在骑车","闭眼的猫","弟弟喝水","妹妹把球抱住", "哪只狗不是黑的",
        "苹果在盒子里面","汽车撞倒摩托车","他们在玩","小妹妹没有跑步","小朋友戴的帽子不是圆的",
        "弟弟在开门","猫在篮子旁边","弟弟坐的椅子是三条腿的","弟弟和妹妹都没有跳", "一些鸟",
        "因为妹妹没有球，所以她抱了一只猫",};
String[] RGC_Ans = {"A","B","B","D","C","D","D","C","A","B",
        "B","A","D","A","B","B","C","B","D","A","A"};
String[] RG_hints={"弟弟拍球","爸爸吃饭","姐姐喝水","爸爸在骑车","爸爸把衣服脱下",
        "妹妹把球抱住","猫在椅子上面","苹果在盒子里面","没有眼睛的猫","没有穿衣服的叔叔", "比较高",
        "比较胖","小妹妹没有跑步","小妹妹没有游泳","一条鱼要吃小虫","妈妈把门打开",
        "猫在篮子旁边","汽车撞摩托车","哪只狗不是黑的","哪一个不是猫", "两个一样",
        "哪两个不一样","他们在玩","我们在吃苹果","爸爸在穿皮鞋","弟弟在开门","撞火车的汽车",
        "黑色的狗追大黄猫","看书的哥哥","拍球的弟弟","妈妈吃过饭了","爸爸穿好鞋了","树在房子中间",
        "小鸟在笼子外面","手在球下面","狗在汽车前面","小朋友戴的帽子不是圆的","一半苹果","一些鸟",
        "妹妹拿的球是黑的","弟弟坐的椅子是三条腿的","妹妹面向爸爸","妈妈离开厨房","妈妈拉爸爸起床",
        "大象推小猫下水","香蕉背苹果上街","戴帽子的小猫咬大象","穿衣服的大象抱小猫","弟弟和妹妹都没有跳",
        "小猫和小狗都没有跑","因为妹妹没有球，所以她抱了一只猫","爸爸吃饭的时候，妹妹在看电视",
        "哥哥告诉弟弟大狗狗拿着一个气球","小猫咪告诉小猪大狗狗趴在桌子下面","大象被小猫咬了","弟弟被姐姐推",
        "大象被小猫追","弟弟和妹妹推爸爸","弟弟追小狗和小猫","妹妹对哥哥说这里有半个苹果"};



String[] RG_Ans = {"B","C","B","C","D",
        "B","A","B","A","B",
        "D","D","A","B","C",
        "D","B","A","B","C",
        "D","B","A","B","A",
        "C","C","B","A","B",
        "B","A","A","D","D",
        "C","D","C","B","A",
        "C","A","B","C","D",
        "D","C","C","A","A",
        "A","D","A","A","D",
        "C","D","D","C","C"};
}
