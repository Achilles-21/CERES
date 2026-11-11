from .ppt_generator import parse_ppt_style_input
from .slidegen_i18n import normalize_locale, t


def test_normalize_locale_aliases():
    assert normalize_locale("en-US") == "enus"
    assert normalize_locale("enus") == "enus"
    assert normalize_locale("zhcn") == "zhcn"
    assert normalize_locale("malay") == "malay"
    assert normalize_locale("hi-IN") == "hindi"


def test_designer_preference_message_uses_locale():
    zh_message = t("zhcn", "designer.preference_recorded", style="简约", color="蓝色")
    en_message = t("enus", "designer.preference_recorded", style="简约", color="蓝色")

    assert zh_message == "已记录您的偏好：风格「简约」、配色「蓝色」。正在匹配PPT模板..."
    assert en_message == "Preference saved: style \"Minimal\", color \"Blue\". Matching a PPT template..."


def test_parse_ppt_style_input_accepts_english_ui_reply():
    assert parse_ppt_style_input("minimal blue") == ("简约", "蓝色")
    assert parse_ppt_style_input("business red") == ("商务", "红色")


if __name__ == "__main__":
    test_normalize_locale_aliases()
    test_designer_preference_message_uses_locale()
    test_parse_ppt_style_input_accepts_english_ui_reply()
    print("slideGen localization tests passed")
