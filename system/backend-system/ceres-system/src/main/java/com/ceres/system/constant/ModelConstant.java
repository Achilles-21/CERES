package com.ceres.system.constant;

public class ModelConstant {

    private static final String modelUrl = System.getenv().getOrDefault("CERES_MODEL_URL", "http://localhost:8000").replaceAll("/+$", "");

    public static final String MODEL_URL = modelUrl+"/model";

    public static final String MODEL_CLEAR_URL = modelUrl+"/clear_chat";

    public static final String AUDIO_TO_STRING_URL = modelUrl+"/recognize";

    public static final String VIDEO_POP_JUDGMENT_URL = modelUrl+"/course_invoke";

    public static final String SUMMARY_REPORT_URL = modelUrl+"/summary";

    public static final String TEXT_TO_AUDIO_URL = modelUrl+"/text_to_speech";

    public static final String USER_ID = "user_id";
    public static final String TIME_SPAN = "time_span";
    public static final String MODE_CODE = "mode_code";
    public static final String INPUT_TEXT = "input_text";
    public static final String Image = "image";

    public static final String OUTPUT_TEXT = "output_text";
    public static final String ERROR_CODE = "error_code";

    public static final String QUESTION = "question";
    public static final String CORRECT_ANS = "correct_ans";
    public static final String USER_ANS = "user_ans";

    public static final String QUESTION_IDS = "question_ids";

    public static final String COURSE_ID = "course_id";
    public static final String SUMMARY = "summary";

    public static final String REPORT_ID = "report_id";
    public static final String CONCEPT = "concept";
    public static final String PATIENT_TEXT = "patient_text";
    public static final String DOCTOR_TEXT = "doctor_text";

    public static final String AUDIO_TEXT = "text";
    public static final String AUDIO_GENDER = "gender";
    public static final String AUDIO_LANGUE = "language";

    public static final String SHOW_IMAGE_1 = "<div role=\"none\" class=\"n-image\" style=\"width: 100%;\"><img width=\"100%\"src=\"";
    public static final String SHOW_IMAGE_2 = "\" loading=\"eager\" data-error=\"false\" data-preview-src=\"";
    public static final String SHOW_IMAGE_3 = "\" data-group-id=\"\" style=\"object-fit: fill;\"><!----><!----></div>";
}
