package com.softx.app

import com.softx.app.data.PocketNativeLoanDueReminder
import kotlin.random.Random

data class DuoNotificationPayload(
    val title: String,
    val message: String,
    val bigText: String,
    val actionLabel: String,
    val emoji: String,
)

object PocketDuoReminderEngine {

    private val UNLOGGED_BANGLA_MEMES = listOf(
        DuoNotificationPayload(
            title = "মানিব্যাগ কোণায় বসে কাঁদছে! 🥺💸",
            message = "আজ কি কোনো খরচ করেননি নাকি লিখতে ভুলে গেছেন? ঝটপট এন্ট্রি দিন!",
            bigText = "টাকা কোথায় গেলো ভাবার চেয়ে, পকেটে লিখে ফেলা ভালো! ঘুমানোর আগে আজকের খরচের হিসাবটা লিখে ফেলুন, নাহলে কাল সকালে মাথা চুলকাতে হবে! 😂",
            actionLabel = "➕ খরচ লিখুন",
            emoji = "🥺",
        ),
        DuoNotificationPayload(
            title = "আজকে কি চা-সিঙ্গারা ফ্রি ছিলো? ☕👀",
            message = "নাকি অলসতার কারণে হিসাব লিখছেন না? মাত্র ১০ সেকেন্ড লাগবে!",
            bigText = "আজকের খরচটা লিখে ফেলুন। ছোট ছোট হিসাব না রাখলে মাস শেষে বড় ধাক্কা খাবেন! 💸",
            actionLabel = "➕ খরচ লিখুন",
            emoji = "☕",
        ),
        DuoNotificationPayload(
            title = "পকেট পেঁচা বলছি... চুপচাপ হিসাব দিন! 🦉🔫",
            message = "আজকের খরচ এন্ট্রি দিন, কারো কোনো ক্ষতি হবে না! 😉",
            bigText = "ডুওলিঙ্গো যেমন ভাষা শেখায়, পকেট প্রো আপনাকে টাকা বাঁচাতে শেখায়! আজকের হিসাব মিটিয়ে দিন।",
            actionLabel = "➕ এন্ট্রি দিন",
            emoji = "🦉",
        ),
        DuoNotificationPayload(
            title = "ঘুমানোর আগে হিসাব মিটিয়ে নিন! 💀",
            message = "নাহলে কাল সকালে টাকা উধাও দেখে হার্টঅ্যাটাক হতে পারে!",
            bigText = "হিসাব ছাড়া খরচ মানে ফুটো পকেটে টাকা রাখা। আজকের আয়-ব্যয় লিখে নিশ্চিন্তে ঘুমান 🌙",
            actionLabel = "➕ হিসাব লিখুন",
            emoji = "💀",
        ),
        DuoNotificationPayload(
            title = "আপনার সেভিংস গোল আপনাকে মিস করছে! 💔",
            message = "আজকে কোনো টাকা জমালেন বা খরচ করলেন? রেকর্ড করুন!",
            bigText = "লক্ষ্যে পৌঁছাতে হলে প্রতিদিনের সচেতনতাই আসল শক্তি। ১ মিনিট ব্যয় করুন নিজের ভবিষ্যতের জন্য। 🎯",
            actionLabel = "➕ আপডেট দিন",
            emoji = "🎯",
        ),
    )

    private val UNLOGGED_ENGLISH_MEMES = listOf(
        DuoNotificationPayload(
            title = "Your wallet is crying right now 🥺💸",
            message = "Did you really spend \$0 today or did you just forget to log it?",
            bigText = "Don't let your money ghost you! Take 10 seconds tonight to log today's expenses before bed. 🌙",
            actionLabel = "➕ Add Expense",
            emoji = "🥺",
        ),
        DuoNotificationPayload(
            title = "Was that coffee free today? ☕👀",
            message = "Didn't think so! Log it before you forget where the cash vanished.",
            bigText = "Where did all my money go? — You tomorrow morning if you don't log your transactions tonight! 💀",
            actionLabel = "➕ Log Coffee",
            emoji = "☕",
        ),
        DuoNotificationPayload(
            title = "The Pocket Owl is watching... 🦉🔫",
            message = "Log your daily spending and nobody gets hurt! 😉",
            bigText = "Just like Duolingo keeps your language sharp, Pocket Pro protects your hard-earned wealth. 10 seconds is all it takes!",
            actionLabel = "➕ Log Now",
            emoji = "🦉",
        ),
        DuoNotificationPayload(
            title = "These reminders don't seem to work... 🦉💔",
            message = "I'll stop bothering you now... unless you log today's expenses?",
            bigText = "Take 10 seconds tonight to keep your financial discipline alive. Sleep peacefully knowing every penny is accounted for!",
            actionLabel = "➕ Quick Entry",
            emoji = "🦉",
        ),
    )

