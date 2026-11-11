from typing import Any


SUPPORTED_LOCALES = {"zhcn", "enus", "malay", "hindi"}

PPT_STYLE_LABELS = {
    "简约": {"zhcn": "简约", "enus": "Minimal", "malay": "Minimalis", "hindi": "न्यूनतम"},
    "商务": {"zhcn": "商务", "enus": "Business", "malay": "Perniagaan", "hindi": "व्यावसायिक"},
    "清新": {"zhcn": "清新", "enus": "Fresh", "malay": "Segar", "hindi": "ताज़ा"},
    "活泼": {"zhcn": "活泼", "enus": "Vibrant", "malay": "Ceria", "hindi": "जीवंत"},
}

PPT_COLOR_LABELS = {
    "蓝色": {"zhcn": "蓝色", "enus": "Blue", "malay": "Biru", "hindi": "नीला"},
    "红色": {"zhcn": "红色", "enus": "Red", "malay": "Merah", "hindi": "लाल"},
    "绿色": {"zhcn": "绿色", "enus": "Green", "malay": "Hijau", "hindi": "हरा"},
    "橙色": {"zhcn": "橙色", "enus": "Orange", "malay": "Jingga", "hindi": "नारंगी"},
    "紫色": {"zhcn": "紫色", "enus": "Purple", "malay": "Ungu", "hindi": "बैंगनी"},
}

