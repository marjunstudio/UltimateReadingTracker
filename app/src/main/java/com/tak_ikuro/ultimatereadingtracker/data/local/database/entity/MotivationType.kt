package com.tak_ikuro.ultimatereadingtracker.data.local.database.entity

enum class MotivationType(val displayName: String) {
    RECOMMENDATION("推薦された"),
    BESTSELLER("ベストセラー"),
    AUTHOR_FAN("著者のファン"),
    TOPIC_INTEREST("トピックへの興味"),
    COVER_DESIGN("表紙デザイン"),
    REVIEW("レビューを見て"),
    GIFT("プレゼント"),
    STUDY("学習・研究"),
    WORK("仕事関連"),
    OTHER("その他")
}