    fun generatePayload(
        loggedToday: Boolean,
        streakDays: Int,
        todayEntryCount: Int,
        overdueLoan: PocketNativeLoanDueReminder?,
        isBangla: Boolean,
    ): DuoNotificationPayload {
        // Scenario 1: Active Streak in Danger (User hasn't logged today and streak > 1)
        if (!loggedToday && streakDays > 1) {
            return if (isBangla) {
                DuoNotificationPayload(
                    title = "🔥 আপনার $streakDays দিনের স্ট্রিক বিপদে!",
                    message = "আজকের হিসাব লিখে স্ট্রিকের আগুন ধরে রাখুন!",
                    bigText = "টানা $streakDays দিন আপনি নিখুঁত হিসাব রাখছেন! আজকের এন্ট্রি না দিলে এই অসাধারণ রেকর্ডটি আজ রাতেই ভেঙে যাবে। ১ ট্যাপে এন্ট্রি দিন! 🚀",
                    actionLabel = "🔥 স্ট্রিক রক্ষা করুন",
                    emoji = "🔥",
                )
            } else {
                DuoNotificationPayload(
                    title = "🔥 Your $streakDays-day streak is on the line!",
                    message = "Log today's entry to keep your financial fire burning!",
                    bigText = "You've been tracking money for $streakDays days straight! Don't let your streak reset to zero tonight.",
                    actionLabel = "🔥 Save Streak",
                    emoji = "🔥",
                )
            }
        }

        // Scenario 2: Outstanding Loan to collect
        if (overdueLoan != null && overdueLoan.kind == "lent") {
            val party = overdueLoan.counterparty.ifBlank { "বাকিদার" }
            return if (isBangla) {
                DuoNotificationPayload(
                    title = "💸 বাকি টাকা আদায়ের সময় হয়েছে!",
                    message = "$party এর কাছে বাকি টাকা পাবেন। তাগাদা পাঠাবেন নাকি মাফ করে দিয়েছেন? 😂",
                    bigText = "পকেট প্রো থেকে ১ ট্যাপে সম্মানজনক হোয়াটসঅ্যাপ তাগাদা পাঠিয়ে দিন। নিজের টাকা চাইতে লজ্জা কিসের? 📲",
                    actionLabel = "📲 তাগাদা পাঠান",
                    emoji = "💸",
                )
            } else {
                DuoNotificationPayload(
                    title = "💸 Time to collect your money!",
                    message = "$party still owes you money. Sending a reminder or feeling generous? 😉",
                    bigText = "Send a respectful reminder with one tap right from Pocket Pro! Keep your cash flow intact.",
                    actionLabel = "📲 Remind",
                    emoji = "💸",
                )
            }
        }

        // Scenario 3: User hasn't logged today -> Pick a funny meme
        if (!loggedToday) {
            val list = if (isBangla) UNLOGGED_BANGLA_MEMES else UNLOGGED_ENGLISH_MEMES
            return list[Random.nextInt(list.size)]
        }

        // Scenario 4: User HAS logged today -> Celebrate!
        return if (isBangla) {
            DuoNotificationPayload(
                title = "🏆 আর্থিক বস! আজকের কাজ সম্পন্ন!",
                message = "আজকে $todayEntryCount টি এন্ট্রি দিয়েছেন। স্ট্রিক এখন $streakDays দিন! 🔥",
                bigText = "দারুণ কাজ! প্রতিদিন এই অভ্যাস বজায় রাখলে আপনার সঞ্চয় বাড়বে এবং অর্থ অপচয় বন্ধ হবে। নিশ্চিন্তে ঘুমান! 🌙",
                actionLabel = "👀 ওয়ালেট দেখুন",
                emoji = "🏆",
            )
        } else {
            DuoNotificationPayload(
                title = "🏆 Financial Boss! Day Completed!",
                message = "You logged $todayEntryCount entries today. Streak is now $streakDays days! 🔥",
                bigText = "Great discipline! Consistent daily tracking puts you in the top 1% of smart money managers. Sleep well! 🌙",
                actionLabel = "👀 View Summary",
                emoji = "🏆",
            )
        }
    }
}
