package com.electroboys.lightsnap.domain.message

import com.electroboys.lightsnap.R
import com.electroboys.lightsnap.data.entity.Contact
import com.electroboys.lightsnap.data.entity.Message

class ContactAndChatObject {
    companion object {
        val contacts = listOf(
            Contact(
                "Mike",  // 运动型朋友
                R.drawable.ic_contact_1,
                "他说每周三都会来哦",
                false,
                true,
                listOf(
                    Message("今天打球太离谱了", true, "3:20 PM"),
                    Message("你绝对猜不到我今天在球场遇到了谁！", true, "3:20 PM"),
                    Message("咋了？又被人虐了？", false, "3:22 PM"),
                    Message("不是！遇到 NBA 退役的大神了", true, "3:25 PM"),
                    Message("哇，真的假的？！", false, "3:25 PM"),
                    Message("骗你干嘛，他还教我后撤步", true, "3:26 PM"),
                    Message("下次带我去！我要签名！", false, "3:30 PM"),
                    Message("他说每周三都会来", true, "3:32 PM"),
                    Message("对了，你的电话号码是多少？", false, "3:33 PM"),
                    Message("我的电话号码是15467832295", true, "3:34 PM")
                )
            ),
            Contact(
                "Julie",
                R.drawable.ic_contact_2,
                "等我补个口红！",
                true,
                false,
                listOf(
                    Message("我恋爱了", false, "5:15 PM"),
                    Message(("啊？！"), true, "5:16 PM"),
                    Message("和火锅店老板的牛油锅底", false, "5:16 PM"),
                    Message("(翻白眼表情) 地址发来", true, "5:17 PM"),
                    Message("就在你家对面那栋楼", false, "5:18 PM"),
                    Message("现在立刻出门？", true, "5:18 PM"),
                    Message("等我补个口红", false, "5:20 PM")
                )
            ),
            Contact(
                "Charlie",
                R.drawable.ic_contact_3,
                "......再信你一次",
                false,
                true,
                listOf(
                    Message("新赛季双排吗？", true, "11:29 PM"),
                    Message("连跪八把我要卸载游戏了", false, "11:30 PM"),
                    Message("别啊！我刚研究出无敌套路", true, "11:32 PM"),
                    Message("上次你也这么说，结果0-12", false, "11:33 PM"),
                    Message("这次真的！偷塔流猫咪", true, "11:35 PM"),
                    Message("(游戏分享链接)", true, "11:36 PM"),
                    Message("......再信你一次", false, "11:40 PM")
                )
            ),
            Contact(
                "David",
                R.drawable.ic_contact_4,
                "速速过来！",
                true,
                false,
                listOf(
                    Message("发现个超棒的爵士酒吧", false, "7:05 PM"),
                    Message("又是那种人均消费500的？", true, "7:10 PM"),
                    Message("周三学生半价！", false, "7:11 PM"),
                    Message("可我们毕业三年了...", true, "7:12 PM"),
                    Message("(学生证照片)", false, "7:12 PM"),
                    Message("...太强了", true, "7:15 PM"),
                    Message("速速过来！", false, "7:17 PM")
                )
            ),
            Contact(
                "Eve",
                R.drawable.ic_contact_5,
                "有道理！",
                false,
                false,
                listOf(
                    Message("帮我选一下这两件衣服哪件更好看！！", false, "2:10 PM"),
                    Message("(惊喜表情包)", false, "2:10 PM"),
                    Message("(柠檬表情包)", true, "2:11 PM"),
                    Message("左边显瘦右边显白", false, "2:15 PM"),
                    Message("小孩子才做选择", true, "2:16 PM"),
                    Message("...所以？", false, "2:17 PM"),
                    Message("都买！反正你那么富有", true, "2:18 PM"),
                    Message("有道理！", false, "2:20 PM")
                )
            )
        )
    }

}