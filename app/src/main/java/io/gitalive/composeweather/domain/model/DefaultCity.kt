package io.gitalive.composeweather.domain.model

/** 首次启动时的默认城市，收藏为空时写入。 */
val DefaultCity = City(
    name = "北京",
    latitude = 39.9042,
    longitude = 116.4074,
    country = "中国",
    admin1 = "北京市"
)