MESSAGES = {
    "zhcn": {
        "api.connected": "已连接用户：{user_id}",
        "api.requirement_received": "已收到需求，正在启动代理流程...",
        "api.generation_error": "生成过程中出错：{error}",
        "api.matching_images": "正在为课件匹配配图...",
        "api.assets_error": "图片/PPT 生成出错：{error}",
        "api.no_current_slide": "没有找到当前课件，请先生成课件。",
        "api.feedback_accepted": "感谢您的认可！即将开始为课件匹配配图...",
        "api.revision_requested": "收到您的修改意见，正在重新修订课件...",
        "api.revision_error": "修订过程中出错：{error}",
        "api.websocket_error": "WebSocket 错误：{error}",
        "assets.images_done": "配图匹配完成！共生成 {count} 张图片。正在生成PPT...",
        "assets.search_template": "正在搜索风格「{style}」、配色「{color}」的模板...",
        "assets.template_selected": "已选择模板：{template}，正在生成PPT（约需1-3分钟）...",
        "assets.ppt_done": "PPT生成完成！您可以在资源面板中下载。",
        "assets.ppt_failed": "PPT生成失败，请检查讯飞API配置。配图已完成。",
        "proxy.received_requirement": "收到需求: {requirement}",
        "proxy.slide_complete": "课件生成完成！Slide ID: {slide_id}",
        "designer.ask_style": (
            "在开始设计课件前，请告诉我您希望最终PPT使用的风格和配色，我将为您匹配合适的模板。\n\n"
            "风格选择：1.简约  2.商务  3.清新  4.活泼\n"
            "配色选择：蓝色 / 红色 / 绿色 / 橙色 / 紫色\n\n"
            "请回复（例如：简约 蓝色）"
        ),
        "designer.preference_recorded": "已记录您的偏好：风格「{style}」、配色「{color}」。正在匹配PPT模板...",
        "designer.template_matched": "已匹配模板：{template}。正在设计大纲...",
        "designer.revising_outline": "收到审核反馈，正在修改大纲...",
        "reviewer.reviewing": "正在审核大纲...",
        "reviewer.result": "审核结果: {result}",
        "reviewer.approved": "大纲已批准，提交给规划员...",
        "planner.assigning": "正在分配写作任务给三位撰写员...",
        "planner.assignment_line": "- Writer {index}: {count} 个章节",
        "planner.assignment_done": "任务分配完成：\n{assignment}",
        "planner.beautifying": "所有撰写员完成，正在美化格式...",
        "planner.revision_received": "收到修改反馈，正在修订课件...",
        "planner.slide_missing": "错误：未找到课件文件",
        "planner.revision_complete": "课件已修订完成。{summary}",
        "writer.started": "开始撰写分配的章节...",
        "writer.finished": "撰写完成，共 {count} 个章节",
    },
    "enus": {
        "api.connected": "Connected as user: {user_id}",
        "api.requirement_received": "Requirement received. Starting the agent workflow...",
        "api.generation_error": "Error during generation: {error}",
        "api.matching_images": "Matching images for the slide deck...",
        "api.assets_error": "Error generating images/PPT: {error}",
        "api.no_current_slide": "No current slide deck found. Generate a slide deck first.",
        "api.feedback_accepted": "Thanks for confirming. Matching images for the slide deck...",
        "api.revision_requested": "Revision request received. Updating the slide deck...",
        "api.revision_error": "Error during revision: {error}",
        "api.websocket_error": "WebSocket error: {error}",
        "assets.images_done": "Images matched. {count} images generated. Generating PPT...",
        "assets.search_template": "Searching for a template with style \"{style}\" and color \"{color}\"...",
        "assets.template_selected": "Template selected: {template}. Generating PPT (about 1-3 minutes)...",
        "assets.ppt_done": "PPT generation complete. You can download it from Resources.",
        "assets.ppt_failed": "PPT generation failed. Check the Xunfei API configuration. Images are ready.",
        "proxy.received_requirement": "Requirement received: {requirement}",
        "proxy.slide_complete": "Slide deck generated. Slide ID: {slide_id}",
        "designer.ask_style": (
            "Before I design the slide deck, choose the final PPT style and color so I can match a suitable template.\n\n"
            "Style: 1. Minimal  2. Business  3. Fresh  4. Vibrant\n"
            "Color: Blue / Red / Green / Orange / Purple\n\n"
            "Reply with a style and color, for example: Minimal Blue."
        ),
        "designer.preference_recorded": "Preference saved: style \"{style}\", color \"{color}\". Matching a PPT template...",
        "designer.template_matched": "Template matched: {template}. Designing the outline...",
        "designer.revising_outline": "Review feedback received. Revising the outline...",
        "reviewer.reviewing": "Reviewing the outline...",
        "reviewer.result": "Review result: {result}",
        "reviewer.approved": "Outline approved. Sending it to the planner...",
        "planner.assigning": "Assigning writing tasks to three writers...",
        "planner.assignment_line": "- Writer {index}: {count} sections",
        "planner.assignment_done": "Task assignment complete:\n{assignment}",
        "planner.beautifying": "All writers finished. Polishing the format...",
        "planner.revision_received": "Revision feedback received. Updating the slide deck...",
        "planner.slide_missing": "Error: slide deck file not found",
        "planner.revision_complete": "Slide deck revision complete. {summary}",
        "writer.started": "Writing assigned sections...",
        "writer.finished": "Writing complete, {count} sections",
    },
    "malay": {
        "api.connected": "Bersambung sebagai pengguna: {user_id}",
        "api.requirement_received": "Keperluan diterima. Memulakan aliran ejen...",
        "api.generation_error": "Ralat semasa penjanaan: {error}",
        "api.matching_images": "Memadankan imej untuk slaid...",
        "api.assets_error": "Ralat menjana imej/PPT: {error}",
        "api.no_current_slide": "Tiada slaid semasa ditemui. Jana slaid dahulu.",
        "api.feedback_accepted": "Terima kasih atas pengesahan. Memadankan imej untuk slaid...",
        "api.revision_requested": "Permintaan semakan diterima. Mengemas kini slaid...",
        "api.revision_error": "Ralat semasa semakan: {error}",
        "api.websocket_error": "Ralat WebSocket: {error}",
        "assets.images_done": "Imej dipadankan. {count} imej dijana. Menjana PPT...",
        "assets.search_template": "Mencari templat dengan gaya \"{style}\" dan warna \"{color}\"...",
        "assets.template_selected": "Templat dipilih: {template}. Menjana PPT (kira-kira 1-3 minit)...",
        "assets.ppt_done": "Penjanaan PPT selesai. Anda boleh memuat turunnya daripada Resources.",
        "assets.ppt_failed": "Penjanaan PPT gagal. Semak konfigurasi API Xunfei. Imej sudah tersedia.",
        "proxy.received_requirement": "Keperluan diterima: {requirement}",
        "proxy.slide_complete": "Slaid telah dijana. Slide ID: {slide_id}",
        "designer.ask_style": (
            "Sebelum saya mereka slaid, pilih gaya dan warna PPT akhir supaya saya boleh memadankan templat yang sesuai.\n\n"
            "Gaya: 1. Minimalis  2. Perniagaan  3. Segar  4. Ceria\n"
            "Warna: Biru / Merah / Hijau / Jingga / Ungu\n\n"
            "Balas dengan gaya dan warna, contohnya: Minimalis Biru."
        ),
        "designer.preference_recorded": "Keutamaan disimpan: gaya \"{style}\", warna \"{color}\". Memadankan templat PPT...",
        "designer.template_matched": "Templat dipadankan: {template}. Mereka rangka...",
        "designer.revising_outline": "Maklum balas semakan diterima. Mengemas kini rangka...",
        "reviewer.reviewing": "Menyemak rangka...",
        "reviewer.result": "Keputusan semakan: {result}",
        "reviewer.approved": "Rangka diluluskan. Menghantar kepada perancang...",
        "planner.assigning": "Mengagihkan tugasan penulisan kepada tiga penulis...",
        "planner.assignment_line": "- Writer {index}: {count} bahagian",
        "planner.assignment_done": "Pengagihan tugasan selesai:\n{assignment}",
        "planner.beautifying": "Semua penulis selesai. Mengemas format...",
        "planner.revision_received": "Maklum balas semakan diterima. Mengemas kini slaid...",
        "planner.slide_missing": "Ralat: fail slaid tidak ditemui",
        "planner.revision_complete": "Semakan slaid selesai. {summary}",
        "writer.started": "Menulis bahagian yang diberikan...",
        "writer.finished": "Penulisan selesai, {count} bahagian",
    },
    "hindi": {
        "api.connected": "उपयोगकर्ता के रूप में कनेक्ट हुआ: {user_id}",
        "api.requirement_received": "आवश्यकता मिल गई। एजेंट workflow शुरू हो रहा है...",
        "api.generation_error": "जनरेशन के दौरान त्रुटि: {error}",
        "api.matching_images": "स्लाइड डेक के लिए चित्र मिलाए जा रहे हैं...",
        "api.assets_error": "चित्र/PPT जनरेट करते समय त्रुटि: {error}",
        "api.no_current_slide": "वर्तमान स्लाइड डेक नहीं मिला। पहले स्लाइड डेक जनरेट करें।",
        "api.feedback_accepted": "पुष्टि के लिए धन्यवाद। स्लाइड डेक के लिए चित्र मिलाए जा रहे हैं...",
        "api.revision_requested": "संशोधन अनुरोध मिला। स्लाइड डेक अपडेट हो रहा है...",
        "api.revision_error": "संशोधन के दौरान त्रुटि: {error}",
        "api.websocket_error": "WebSocket त्रुटि: {error}",
        "assets.images_done": "चित्र मिल गए। {count} चित्र जनरेट हुए। PPT जनरेट हो रही है...",
        "assets.search_template": "शैली \"{style}\" और रंग \"{color}\" वाला template खोजा जा रहा है...",
        "assets.template_selected": "Template चुना गया: {template}. PPT जनरेट हो रही है (लगभग 1-3 मिनट)...",
        "assets.ppt_done": "PPT जनरेशन पूरी हुई। आप इसे Resources से डाउनलोड कर सकते हैं।",
        "assets.ppt_failed": "PPT जनरेशन विफल। Xunfei API configuration जांचें। चित्र तैयार हैं।",
        "proxy.received_requirement": "आवश्यकता मिली: {requirement}",
        "proxy.slide_complete": "स्लाइड डेक जनरेट हो गया। Slide ID: {slide_id}",
        "designer.ask_style": (
            "स्लाइड डेक डिजाइन करने से पहले अंतिम PPT शैली और रंग चुनें ताकि मैं सही template मिला सकूं।\n\n"
            "शैली: 1. न्यूनतम  2. व्यावसायिक  3. ताज़ा  4. जीवंत\n"
            "रंग: नीला / लाल / हरा / नारंगी / बैंगनी\n\n"
            "कृपया शैली और रंग लिखें, जैसे: न्यूनतम नीला."
        ),
        "designer.preference_recorded": "पसंद सहेजी गई: शैली \"{style}\", रंग \"{color}\". PPT template मिलाया जा रहा है...",
        "designer.template_matched": "Template मिला: {template}. Outline डिजाइन हो रहा है...",
        "designer.revising_outline": "Review feedback मिला। Outline संशोधित हो रहा है...",
        "reviewer.reviewing": "Outline review हो रहा है...",
        "reviewer.result": "Review result: {result}",
        "reviewer.approved": "Outline approved. Planner को भेजा जा रहा है...",
        "planner.assigning": "तीन writers को writing tasks दिए जा रहे हैं...",
        "planner.assignment_line": "- Writer {index}: {count} sections",
        "planner.assignment_done": "Task assignment complete:\n{assignment}",
        "planner.beautifying": "सभी writers ने पूरा किया। Format polished हो रहा है...",
        "planner.revision_received": "Revision feedback मिला। Slide deck update हो रहा है...",
        "planner.slide_missing": "त्रुटि: slide deck file नहीं मिली",
        "planner.revision_complete": "Slide deck revision complete. {summary}",
        "writer.started": "Assigned sections लिखे जा रहे हैं...",
        "writer.finished": "Writing complete, {count} sections",
    },
}


def normalize_locale(locale: str | None) -> str:
    normalized = (locale or "zhcn").lower().replace("-", "").replace("_", "")
    if normalized in {"en", "enus", "enus"} or normalized.startswith("en"):
        return "enus"
    if normalized in {"zh", "zhcn", "zhchs", "zhhans"} or normalized.startswith("zh"):
        return "zhcn"
    if normalized in {"ms", "msmy", "malay"}:
        return "malay"
    if normalized in {"hi", "hiin", "hindi"}:
        return "hindi"
    return normalized if normalized in SUPPORTED_LOCALES else "zhcn"


def ppt_label(locale: str | None, value: Any) -> str:
    text = str(value)
    labels = PPT_STYLE_LABELS.get(text) or PPT_COLOR_LABELS.get(text)
    if not labels:
        return text
    return labels.get(normalize_locale(locale), labels["zhcn"])


def t(locale: str | None, key: str, **params: Any) -> str:
    resolved_locale = normalize_locale(locale)
    template = MESSAGES.get(resolved_locale, MESSAGES["zhcn"]).get(key, MESSAGES["zhcn"].get(key, key))
    translated_params = {
        name: ppt_label(resolved_locale, value) if name in {"style", "color"} else value
        for name, value in params.items()
    }
    return template.format(**translated_params)


def language_instruction(locale: str | None) -> str:
    resolved_locale = normalize_locale(locale)
    instructions = {
        "zhcn": "请使用简体中文生成面向用户的内容和说明。",
        "enus": "Use English for all user-facing slide content and explanations.",
        "malay": "Gunakan bahasa Melayu untuk semua kandungan slaid dan penjelasan kepada pengguna.",
        "hindi": "उपयोगकर्ता को दिखने वाली सभी slide content और explanations हिंदी में लिखें।",
    }
    return instructions[resolved_locale]
